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

public class MagicNightmareHelmetModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "magic_nightmare_helmet"), "main");
    public final ModelPart Head;

    public MagicNightmareHelmetModel(ModelPart root) {
        this.Head = root.getChild("Head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition Head = partdefinition.addOrReplaceChild(
                "Head",
                CubeListBuilder.create()
                        .texOffs(0, 112)
                        .addBox(-4.0f, -7.3f, -4.0f, 8.0f, 7.0f, 8.0f, new CubeDeformation(0.8f))
                        .texOffs(0, 93)
                        .addBox(-4.0f, -7.3f, -4.0f, 8.0f, 7.1f, 8.0f, new CubeDeformation(0.6f))
                        .texOffs(33, 108)
                        .addBox(-4.5f, -8.3f, -4.6f, 9.0f, 11.0f, 9.0f, new CubeDeformation(0.6f)),
                PartPose.offset(0.0f, 0.0f, 0.0f));
        Head.addOrReplaceChild(
                "cube_r1",
                CubeListBuilder.create()
                        .texOffs(11, 55)
                        .mirror()
                        .addBox(-1.5f, 1.6f, 4.3f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f))
                        .mirror(false)
                        .texOffs(39, 58)
                        .mirror()
                        .addBox(-1.5f, 0.6f, 5.3f, 3.0f, 2.0f, 1.0f, new CubeDeformation(0.0f))
                        .mirror(false)
                        .texOffs(33, 65)
                        .mirror()
                        .addBox(-1.5f, 0.6f, -5.7f, 3.0f, 1.0f, 6.0f, new CubeDeformation(0.0f))
                        .mirror(false)
                        .texOffs(0, 59)
                        .mirror()
                        .addBox(-1.5f, -1.4f, -5.7f, 3.0f, 2.0f, 12.0f, new CubeDeformation(0.0f))
                        .mirror(false)
                        .texOffs(33, 65)
                        .addBox(7.3f, 0.6f, -5.7f, 3.0f, 1.0f, 6.0f, new CubeDeformation(0.0f))
                        .texOffs(11, 55)
                        .addBox(7.3f, 1.6f, 4.3f, 3.0f, 1.0f, 1.0f, new CubeDeformation(0.0f))
                        .texOffs(39, 58)
                        .addBox(7.3f, 0.6f, 5.3f, 3.0f, 2.0f, 1.0f, new CubeDeformation(0.0f))
                        .texOffs(0, 59)
                        .addBox(7.3f, -1.4f, -5.7f, 3.0f, 2.0f, 12.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-4.4f, -9.0f, 5.4f, 0.3927f, 0.0f, 0.0f));
        Head.addOrReplaceChild(
                "Helmet_r1",
                CubeListBuilder.create()
                        .texOffs(32, 76)
                        .addBox(-2.45f, -3.5313f, -6.1322f, 5.0f, 5.0f, 6.0f, new CubeDeformation(-0.2f))
                        .texOffs(32, 90)
                        .addBox(-2.45f, 1.4687f, -6.1322f, 5.0f, 2.0f, 7.0f, new CubeDeformation(0.0f))
                        .texOffs(1, 75)
                        .addBox(-2.45f, -3.5313f, -6.1322f, 5.0f, 5.0f, 7.0f, new CubeDeformation(0.0f)),
                PartPose.offsetAndRotation(-0.05f, -1.1061f, -4.1657f, 0.0349f, 0.0f, 0.0f));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            int rgb) {
        this.Head.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
}
