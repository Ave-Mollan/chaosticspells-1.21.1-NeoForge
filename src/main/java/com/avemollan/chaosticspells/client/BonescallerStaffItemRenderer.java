package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.client.model.BonescallerStaffItemModel;
import com.avemollan.chaosticspells.item.BonescallerStaffItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class BonescallerStaffItemRenderer extends GeoItemRenderer<BonescallerStaffItem> {
    public BonescallerStaffItemRenderer() {
        super(new BonescallerStaffItemModel());
    }

    @Override
    public RenderType getRenderType(BonescallerStaffItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
