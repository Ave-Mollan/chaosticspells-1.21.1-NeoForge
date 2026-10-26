package com.avemollan.chaosticspells.compat.viltrumite;

import com.mojang.brigadier.tree.CommandNode;
import dev.baranhan.viltrumitecore.config.ViltrumiteCoreConfig;
import dev.baranhan.viltrumitecore.util.ViltrumiteCorePlayer;
import dev.baranhan.viltrumiteflight.util.ViltrumiteFlightPlayer;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import java.lang.reflect.Field;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

/** Loaded only after both optional mods have finished loading. */
public final class ViltrumiteIntegration {
    // Brigadier is loaded by MC-BOOTSTRAP, outside the Mixin transformer.
    private static final Field COMMAND_REQUIREMENT = commandRequirementField();

    private static Field commandRequirementField() {
        try {
            Field field = CommandNode.class.getDeclaredField("requirement");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unsupported Brigadier command node", exception);
        }
    }

    private static void requirement(CommandNode<CommandSourceStack> node, Predicate<CommandSourceStack> predicate) {
        try {
            COMMAND_REQUIREMENT.set(node, predicate);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Unable to restrict Viltrumite command", exception);
        }
    }
    public static void install() {
        ViltrumiteCoreConfig.INSTANCE.shouldAskRace = false;
        ViltrumiteCoreConfig.INSTANCE.isViltrumiteByDefault = false;
        NeoForge.EVENT_BUS.register(ViltrumiteIntegration.class);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void commands(RegisterCommandsEvent event) {
        restrictNativeCommand(event.getDispatcher().getRoot().getChild("viltrumite"));
        event.getDispatcher().register(Commands.literal("ave")
                .requires(source -> AveViltrumiteAccess.isOwner(source.getEntity()))
                .then(Commands.literal("race")
                        .executes(context -> changeRace(context.getSource().getPlayerOrException(),
                                !AveViltrumiteAccess.selected(context.getSource().getPlayerOrException())))
                        .then(Commands.literal("human").executes(context -> changeRace(context.getSource().getPlayerOrException(), false)))
                        .then(Commands.literal("viltrumite").executes(context -> changeRace(context.getSource().getPlayerOrException(), true)))));
    }

    public static void restrictNativeCommand(CommandNode<CommandSourceStack> node) {
        if (node == null) return;
        Predicate<CommandSourceStack> original = node.getRequirement();
        requirement(node, source -> original.test(source) && AveViltrumiteAccess.isActive(source.getEntity()));
        // These bypass race selection or grant flight to other players; use /ave race instead.
        for (String name : new String[]{"power", "depower", "flight"}) {
            CommandNode<CommandSourceStack> child = node.getChild(name);
            if (child != null) requirement(child, source -> false);
        }
    }

    public static int changeRace(ServerPlayer player, boolean viltrumite) {
        if (!AveViltrumiteAccess.isOwner(player)) return 0;
        if (!viltrumite) clearPowers(player);
        player.getPersistentData().putBoolean(AveViltrumiteAccess.ENABLED_TAG, viltrumite);
        synchronize(player);
        player.getServer().getCommands().sendCommands(player);
        player.displayClientMessage(Component.translatable(viltrumite
                ? "message.chaosticspells.ave_race_viltrumite" : "message.chaosticspells.ave_race_human"), false);
        return 1;
    }

    public static void synchronize(ServerPlayer player) {
        boolean enabled = AveViltrumiteAccess.selected(player);
        ((ViltrumiteCorePlayer) player).setChosenRace(true);
        ((ViltrumiteCorePlayer) player).setViltrumite(enabled);
        if (!enabled) {
            clearPowers(player);
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            } else player.getAbilities().mayfly = true;
        } else player.getAbilities().mayfly = true;
        player.onUpdateAbilities();
    }

    private static void clearPowers(ServerPlayer player) {
        ViltrumiteCorePlayer core = (ViltrumiteCorePlayer) player;
        core.releaseTarget();
        core.clearHeldSubLevel();
        core.setTryingToGrab(false);
        core.setBlocking(false);
        core.setSuperSpeed(false);
        core.setBarraging(false);
        core.setChargingPunch(false);
        core.setPunchTicks(0);
        core.setChopTicks(0);
        core.setThunderclapTicks(0);
        core.setBarrageTicks(0);
        core.setChargePunchTicks(0);
        ((ViltrumiteFlightPlayer) player).stopFlight();
        for (var effect : java.util.List.copyOf(player.getActiveEffects())) {
            var key = BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
            if (key != null && AveViltrumiteAccess.isViltrumiteNamespace(key.getNamespace())) player.removeEffect(effect.getEffect());
        }
        for (var holder : BuiltInRegistries.ATTRIBUTE.holders().toList()) {
            var attribute = player.getAttribute(holder);
            if (attribute == null) continue;
            for (AttributeModifier modifier : java.util.List.copyOf(attribute.getModifiers())) {
                if (AveViltrumiteAccess.isViltrumiteNamespace(modifier.id().getNamespace())) attribute.removeModifier(modifier.id());
            }
        }
    }

    @SubscribeEvent
    public static void loadLevel(LevelEvent.Load event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
            var manager = dev.baranhan.viltrumitecore.worldevent.WorldEventManager.get(level);
            if (!manager.activeEvents().isEmpty()) manager.stopAll(level);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) synchronize(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void clone(PlayerEvent.Clone event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            boolean selected = AveViltrumiteAccess.selected(event.getOriginal());
            player.getPersistentData().putBoolean(AveViltrumiteAccess.ENABLED_TAG, selected);
            synchronize(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) synchronize(player);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) synchronize(player);
    }

    @SubscribeEvent
    public static void restrictEffects(MobEffectEvent.Applicable event) {
        var key = BuiltInRegistries.MOB_EFFECT.getKey(event.getEffectInstance().getEffect().value());
        if (key != null && AveViltrumiteAccess.isViltrumiteNamespace(key.getNamespace())
                && !AveViltrumiteAccess.isActive(event.getEntity())) event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }

    @SubscribeEvent
    public static void restrictItems(PlayerInteractEvent.RightClickItem event) {
        var key = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (AveViltrumiteAccess.isViltrumiteNamespace(key.getNamespace()) && !AveViltrumiteAccess.isActive(event.getEntity())) event.setCanceled(true);
    }

    private ViltrumiteIntegration() {}
}
