package com.avemollan.chaosticspells.client;

import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.events.WhipCombat;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.physics.WhipArmPose;
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

@EventBusSubscriber(modid=ChaosticSpells.MODID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class WhipPlayerAnimation {
    @SubscribeEvent public static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(()->PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                ResourceLocation.fromNamespaceAndPath(ChaosticSpells.MODID,"whip_arm"),20000,Arm::new));
    }
    private static final class Arm implements IAnimation {
        final AbstractClientPlayer player;
        Arm(AbstractClientPlayer p){player=p;}
        @Override public boolean isActive(){return OwnersWhipItem.isOwner(player) && player.isAlive() && WhipCombat.holdsWhip(player);}
        @Override public void setupAnim(float tickDelta){}
        @Override public Vec3f get3DTransform(String bone,TransformType type,float partial,Vec3f base) {
            boolean left=player.getMainArm()==HumanoidArm.LEFT;
            if(!bone.equals(left?"leftArm":"rightArm"))return base;
            var state=WhipClientMotion.forPlayer(player);
            var pose=state==null?WhipArmPose.sample(false,0,30,-1,50,1,left):state.arm(partial,left);
            if(type==TransformType.ROTATION)return new Vec3f((float)pose.x(),(float)pose.y(),(float)pose.z());
            if(type==TransformType.POSITION)return new Vec3f(left?5:-5,2,0);
            if(type==TransformType.BEND)return Vec3f.ZERO;
            return base;
        }
        @Override public FirstPersonMode getFirstPersonMode(float partial){return FirstPersonMode.THIRD_PERSON_MODEL;}
        @Override public FirstPersonConfiguration getFirstPersonConfiguration(float partial) {
            boolean left=player.getMainArm()==HumanoidArm.LEFT,other=!player.getOffhandItem().isEmpty();
            return new FirstPersonConfiguration(!left||other,left||other,!left||other,left||other);
        }
    }
    private WhipPlayerAnimation(){}
}
