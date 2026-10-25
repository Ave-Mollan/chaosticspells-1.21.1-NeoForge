package com.avemollan.chaosticspells.client.model;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

/**
 * Head geometry ported from Born in Chaos {@code Modelmissionaryhat}; atlas is {@link com.avemollan.chaosticspells.item.MissionaryHatHelmetItem} armor texture path.
 */
public final class MissionaryHatModel extends EntityModel<Entity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID, "missionary_hat_model"),
            "main");

    private final ModelPart head;

    public MissionaryHatModel(ModelPart root) {
        this.head = root.getChild("Head");
    }

    public ModelPart getHeadPart() {
        return head;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition root = meshdefinition.getRoot();
        root.addOrReplaceChild(
                "Head",
                CubeListBuilder.create()
                        .texOffs(9, 83)
                        .addBox(-5.5F, -9.7F, -5.5F, 11.0F, 5.0F, 11.0F, new CubeDeformation(-0.5F))
                        .texOffs(5, 109)
                        .addBox(-8.0F, -5.6F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(-0.4F)),
                PartPose.offset(0.0F, 2.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int rgb) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, rgb);
    }

    @Override
    public void setupAnim(
            Entity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
    }
}
