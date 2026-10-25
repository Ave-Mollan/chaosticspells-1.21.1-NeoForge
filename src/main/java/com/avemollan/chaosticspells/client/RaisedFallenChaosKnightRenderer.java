package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.client.model.RaisedFallenChaosKnightModel;
import com.avemollan.chaosticspells.entity.RaisedFallenChaosKnight;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RaisedFallenChaosKnightRenderer extends GeoEntityRenderer<RaisedFallenChaosKnight> {
    public RaisedFallenChaosKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new RaisedFallenChaosKnightModel());
        this.shadowRadius = 0.7f;
    }
}
