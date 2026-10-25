package com.avemollan.chaosticspells.registry;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.registry.ChaosticArmorMaterialRegistry;
import com.avemollan.chaosticspells.item.BonescallerStaffItem;
import com.avemollan.chaosticspells.item.ChaosStaffItem;
import com.avemollan.chaosticspells.item.MagicDarkMetalArmorItem;
import com.avemollan.chaosticspells.item.MagicNightmareArmorItem;
import com.avemollan.chaosticspells.item.MissionaryHatHelmetItem;
import com.avemollan.chaosticspells.item.curio.ChaosTransformationAmulet;

import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChaosticItemRegistry {
    /** Irons datapack registry {@code irons_spellbooks:upgrade_orb_type}; builtin entry at {@code data/irons_spellbooks/.../chaos_power.json}. */
    public static final ResourceKey<UpgradeOrbType> IRONS_UPGRADE_ORB_CHAOS_POWER =
            ResourceKey.create(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "chaos_power"));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ChaosticSpells.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        eventBus.addListener(ChaosticCreativeTabs::onBuildCreativeModeTab);
    }

    public static final DeferredHolder<Item, BonescallerStaffItem> BONESCALLER_STAFF =
            ITEMS.register("bonescaller_staff", BonescallerStaffItem::new);

    public static final DeferredHolder<Item, ChaosStaffItem> CHAOS_STAFF =
            ITEMS.register("chaos_staff", ChaosStaffItem::new);

    public static final DeferredHolder<Item, ChaosTransformationAmulet> CHAOS_TRANSFORMATION_AMULET =
            ITEMS.register("chaos_transformation_amulet", ChaosTransformationAmulet::new);

    public static final DeferredHolder<Item, MissionaryHatHelmetItem> MISSIONARY_HAT_HELMET =
            ITEMS.register("missionary_hat_helmet",
                    () -> new MissionaryHatHelmetItem(ChaosticArmorMaterialRegistry.MISSIONARY_HAT,
                            new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(40))));

    public static final DeferredHolder<Item, MagicNightmareArmorItem.Helmet> MAGIC_NIGHTMARE_HELMET =
            ITEMS.register("magic_nightmare_helmet", MagicNightmareArmorItem.Helmet::new);

    public static final DeferredHolder<Item, MagicNightmareArmorItem.Chestplate> MAGIC_NIGHTMARE_CHESTPLATE =
            ITEMS.register("magic_nightmare_chestplate", MagicNightmareArmorItem.Chestplate::new);

    public static final DeferredHolder<Item, MagicNightmareArmorItem.Leggings> MAGIC_NIGHTMARE_LEGGINGS =
            ITEMS.register("magic_nightmare_leggings", MagicNightmareArmorItem.Leggings::new);

    public static final DeferredHolder<Item, MagicNightmareArmorItem.Boots> MAGIC_NIGHTMARE_BOOTS =
            ITEMS.register("magic_nightmare_boots", MagicNightmareArmorItem.Boots::new);

    public static final DeferredHolder<Item, MagicDarkMetalArmorItem.Helmet> MAGIC_DARK_METAL_HELMET =
            ITEMS.register("magic_dark_metal_helmet", MagicDarkMetalArmorItem.Helmet::new);

    public static final DeferredHolder<Item, MagicDarkMetalArmorItem.CrownedHelmet> CROWNED_MAGIC_DARK_METAL_HELMET =
            ITEMS.register("crowned_magic_dark_metal_helmet", MagicDarkMetalArmorItem.CrownedHelmet::new);

    public static final DeferredHolder<Item, MagicDarkMetalArmorItem.Chestplate> MAGIC_DARK_METAL_CHESTPLATE =
            ITEMS.register("magic_dark_metal_chestplate", MagicDarkMetalArmorItem.Chestplate::new);

    public static final DeferredHolder<Item, MagicDarkMetalArmorItem.Leggings> MAGIC_DARK_METAL_LEGGINGS =
            ITEMS.register("magic_dark_metal_leggings", MagicDarkMetalArmorItem.Leggings::new);

    public static final DeferredHolder<Item, MagicDarkMetalArmorItem.Boots> MAGIC_DARK_METAL_BOOTS =
            ITEMS.register("magic_dark_metal_boots", MagicDarkMetalArmorItem.Boots::new);

    public static final DeferredHolder<Item, Item> CHAOS_RUNE =
            ITEMS.register("chaos_rune", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> CHAOS_UPGRADE_ORB = ITEMS.register("chaos_upgrade_orb", () -> new UpgradeOrbItem(
            new Item.Properties()
                    .rarity(Rarity.UNCOMMON)
                    .component(ComponentRegistry.UPGRADE_ORB_TYPE, IRONS_UPGRADE_ORB_CHAOS_POWER)));

    private ChaosticItemRegistry() {
    }
}
