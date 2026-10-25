package com.avemollan.chaosticspells.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.avemollan.chaosticspells.ChaosticSpells;

public class MagicDarkMetalBootModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "magic_dark_metal_boot"), "main");
    public final ModelPart RightLeg;
    public final ModelPart LeftLeg;

    public MagicDarkMetalBootModel(ModelPart root) {
        this.RightLeg = root.getChild("RightLeg");
        this.LeftLeg = root.getChild("LeftLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
                "RightLeg",
                CubeListBuilder.create().texOffs(107, 0).addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(1.0f)),
                PartPose.offset(-1.9f, 12.0f, 0.0f));
        partdefinition.addOrReplaceChild(
                "LeftLeg",
                CubeListBuilder.create()
                        .texOffs(107, 0)
                        .mirror()
                        .addBox(-2.0f, 0.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(1.0f))
                        .mirror(false),
                PartPose.offset(1.9f, 12.0f, 0.0f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
        this.RightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
        this.LeftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
}
