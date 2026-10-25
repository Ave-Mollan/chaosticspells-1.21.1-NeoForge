package com.avemollan.chaosticspells.client;

import java.util.Collections;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import com.avemollan.chaosticspells.client.model.CrownedMagicDarkMetalHelmetModel;
import com.avemollan.chaosticspells.client.model.MagicDarkMetalBootModel;
import com.avemollan.chaosticspells.client.model.MagicDarkMetalChestplateModel;
import com.avemollan.chaosticspells.client.model.MagicDarkMetalHelmetModel;
import com.avemollan.chaosticspells.client.model.MagicDarkMetalLeggingsModel;

public enum MagicDarkMetalArmorClientExtensions implements IClientItemExtensions {
    HELMET(MagicDarkMetalHelmetModel.LAYER_LOCATION, ArmorPiece.HELMET),
    CROWNED_HELMET(CrownedMagicDarkMetalHelmetModel.LAYER_LOCATION, ArmorPiece.CROWNED_HELMET),
    CHESTPLATE(MagicDarkMetalChestplateModel.LAYER_LOCATION, ArmorPiece.CHESTPLATE),
    LEGGINGS(MagicDarkMetalLeggingsModel.LAYER_LOCATION, ArmorPiece.LEGGINGS),
    BOOTS(MagicDarkMetalBootModel.LAYER_LOCATION, ArmorPiece.BOOTS);

    private final ModelLayerLocation layer;
    private final ArmorPiece piece;

    MagicDarkMetalArmorClientExtensions(ModelLayerLocation layer, ArmorPiece piece) {
        this.layer = layer;
        this.piece = piece;
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        ModelPart baked = Minecraft.getInstance().getEntityModels().bakeLayer(layer);
        HumanoidModel<?> armorModel =
                switch (piece) {
                    case HELMET -> {
                        var geo = new MagicDarkMetalHelmetModel<>(baked);
                        yield new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                                "head", geo.Head,
                                "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    }
                    case CROWNED_HELMET -> {
                        var geo = new CrownedMagicDarkMetalHelmetModel<>(baked);
                        yield new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                                "head", geo.Head,
                                "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    }
                    case CHESTPLATE -> {
                        var geo = new MagicDarkMetalChestplateModel<>(baked);
                        yield new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                                "body", geo.Body,
                                "left_arm", geo.LeftArm,
                                "right_arm", geo.RightArm,
                                "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    }
                    case LEGGINGS -> {
                        var geo = new MagicDarkMetalLeggingsModel<>(baked);
                        yield new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                                "left_leg", geo.LeftLeg,
                                "right_leg", geo.RightLeg,
                                "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    }
                    case BOOTS -> {
                        var geo = new MagicDarkMetalBootModel<>(baked);
                        yield new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                                "left_leg", geo.LeftLeg,
                                "right_leg", geo.RightLeg,
                                "head", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                                "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
                    }
                };
        armorModel.crouching = entity.isShiftKeyDown();
        armorModel.riding = original.riding;
        armorModel.young = entity.isBaby();
        return armorModel;
    }

    private enum ArmorPiece {
        HELMET,
        CROWNED_HELMET,
        CHESTPLATE,
        LEGGINGS,
        BOOTS
    }
}
