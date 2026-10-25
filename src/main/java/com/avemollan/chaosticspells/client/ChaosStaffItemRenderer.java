package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.client.model.ChaosStaffItemModel;
import com.avemollan.chaosticspells.item.ChaosStaffItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class ChaosStaffItemRenderer extends GeoItemRenderer<ChaosStaffItem> {
    public ChaosStaffItemRenderer() {
        super(new ChaosStaffItemModel());
    }

    @Override
    public RenderType getRenderType(ChaosStaffItem animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
