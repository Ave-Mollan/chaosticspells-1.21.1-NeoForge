package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.entity.RaisedBabySkeleton;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@OnlyIn(Dist.CLIENT)
public class RaisedBabySkeletonEyesLayer extends GeoRenderLayer<RaisedBabySkeleton> {
    private static final ResourceLocation EYES_TEXTURE = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/babyskeleton_e.png");

    public RaisedBabySkeletonEyesLayer(GeoEntityRenderer<RaisedBabySkeleton> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, RaisedBabySkeleton animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType eyes = RenderType.eyes(EYES_TEXTURE);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(eyes);
        this.getRenderer().actuallyRender(poseStack, animatable, bakedModel, eyes, bufferSource, vertexConsumer, true, partialTick,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
    }
}
