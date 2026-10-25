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

public class MagicDarkMetalHelmetModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "magic_dark_metal_helmet"), "main");
    public final ModelPart Head;

    public MagicDarkMetalHelmetModel(ModelPart root) {
        this.Head = root.getChild("Head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition Head =
                partdefinition.addOrReplaceChild(
                        "Head",
                        CubeListBuilder.create()
                                .texOffs(0, 112)
                                .addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(1.0f))
                                .texOffs(33, 112)
                                .addBox(-4.0f, -7.9f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.6f)),
                        PartPose.offset(0.0f, 0.0f, 0.0f));
        Head.addOrReplaceChild(
                "Helmet_r1",
                CubeListBuilder.create()
                        .texOffs(26, 98)
                        .mirror()
                        .addBox(0.8f, -3.15f, -0.2f, 2.0f, 3.0f, 4.0f, new CubeDeformation(0.0f))
                        .mirror(false)
                        .texOffs(44, 105)
                        .mirror()
                        .addBox(-1.2f, -3.15f, 2.8f, 4.0f, 3.0f, 1.0f, new CubeDeformation(0.0f))
                        .mirror(false)
                        .texOffs(0, 94)
                        .mirror()
                        .addBox(0.8f, -3.15f, -3.2f, 2.0f, 7.0f, 3.0f, new CubeDeformation(0.0f))
                        .mirror(false),
                PartPose.offsetAndRotation(-6.2746f, -8.4704f, -0.0786f, -2.7592f, 0.4708f, 1.9927f));
        Head.addOrReplaceChild(
                "Helmet_r2",
                CubeListBuilder.create()
                        .texOffs(26, 98)
                        .addBox(-2.8f, -3.15f, -0.2f, 2.0f, 3.0f, 4.0f, new CubeDeformation(0.0f))
                        .texOffs(0, 94)
                        .addBox(-2.8f, -3.15f, -3.2f, 2.0f, 7.0f, 3.0f, new CubeDeformation(0.0f))
                        .texOffs(44, 105)
                        .addBox(-2.8f, -3.15f, 2.8f, 4.0f, 3.0f, 1.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(6.2746f, -8.4704f, -0.0786f, -2.7592f, -0.4708f, -1.9927f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
}
