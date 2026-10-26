package com.avemollan.chaosticspells.compat.viltrumite;

import com.avemollan.chaosticspells.mixin.compat.viltrumite.CreativeTabsAccess;
import dev.baranhan.viltrumitecore.client.TargetLockManager;
import dev.baranhan.viltrumitecore.client.ViltrumiteCoreClient;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/** Client presentation only. Server-side checks remain authoritative. */
public final class ViltrumiteClientPolicy {
    private static Boolean lastActive;
    private static KeyMapping[] modKeys;

    public static void install() {
        NeoForge.EVENT_BUS.register(ViltrumiteClientPolicy.class);
    }

    public static boolean active() {
        return AveViltrumiteAccess.isActive(Minecraft.getInstance().player);
    }

    public static boolean mayOpenScreen(String className, boolean active) {
        return AveViltrumiteAccess.allowScreen(className, active);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void screen(ScreenEvent.Opening event) {
        if (event.getNewScreen() != null && !mayOpenScreen(event.getNewScreen().getClass().getName(), active())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void clientCommands(RegisterClientCommandsEvent event) {
        for (String name : new String[]{"spawnclone", "firstpersontool", "thirdpersontool"}) {
            ViltrumiteIntegration.restrictNativeCommand(event.getDispatcher().getRoot().getChild(name));
        }
    }

    private static boolean modKey(KeyMapping key) {
        return key.getName().startsWith("key.viltrumitecore.") || key.getName().startsWith("key.viltrumiteflight.");
    }

    public static KeyMapping[] visibleKeys(KeyMapping[] keys) {
        return active() ? keys : Arrays.stream(keys).filter(key -> !modKey(key)).toArray(KeyMapping[]::new);
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.options == null) return;
        boolean enabled = active();
        if (!enabled) {
            // Key registration is finished long before the first client tick.
            if (modKeys == null) modKeys = Arrays.stream(client.options.keyMappings).filter(ViltrumiteClientPolicy::modKey).toArray(KeyMapping[]::new);
            for (KeyMapping key : modKeys) while (key.consumeClick()) { /* Discard stale ability presses before activation. */ }
        }
        if (lastActive == null || lastActive != enabled) {
            CreativeTabsAccess.chaosticspells$invalidate(null);
            if (client.level != null) CreativeModeTabs.tryRebuildTabContents(client.level.enabledFeatures(),
                    client.player != null && client.player.canUseGameMasterBlocks(), client.level.registryAccess());
            if (!enabled && Boolean.TRUE.equals(lastActive)) {
                TargetLockManager.lockedTarget = null;
                if (ViltrumiteCoreClient.preChargePerspective != null) {
                    client.options.setCameraType(ViltrumiteCoreClient.preChargePerspective);
                    ViltrumiteCoreClient.preChargePerspective = null;
                }
                if (client.screen != null && !mayOpenScreen(client.screen.getClass().getName(), false)) client.setScreen(null);
            }
            lastActive = enabled;
        }
    }

    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (active()) return;
        for (ItemStack stack : List.copyOf(event.getParentEntries())) {
            if (isModItem(stack)) event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
        for (ItemStack stack : List.copyOf(event.getSearchEntries())) {
            if (isModItem(stack)) event.remove(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private static boolean isModItem(ItemStack stack) {
        if (AveViltrumiteAccess.isViltrumiteNamespace(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace())) return true;
        var contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.potion().map(holder -> holder.unwrapKey()
                .map(key -> AveViltrumiteAccess.isViltrumiteNamespace(key.location().getNamespace())).orElse(false)).orElse(false);
    }

    private ViltrumiteClientPolicy() {}
}
