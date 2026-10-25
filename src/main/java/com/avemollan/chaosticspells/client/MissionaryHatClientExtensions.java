package com.avemollan.chaosticspells.client;

import java.util.Collections;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import com.avemollan.chaosticspells.client.model.MissionaryHatModel;

public enum MissionaryHatClientExtensions implements IClientItemExtensions {
    INSTANCE;

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(
            LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        var headGeom = new MissionaryHatModel(
                        Minecraft.getInstance().getEntityModels().bakeLayer(MissionaryHatModel.LAYER_LOCATION))
                .getHeadPart();
        HumanoidModel<?> armorModel = new HumanoidModel<>(new ModelPart(Collections.emptyList(), Map.of(
                "head", headGeom,
                "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                "body", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                "left_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                "right_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()),
                "left_leg", new ModelPart(Collections.emptyList(), Collections.emptyMap()))));
        armorModel.crouching = entity.isShiftKeyDown();
        armorModel.riding = original.riding;
        armorModel.young = entity.isBaby();
        return armorModel;
    }
}
