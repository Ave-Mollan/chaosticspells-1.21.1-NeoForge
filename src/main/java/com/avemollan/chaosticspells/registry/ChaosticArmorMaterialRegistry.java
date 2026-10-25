package com.avemollan.chaosticspells.registry;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.avemollan.chaosticspells.ChaosticSpells;

import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Armor materials for ChaoticSpells gear.
 *
 * <p>{@link ArmorMaterial.Layer} uses vanilla asset ids ({@code minecraft:leather}, {@code minecraft:netherite}) so
 * smithing trim picks up existing color palettes under {@code minecraft:textures/trims/color_palettes/}. In-world
 * armor appearance still comes from each item's {@code getArmorTexture} (custom models in client extensions).
 */
public final class ChaosticArmorMaterialRegistry {
    private static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ChaosticSpells.MODID);

    private ChaosticArmorMaterialRegistry() {
    }

    public static void register(IEventBus eventBus) {
        ARMOR_MATERIALS.register(eventBus);
    }

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MISSIONARY_HAT = registerMissionaryHat();

    /** Matches legacy compat stats (defense/toughness/enchant like nightmare mantle line). */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MAGIC_NIGHTMARE = registerMagicNightmare();

    /** Legacy compat Magic Dark Metal (netherite-tier stats). */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MAGIC_DARK_METAL = registerMagicDarkMetal();

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> registerMissionaryHat() {
        return ARMOR_MATERIALS.register(
                "missionary_hat",
                () -> new ArmorMaterial(
                        missionaryHatDefense(),
                        18,
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        missionaryRepairIngredient(),
                        List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("leather"))),
                        0.5f,
                        0.0f));
    }

    private static EnumMap<ArmorItem.Type, Integer> missionaryHatDefense() {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 0);
            map.put(ArmorItem.Type.LEGGINGS, 0);
            map.put(ArmorItem.Type.CHESTPLATE, 0);
            map.put(ArmorItem.Type.HELMET, 3);
            map.put(ArmorItem.Type.BODY, 0);
        });
    }

    private static Supplier<Ingredient> missionaryRepairIngredient() {
        return () -> BuiltInRegistries.ITEM
                .getHolder(ResourceKey.create(
                        Registries.ITEM,
                        ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "monster_skin")))
                .filter(net.minecraft.core.Holder.Reference::isBound)
                .map(h -> Ingredient.of(h.value()))
                .orElseGet(() -> Ingredient.of(Items.LEATHER));
    }

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> registerMagicNightmare() {
        return ARMOR_MATERIALS.register(
                "magic_nightmare",
                () -> new ArmorMaterial(
                        magicNightmareDefense(),
                        15,
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        () -> Ingredient.of(Items.LEATHER),
                        List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("leather"))),
                        1.0f,
                        0.0f));
    }

    private static EnumMap<ArmorItem.Type, Integer> magicNightmareDefense() {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 2);
            map.put(ArmorItem.Type.LEGGINGS, 6);
            map.put(ArmorItem.Type.CHESTPLATE, 7);
            map.put(ArmorItem.Type.HELMET, 4);
            map.put(ArmorItem.Type.BODY, 0);
        });
    }

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> registerMagicDarkMetal() {
        return ARMOR_MATERIALS.register(
                "magic_dark_metal",
                () -> new ArmorMaterial(
                        magicDarkMetalDefense(),
                        20,
                        SoundEvents.ARMOR_EQUIP_NETHERITE,
                        () -> Ingredient.of(Items.NETHERITE_INGOT),
                        List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("netherite"))),
                        5.0f,
                        0.1f));
    }

    private static EnumMap<ArmorItem.Type, Integer> magicDarkMetalDefense() {
        return Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
            map.put(ArmorItem.Type.BOOTS, 3);
            map.put(ArmorItem.Type.LEGGINGS, 7);
            map.put(ArmorItem.Type.CHESTPLATE, 10);
            map.put(ArmorItem.Type.HELMET, 4);
            map.put(ArmorItem.Type.BODY, 0);
        });
    }
}
