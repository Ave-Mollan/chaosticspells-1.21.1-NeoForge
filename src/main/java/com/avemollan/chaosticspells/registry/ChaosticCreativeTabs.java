package com.avemollan.chaosticspells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class ChaosticCreativeTabs {

    private static final ResourceKey<CreativeModeTab> IRONS_SPELL_EQUIPMENT = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "spellbook_equipment"));

    private ChaosticCreativeTabs() {
    }

    public static void onBuildCreativeModeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(IRONS_SPELL_EQUIPMENT)) {
            event.accept(ChaosticItemRegistry.BONESCALLER_STAFF.get());
            event.accept(ChaosticItemRegistry.CHAOS_STAFF.get());
            event.accept(ChaosticItemRegistry.CHAOS_TRANSFORMATION_AMULET.get());
            event.accept(ChaosticItemRegistry.MISSIONARY_HAT_HELMET.get());
            event.accept(ChaosticItemRegistry.MAGIC_NIGHTMARE_HELMET.get());
            event.accept(ChaosticItemRegistry.MAGIC_NIGHTMARE_CHESTPLATE.get());
            event.accept(ChaosticItemRegistry.MAGIC_NIGHTMARE_LEGGINGS.get());
            event.accept(ChaosticItemRegistry.MAGIC_NIGHTMARE_BOOTS.get());
            event.accept(ChaosticItemRegistry.MAGIC_DARK_METAL_HELMET.get());
            event.accept(ChaosticItemRegistry.CROWNED_MAGIC_DARK_METAL_HELMET.get());
            event.accept(ChaosticItemRegistry.MAGIC_DARK_METAL_CHESTPLATE.get());
            event.accept(ChaosticItemRegistry.MAGIC_DARK_METAL_LEGGINGS.get());
            event.accept(ChaosticItemRegistry.MAGIC_DARK_METAL_BOOTS.get());
            event.accept(ChaosticItemRegistry.CHAOS_RUNE.get());
            event.accept(ChaosticItemRegistry.CHAOS_UPGRADE_ORB.get());
        }
    }
}
