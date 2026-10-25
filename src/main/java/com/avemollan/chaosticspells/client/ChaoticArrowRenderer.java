package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.entity.ChaoticArrowProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class ChaoticArrowRenderer extends EntityRenderer<ChaoticArrowProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "textures/entity/magic_arrow.png");

    public ChaoticArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ChaoticArrowProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();

        Vec3 motion = entity.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        renderModel(poseStack, bufferSource, light);
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    public static void renderModel(PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.scale(0.13f, 0.13f, 0.13f);

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.translate(-2, 0, 0);

        for (int j = 0; j < 4; ++j) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            Matrix4f poseMatrix = poseStack.last().pose();
            vertex(poseMatrix, consumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, light);
            vertex(poseMatrix, consumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, light);
            vertex(poseMatrix, consumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, light);
            vertex(poseMatrix, consumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, light);
        }
    }

    public static void vertex(Matrix4f matrix, VertexConsumer consumer, int ox, int oy, int oz, float u, float v, int nx, int ny, int nz, int packedLight) {
        consumer.addVertex(matrix, (float) ox, (float) oy, (float) oz)
                .setColor(200, 200, 200, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal((float) nx, (float) nz, (float) ny);
    }

    @Override
    public ResourceLocation getTextureLocation(ChaoticArrowProjectile entity) {
        return TEXTURE;
    }
}
