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

public class MagicNightmareChestplateModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "magic_nightmare_chestplate"), "main");
    public final ModelPart Body;
    public final ModelPart RightArm;
    public final ModelPart LeftArm;

    public MagicNightmareChestplateModel(ModelPart root) {
        this.Body = root.getChild("Body");
        this.RightArm = root.getChild("RightArm");
        this.LeftArm = root.getChild("LeftArm");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild(
                "Body",
                CubeListBuilder.create()
                        .texOffs(0, 1)
                        .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 18.0f, 4.0f, new CubeDeformation(0.9f))
                        .texOffs(26, 26)
                        .addBox(-4.0f, 0.0f, -2.2f, 8.0f, 18.0f, 4.0f, new CubeDeformation(1.0f))
                        .texOffs(29, 0)
                        .addBox(-4.0f, 0.0f, -2.0f, 8.0f, 14.0f, 4.0f, new CubeDeformation(0.8f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));
        partdefinition.addOrReplaceChild(
                "RightArm",
                CubeListBuilder.create()
                        .texOffs(112, 1)
                        .addBox(-3.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.8f))
                        .texOffs(90, 28)
                        .addBox(-3.1f, -2.1f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(1.0f))
                        .texOffs(91, 0)
                        .addBox(-3.0f, -1.1f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.6f))
                        .texOffs(70, 26)
                        .addBox(-3.1f, -1.8f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(1.1f)),
                PartPose.offset(-5.0f, 2.0f, 0.0f));
        partdefinition.addOrReplaceChild(
                "LeftArm",
                CubeListBuilder.create()
                        .texOffs(112, 1)
                        .mirror()
                        .addBox(-1.0f, -2.0f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.8f))
                        .mirror(false)
                        .texOffs(91, 0)
                        .mirror()
                        .addBox(-1.0f, -1.1f, -2.0f, 4.0f, 12.0f, 4.0f, new CubeDeformation(0.6f))
                        .mirror(false)
                        .texOffs(90, 28)
                        .mirror()
                        .addBox(-0.9f, -2.1f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(1.0f))
                        .mirror(false)
                        .texOffs(70, 26)
                        .mirror()
                        .addBox(-0.9f, -1.8f, -2.0f, 4.0f, 4.0f, 4.0f, new CubeDeformation(1.1f))
                        .mirror(false),
                PartPose.offset(5.0f, 2.0f, 0.0f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            int rgb) {
        this.Body.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
        this.RightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
        this.LeftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
}
