package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.events.WhipCombat;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.network.WhipMotionPayload;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * The arm keeps vanilla's pose and walk swing until a stroke; then it blends into the shared choreography,
 * the torso twists and leans with it, and the head and legs are counter-rotated so only the upper body turns.
 */
@EventBusSubscriber(modid=ChaosticSpells.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class WhipPlayerAnimation {
    @SubscribeEvent public static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(()->PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID,"whip_arm"),20000,Arm::new));
    }
    private static final class Arm implements IAnimation {
        final AbstractClientPlayer player;
        Arm(AbstractClientPlayer p){player=p;}
        /** A loose lash stays in this mode so first person draws it from the model hand that the world points are projected through. */
        @Override public boolean isActive(){
            if(!OwnersWhipItem.isOwner(player) || !player.isAlive() || !WhipCombat.holdsWhip(player))return false;
            var s=WhipClientMotion.forPlayer(player);
            return s!=null && (s.current.loose() || s.current.kind()==WhipMotionPayload.ATTACK);
        }
        @Override public void setupAnim(float tickDelta){}
        @Override public Vec3f get3DTransform(String bone,TransformType type,float partial,Vec3f base) {
            var s=WhipClientMotion.forPlayer(player);if(s==null)return base;
            float w=(float)s.weight(partial);if(w<=0)return base;
            boolean left=player.getMainArm()==HumanoidArm.LEFT;
            var pose=s.pose(partial,left);var arm=pose.arm();var body=pose.body();
            if(bone.equals(left?"leftArm":"rightArm")) {
                if(type==TransformType.ROTATION)return mix(base,(float)arm.x(),(float)arm.y(),(float)arm.z(),w);
                if(type==TransformType.POSITION)return mix(base,left?5:-5,2,0,w);
                return base;
            }
            if(type!=TransformType.ROTATION)return base;
            float lean=(float)body.x()*w,twist=(float)body.y()*w;
            return switch(bone) {
                case "body"->new Vec3f(lean,twist,0);
                // The model is mirrored after the body rotation, so cancelling it means adding the same angles.
                case "head"->new Vec3f(base.getX(),base.getY()+twist,base.getZ());
                case "rightLeg","leftLeg"->new Vec3f(base.getX()+lean,base.getY()+twist,base.getZ());
                default->base;
            };
        }
        private static Vec3f mix(Vec3f a,float x,float y,float z,float t) {
            return new Vec3f(a.getX()+(x-a.getX())*t,a.getY()+(y-a.getY())*t,a.getZ()+(z-a.getZ())*t);
        }
        @Override public FirstPersonMode getFirstPersonMode(float partial){return FirstPersonMode.THIRD_PERSON_MODEL;}
        @Override public FirstPersonConfiguration getFirstPersonConfiguration(float partial) {
            boolean left=player.getMainArm()==HumanoidArm.LEFT,other=!player.getOffhandItem().isEmpty();
            return new FirstPersonConfiguration(!left||other,left||other,!left||other,left||other);
        }
    }
    private WhipPlayerAnimation(){}
}
