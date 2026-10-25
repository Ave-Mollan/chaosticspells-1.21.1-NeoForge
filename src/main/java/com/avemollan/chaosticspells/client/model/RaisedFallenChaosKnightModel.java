package com.avemollan.chaosticspells.client.model;

import com.avemollan.chaosticspells.entity.RaisedFallenChaosKnight;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * Uses Born in Chaos geometry and textures (same as the original compat spell).
 */
public class RaisedFallenChaosKnightModel extends GeoModel<RaisedFallenChaosKnight> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "geo/fallenchaosknightn.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "textures/entities/fallenchaosknightn.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath("born_in_chaos_v1", "animations/fallenchaosknightn.animation.json");

    @Override
    public ResourceLocation getModelResource(RaisedFallenChaosKnight object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(RaisedFallenChaosKnight object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(RaisedFallenChaosKnight animatable) {
        return ANIMATIONS;
    }
}
