package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.physics.WhipRope;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

/** Fixed fist grip; world snapshots are projected through the actual hand's render matrix. */
public final class WhipPhysicsModel extends GeoModel<OwnersWhipItem> {
    @Override public ResourceLocation getModelResource(OwnersWhipItem i){return id("geo/owners_whip.geo.json");}
    @Override public ResourceLocation getTextureResource(OwnersWhipItem i){return id("textures/item/owners_whip.png");}
    @Override public ResourceLocation getAnimationResource(OwnersWhipItem i){return id("animations/owners_whip.animation.json");}
    public static Matrix4f rootTransform(ItemDisplayContext context) {
        boolean third=context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        return third?new Matrix4f().translation(0,-.25f,-.07f).rotateY((float)Math.PI):new Matrix4f();
    }
    @Override public void setCustomAnimations(OwnersWhipItem item,long instance,AnimationState<OwnersWhipItem> state) {
        var context=state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE);
        var motion=WhipClientMotion.forStack(state.getData(DataTickets.ITEMSTACK),context);
        double partial=state.getPartialTick();boolean packed=motion==null || !motion.current.loose();
        boolean third=context==ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        var root=getAnimationProcessor().getBone("root");
        if(root!=null){root.setRotX(0);root.setRotY(third?(float)Math.PI:0);root.setRotZ(0);root.setPosX(0);root.setPosY(third?-4:0);root.setPosZ(third?-1.12f:0);}
        var handle=getAnimationProcessor().getBone("handle");
        if(handle!=null){handle.updatePosition(0,0,0);handle.updateRotation(0,0,0);}
        var bundle=getAnimationProcessor().getBone("packed_coil");
        if(bundle!=null){bundle.setHidden(!packed);bundle.updatePosition(0,0,0);bundle.updateRotation(0,0,0);bundle.setScaleX(1);bundle.setScaleY(1);bundle.setScaleZ(1);}
        var space=WhipRenderSpace.current;
        Vec3[] points=null;
        if(!packed && space!=null) {
            points=new Vec3[25];
            Vec3 anchor=space.world(0,.5f,0),correction=anchor.subtract(motion.point(0,partial));
            for(int i=0;i<25;i++) {
                Vec3 world=i==0?anchor:i==1?space.world(0,(float)(.5+WhipRope.length(0)),0):
                        motion.point(i,partial).add(correction.scale(Math.exp(-.35*(i-1)*(i-1))));
                points[i]=space.local(world);
            }
        }
        for(int i=0;i<24;i++) {
            var bone=getAnimationProcessor().getBone("lash_"+(i+1));if(bone==null)continue;
            bone.setHidden(points==null);if(points==null)continue;
            Vec3 p=points[i],d=points[i+1].subtract(p);
            bone.updatePosition((float)-p.x*16,(float)p.y*16,(float)p.z*16);
            bone.updateRotation((float)Math.atan2(d.z,Math.hypot(d.x,d.y)),0,(float)-Math.atan2(d.x,d.y));
            bone.setScaleX(1);bone.setScaleY((float)(d.length()/WhipRope.length(i)));bone.setScaleZ(1);
        }
    }
    private static ResourceLocation id(String p){return ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID,p);}
}
