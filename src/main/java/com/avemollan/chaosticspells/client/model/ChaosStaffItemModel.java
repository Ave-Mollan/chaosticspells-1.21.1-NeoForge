package com.avemollan.chaosticspells.client.model;

import com.avemollan.chaosticspells.item.ChaosStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Same model as Born in Chaos "staff of magic arrows" ({@code staffmagicarrows} in {@code born_in_chaos_v1}).
 */
public final class ChaosStaffItemModel extends GeoModel<ChaosStaffItem> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "geo/staffmagicarrows.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/item/staffmagicarrows.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "animations/staffmagicarrows.animation.json");

    @Override
    public ResourceLocation getModelResource(ChaosStaffItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(ChaosStaffItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(ChaosStaffItem animatable) {
        return ANIMATION;
    }
}
