package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public final class OwnersWhipRenderer extends GeoItemRenderer<OwnersWhipItem> {
    public OwnersWhipRenderer(){super(new WhipPhysicsModel());}
    @Override public void actuallyRender(PoseStack pose,OwnersWhipItem item,BakedGeoModel model,RenderType type,
            MultiBufferSource buffers,VertexConsumer consumer,boolean reRender,float partial,int light,int overlay,int color) {
        var old=WhipRenderSpace.current;
        WhipRenderSpace.current=WhipRenderSpace.create(pose.last().pose(),WhipPhysicsModel.rootTransform(renderPerspective));
        try{super.actuallyRender(pose,item,model,type,buffers,consumer,reRender,partial,light,overlay,color);}
        finally{WhipRenderSpace.current=old;}
    }
}
