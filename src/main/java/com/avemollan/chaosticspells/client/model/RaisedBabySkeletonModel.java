package com.avemollan.chaosticspells.client.model;

import com.avemollan.chaosticspells.entity.RaisedBabySkeleton;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RaisedBabySkeletonModel extends GeoModel<RaisedBabySkeleton> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "geo/babyskeleton2.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/baby_skeleton_controlled.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "animations/babyskeleton2.animation.json");

    @Override
    public ResourceLocation getModelResource(RaisedBabySkeleton object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RaisedBabySkeleton object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RaisedBabySkeleton animatable) {
        return ANIMATIONS;
    }
}
