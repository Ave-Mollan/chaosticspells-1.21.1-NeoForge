package com.avemollan.chaosticspells.events;

import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Save the actual whip stacks, including components, rather than issuing a new whip on every respawn. */
public final class WhipRetention {
    private static final String SAVED = "chaosticspells:retained_whips";

    private static boolean whip(ItemStack stack) {
        return stack.is(ChaosticItemRegistry.OWNERS_WHIP.get());
    }

    private static void retain(ServerPlayer player, ItemStack stack) {
        ListTag saved = player.getPersistentData().getList(SAVED, Tag.TAG_COMPOUND);
        saved.add(stack.save(player.registryAccess()));
        player.getPersistentData().put(SAVED, saved);
    }

    private static void retain(ServerPlayer player, Container container) {
        for (int i = 0; i < container.getContainerSize(); i++) if (whip(container.getItem(i))) retain(player, container.removeItemNoUpdate(i));
    }

    /** The cursor and the 2x2 grid are thrown into the world when a dead or disconnecting player's menu closes. */
    private static void retainLoose(ServerPlayer player) {
        if (whip(player.containerMenu.getCarried())) {
            retain(player, player.containerMenu.getCarried());
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
        retain(player, player.inventoryMenu.getCraftSlots());
    }

    /** Before grave/corpse mods read the inventory. A death cancelled later is undone by the periodic restore. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void death(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !OwnersWhipItem.isOwner(player)) return;
        WhipCombat.cancel(player);
        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) retain(player, player.getInventory());
        retainLoose(player);
    }

    /** HIGHEST runs before drop-capturing grave mods; LOWEST catches stacks other mods (e.g. Curios) add later. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void drops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !OwnersWhipItem.isOwner(player)) return;
        WhipCombat.cancel(player);
        event.getDrops().removeIf(drop -> {
            if (!whip(drop.getItem())) return false;
            retain(player, drop.getItem());
            return true;
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void lateDrops(LivingDropsEvent event) {
        drops(event);
    }

    /** Menus closed after death throw their contents through Player.drop, bypassing LivingDropsEvent. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void toss(ItemTossEvent event) {
        // After disconnect the player data is already saved, so a retained copy would be lost: leave that drop alone.
        if (event.getPlayer() instanceof ServerPlayer player && !player.isAlive() && !player.hasDisconnected()
                && OwnersWhipItem.isOwner(player) && whip(event.getEntity().getItem())) {
            retain(player, event.getEntity().getItem());
            event.setCanceled(true);
        }
    }

    /** Fires before the player data is saved; vanilla would otherwise drop the cursor and crafting grid on disconnect. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && OwnersWhipItem.isOwner(player)) retainLoose(player);
    }

    /** Root persistent data is not copied by NeoForge on any clone, including the End exit portal (wasDeath=false). */
    @SubscribeEvent
    public static void clonePlayer(PlayerEvent.Clone event) {
        if (!OwnersWhipItem.isOwner(event.getEntity())) return;
        var original = event.getOriginal().getPersistentData();
        if (original.contains(SAVED, Tag.TAG_LIST)) {
            event.getEntity().getPersistentData().put(SAVED, original.getList(SAVED, Tag.TAG_COMPOUND).copy());
            original.remove(SAVED);
        }
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player);
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) restore(player);
    }

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) restore(player);
    }

    public static void restore(ServerPlayer player) {
        if (!player.isAlive() || !OwnersWhipItem.isOwner(player) || !player.getPersistentData().contains(SAVED)) return;
        ListTag pending = new ListTag();
        var inventory = player.getInventory();
        boolean added = false;
        for (Tag tag : player.getPersistentData().getList(SAVED, Tag.TAG_COMPOUND)) {
            ItemStack stack = ItemStack.parse(player.registryAccess(), tag).orElse(ItemStack.EMPTY);
            if (!whip(stack)) continue;
            // A full inventory must never cause a protected item to be dropped or lost. Inventory.add
            // voids a stack it cannot place for creative players, so only add into a real free slot.
            if (inventory.getFreeSlot() >= 0) added |= inventory.add(stack);
            if (!stack.isEmpty()) pending.add(stack.save(player.registryAccess()));
        }
        if (pending.isEmpty()) player.getPersistentData().remove(SAVED);
        else player.getPersistentData().put(SAVED, pending);
        if (added) player.inventoryMenu.broadcastChanges();
    }

    private WhipRetention() {}
}
