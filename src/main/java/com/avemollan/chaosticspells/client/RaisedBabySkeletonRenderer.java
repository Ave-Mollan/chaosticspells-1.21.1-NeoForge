package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.client.model.RaisedBabySkeletonModel;
import com.avemollan.chaosticspells.entity.RaisedBabySkeleton;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class RaisedBabySkeletonRenderer extends GeoEntityRenderer<RaisedBabySkeleton> {
    public RaisedBabySkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, new RaisedBabySkeletonModel());
        this.shadowRadius = 0.2f;
        this.addRenderLayer(new RaisedBabySkeletonEyesLayer(this));
    }
}
