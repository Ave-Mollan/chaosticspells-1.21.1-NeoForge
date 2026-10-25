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

public class MagicDarkMetalChestplateModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "magic_dark_metal_chestplate"), "main");
    public final ModelPart Body;
    public final ModelPart RightArm;
    public final ModelPart LeftArm;

    public MagicDarkMetalChestplateModel(ModelPart root) {
        this.Body = root.getChild("Body");
        this.RightArm = root.getChild("RightArm");
        this.LeftArm = root.getChild("LeftArm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition parts = meshdefinition.getRoot();
        parts.addOrReplaceChild(
                        "Body",
                        CubeListBuilder.create()
                                .texOffs(6, 3)
                                .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new CubeDeformation(1.01f))
                                .texOffs(0, 22)
                                .addBox(-4.5f, -0.1f, -2.5f, 9.0f, 12.0f, 5.0f, new CubeDeformation(1.0f))
                                .texOffs(0, 40)
                                .addBox(-4.5f, 9.1f, -2.5f, 9.0f, 8.0f, 5.0f, new CubeDeformation(0.8f)),
                        PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition RightArm =
                parts.addOrReplaceChild(
                        "RightArm",
                        CubeListBuilder.create()
                                .texOffs(90, 0)
                                .addBox(-4.3f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(1.0f))
                                .texOffs(112, 24)
                                .addBox(-3.3f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.7f))
                                .texOffs(84, 4)
                                .addBox(-3.9f, -5.6f, 0.0f, 1.0f, 4.0f, 0.0f, new CubeDeformation(0.0f)),
                        PartPose.offset(-4.5f, 2.0f, 0.0f));
        RightArm.addOrReplaceChild(
                "RightArmArmor_r1",
                CubeListBuilder.create().texOffs(84, 4).addBox(-0.238f, -1.3282f, 0.0f, 1.0f, 4.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-6.2007f, -3.4458f, 0.0f, 0.0f, 0.0f, -0.6109f));
        PartDefinition RightArmArmor_r2 =
                RightArm.addOrReplaceChild("RightArmArmor_r2", CubeListBuilder.create(), PartPose.offsetAndRotation(-8.1f, -3.7f, 0.0f, 0.0f, 0.0f, -1.2217f));
        RightArmArmor_r2.addOrReplaceChild(
                "RightArmArmor_r2_r1",
                CubeListBuilder.create()
                        .texOffs(84, 4)
                        .addBox(-0.6125f, -1.5672f, 0.0f, 1.0f, 4.0f, 0.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-1.829f, 1.9698f, 0.0f, 0.0f, 0.0f, -0.1396f));
        PartDefinition LeftArm =
                parts.addOrReplaceChild(
                        "LeftArm",
                        CubeListBuilder.create()
                                .texOffs(90, 0)
                                .mirror()
                                .addBox(0.3f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(1.0f))
                                .mirror(false)
                                .texOffs(112, 24)
                                .mirror()
                                .addBox(-0.8f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.7f))
                                .mirror(false)
                                .texOffs(84, 4)
                                .mirror()
                                .addBox(2.9f, -5.7f, 0.0f, 1.0f, 4.0f, 0.0f, new CubeDeformation(0.0f))
                                .mirror(false),
                        PartPose.offset(4.4f, 2.0f, 0.0f));
        PartDefinition RightArmArmor_r3 =
                LeftArm.addOrReplaceChild("RightArmArmor_r3", CubeListBuilder.create(), PartPose.offsetAndRotation(8.1f, -3.7f, 0.0f, 0.0f, 0.0f, 1.2217f));
        RightArmArmor_r3.addOrReplaceChild(
                "RightArmArmor_r3_r1",
                CubeListBuilder.create()
                        .texOffs(84, 4)
                        .mirror()
                        .addBox(-0.3904f, -1.6565f, 0.0f, 1.0f, 4.0f, 0.0f, new CubeDeformation(0.0f))
                        .mirror(false),
                PartPose.offsetAndRotation(1.829f, 1.9698f, 0.0f, 0.0f, 0.0f, 0.0698f));
        LeftArm.addOrReplaceChild(
                "RightArmArmor_r4",
                CubeListBuilder.create()
                        .texOffs(84, 4)
                        .mirror()
                        .addBox(-0.8768f, -1.492f, 0.0f, 1.0f, 4.0f, 0.0f, new CubeDeformation(0.0f))
                        .mirror(false),
                PartPose.offsetAndRotation(6.2007f, -3.4458f, 0.0f, 0.0f, 0.0f, 0.6109f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
        this.Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
        this.RightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
        this.LeftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
}
