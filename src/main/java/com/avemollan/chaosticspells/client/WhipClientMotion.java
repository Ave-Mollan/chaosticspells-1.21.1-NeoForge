package com.avemollan.chaosticspells.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.network.WhipMotionPayload;
import com.avemollan.chaosticspells.physics.WhipArmPose;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import software.bernie.geckolib.animatable.GeoItem;

/** Rendering interpolates authoritative world snapshots. No camera-driven client rope simulation. */
@EventBusSubscriber(modid=ChaosticSpells.MODID,value=Dist.CLIENT)
public final class WhipClientMotion {
    private static final Map<UUID,Motion> STATES=new HashMap<>();
    private static Object currentLevel;
    public static final class Motion {
        public WhipMotionPayload previous,current;
        Motion(WhipMotionPayload p){previous=current=p;}
        double alpha(double partial) {
            var level=Minecraft.getInstance().level;
            return level==null?1:Math.clamp((level.getGameTime()+partial-current.tick())/Math.max(1,current.tick()-previous.tick()),0,1);
        }
        public double time(double partial) {return previous.tick()+(current.tick()-previous.tick())*alpha(partial);}
        public Vec3 point(int i,double partial) {
            int j=i*3; double t=alpha(partial);
            Vec3 a=new Vec3(previous.x()+previous.points()[j],previous.y()+previous.points()[j+1],previous.z()+previous.points()[j+2]);
            Vec3 b=new Vec3(current.x()+current.points()[j],current.y()+current.points()[j+1],current.z()+current.points()[j+2]);
            return a.lerp(b,t);
        }
        public WhipArmPose.Pose pose(double partial,boolean left) {
            return WhipArmPose.sample(current.kind()==WhipMotionPayload.ATTACK,time(partial)-current.started(),
                    current.peakA(),current.peakB(),current.duration(),current.side(),left);
        }
        public double weight(double partial) {
            return WhipArmPose.weight(current.kind()==WhipMotionPayload.ATTACK,time(partial)-current.started(),current.duration());
        }
    }
    public static void accept(WhipMotionPayload p) {
        var level=Minecraft.getInstance().level;
        if(currentLevel!=level){STATES.clear();currentLevel=level;}
        if(level==null || !p.dimension().equals(level.dimension().location()))return;
        if(!OwnersWhipItem.isOwner(level.getPlayerByUUID(p.player())))return;
        if(p.kind()==WhipMotionPayload.HIDDEN){STATES.remove(p.player());return;}
        Motion s=STATES.get(p.player());
        // After leaving tracking range the last snapshot is stale; start over instead of sliding from it.
        if(s==null || s.current.item()!=p.item() || p.tick()-s.current.tick()>40){STATES.put(p.player(),new Motion(p));return;}
        if(p.tick()<s.current.tick())return;
        if(p.tick()>s.current.tick())s.previous=s.current;
        s.current=p;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post e) {
        var level=Minecraft.getInstance().level;
        if(currentLevel!=level){STATES.clear();currentLevel=level;}
        else if(level!=null && !STATES.isEmpty() && OwnersWhipItem.owner(level)==null) STATES.clear();
    }
    public static Motion forPlayer(Player p){return STATES.get(p.getUUID());}
    public static Motion forStack(ItemStack stack,ItemDisplayContext context) {
        if(stack==null || context==null || context==ItemDisplayContext.GUI || context==ItemDisplayContext.GROUND
                || context==ItemDisplayContext.FIXED || context==ItemDisplayContext.HEAD)return null;
        var level=Minecraft.getInstance().level;if(level==null)return null;
        var holder=OwnersWhipItem.owner(level);
        if(holder==null || !(holder.getMainHandItem().getItem() instanceof OwnersWhipItem))return null;
        Motion s=STATES.get(holder.getUUID());
        return s!=null && GeoItem.getId(stack)==s.current.item()?s:null;
    }
    public static boolean canQueueFollowUp() {
        var client=Minecraft.getInstance();
        if(client.level==null || client.player==null || !OwnersWhipItem.isOwner(client.player))return false;
        var s=STATES.get(client.player.getUUID());if(s==null)return false;
        long age=client.level.getGameTime()-s.current.started();
        return s.current.kind()==WhipMotionPayload.ATTACK && s.current.peakB()<0 && age>0 && age<=s.current.peakA()+8;
    }
    private WhipClientMotion(){}
}
