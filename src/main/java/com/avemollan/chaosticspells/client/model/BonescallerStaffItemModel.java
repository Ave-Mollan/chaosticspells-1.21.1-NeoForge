package com.avemollan.chaosticspells.client.model;

import com.avemollan.chaosticspells.item.BonescallerStaffItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Assets live in Born in Chaos (namespace {@code born_in_chaos_v1}), same paths as vanilla BiC staff.
 */
public final class BonescallerStaffItemModel extends GeoModel<BonescallerStaffItem> {

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "geo/bonescallerstaff.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/item/bonescallerstaff.png");
    private static final ResourceLocation ANIMATION =
            ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "animations/bonescallerstaff.animation.json");

    @Override
    public ResourceLocation getModelResource(BonescallerStaffItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(BonescallerStaffItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(BonescallerStaffItem animatable) {
        return ANIMATION;
    }
}
