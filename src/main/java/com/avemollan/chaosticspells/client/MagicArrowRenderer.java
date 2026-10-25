package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.entity.MagicArrowProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MagicArrowRenderer extends EntityRenderer<MagicArrowProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "textures/entity/magic_arrow.png");

    public MagicArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MagicArrowProjectile entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicArrowProjectile entity) {
        return TEXTURE;
    }
}
