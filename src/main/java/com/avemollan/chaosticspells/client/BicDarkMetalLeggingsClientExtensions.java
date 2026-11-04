package com.avemollan.chaosticspells.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import com.avemollan.chaosticspells.ChaosticSpells;

/**
 * Born in Chaos renders Dark Metal leggings with the shared vanilla inner-armor model, which a resource pack
 * cannot reshape for one item only. This gives them their own layer whose default geometry is exactly the vanilla
 * inner-armor mesh (same look without a pack), so an EMF pack can replace it via
 * {@code assets/chaosticspells/optifine/cem/bic_dark_metal_leggings.jem}.
 */
public final class BicDarkMetalLeggingsClientExtensions implements IClientItemExtensions {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "bic_dark_metal_leggings"), "main");
    private static final ResourceLocation ITEM_ID =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "dark_metal_armor_leggings");

    private BicDarkMetalLeggingsClientExtensions() {}

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(0.5f), 0.0f), 64, 32);
    }

    /** Registers only if Born in Chaos is present and has no client extension of its own for the item. */
    public static void register(RegisterClientExtensionsEvent event) {
        BuiltInRegistries.ITEM.getOptional(ITEM_ID)
                .filter(item -> IClientItemExtensions.of(item) == IClientItemExtensions.DEFAULT)
                .ifPresent(item -> event.registerItem(new BicDarkMetalLeggingsClientExtensions(), item));
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        return new HumanoidModel<LivingEntity>(ArmorModelCache.bake(LAYER_LOCATION));
    }
}
