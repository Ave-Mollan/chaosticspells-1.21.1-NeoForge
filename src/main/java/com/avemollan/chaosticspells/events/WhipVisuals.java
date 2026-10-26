package com.avemollan.chaosticspells.events;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.avemollan.chaosticspells.ChaosticSpells;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.network.WhipMotionPayload;
import com.avemollan.chaosticspells.physics.WhipArmPose;
import com.avemollan.chaosticspells.physics.WorldWhipRope;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoItem;

@EventBusSubscriber(modid=ChaosticSpells.MODID)
public final class WhipVisuals {
    private static final Map<UUID,State> STATES=new HashMap<>();
    private static final class State {
        final long item; final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        final WorldWhipRope rope;
        byte kind=WhipMotionPayload.REST;
        long started,lastTick=Long.MIN_VALUE,lastSent=Long.MIN_VALUE;
        int peakA=30,peakB=-1,duration=50,side=1,still;
        WhipArmPose.Frame frame;
        State(ServerPlayer player,long item) {
            this.item=item;dimension=player.level().dimension();frame=frame(player,this);rope=new WorldWhipRope(frame);
        }
    }
    private static WhipArmPose.Frame frame(ServerPlayer p,State s) {
        var angles=WhipArmPose.sample(s.kind==WhipMotionPayload.ATTACK,p.level().getGameTime()-s.started,s.peakA,s.peakB,s.duration,s.side,p.getMainArm()==HumanoidArm.LEFT);
        return WhipArmPose.frame(p.getX(),p.getY(),p.getZ(),p.yBodyRot,p.getMainArm()==HumanoidArm.LEFT,false,p.isCrouching(),angles);
    }
    private static State ensure(ServerPlayer p) {
        if(!OwnersWhipItem.isOwner(p) || !WhipCombat.holdsWhip(p) || !p.isAlive()) return null;
        long item=GeoItem.getOrAssignId(p.getMainHandItem(),p.serverLevel());
        State s=STATES.get(p.getUUID());
        if(s==null || s.item!=item || s.dimension!=p.level().dimension()) { s=new State(p,item);STATES.put(p.getUUID(),s); }
        return s;
    }
    public static void change(ServerPlayer p,byte kind,long started,int peakA,int peakB,int duration,int side) {
        State s=ensure(p);if(s==null){clear(p);return;}
        s.kind=kind;s.started=started;s.peakA=peakA;s.peakB=peakB;s.duration=duration;s.side=side;s.still=0;
        advance(p,s,true);
    }
    public static void tick(ServerPlayer p) {
        if(!OwnersWhipItem.isOwner(p)) return;
        State s=ensure(p);if(s==null){clear(p);return;}advance(p,s,false);
    }
    private static void advance(ServerPlayer p,State s,boolean force) {
        long time=p.level().getGameTime();
        var frame=frame(p,s);
        double shift=frame.point(0,.5,0).subtract(s.frame.point(0,.5,0)).lengthSquared();
        if(shift>144) {s.rope.reset(frame);s.still=0;}
        if(s.lastTick!=time) {
            boolean attack=s.kind==WhipMotionPayload.ATTACK;
            boolean release=attack && time-s.started>=3;
            if(s.still<20 || attack || shift>.000025) {
                if(s.rope.released() || release) for(int i=2;i<WorldWhipRope.POINTS;i++) {
                    var point=s.rope.point(i);Vec3 v=new Vec3(point.x(),point.y(),point.z());
                    var floor=p.level().clip(new ClipContext(v.add(0,.9,0),v.add(0,-2,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
                    s.rope.ground(i,floor.getType()==HitResult.Type.BLOCK && floor.getDirection()==Direction.UP?floor.getLocation().y:Double.NaN);
                }
                s.rope.step(frame,release,attack);
                s.still=!attack && shift<.000025 && s.rope.movement()<.0005?s.still+1:0;
            }
            s.frame=frame;s.lastTick=time;
        }
        int interval=s.kind==WhipMotionPayload.ATTACK?1:s.rope.released() && s.still<20?2:20;
        if(force || s.lastSent==Long.MIN_VALUE || time-s.lastSent>=interval) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,payload(p,s));s.lastSent=time;
        }
    }
    private static WhipMotionPayload payload(ServerPlayer p,State s) {
        double[] world=s.rope.coordinates();float[] local=new float[world.length];
        for(int i=0;i<WorldWhipRope.POINTS;i++) {local[i*3]=(float)(world[i*3]-p.getX());local[i*3+1]=(float)(world[i*3+1]-p.getY());local[i*3+2]=(float)(world[i*3+2]-p.getZ());}
        return new WhipMotionPayload(p.getUUID(),p.level().dimension().location(),s.item,p.level().getGameTime(),s.kind,s.started,
                s.peakA,s.peakB,s.duration,s.side,s.rope.released(),p.getX(),p.getY(),p.getZ(),local);
    }
    public static WhipMotionPayload snapshot(ServerPlayer p) {
        State s=ensure(p);if(s==null) return null;advance(p,s,false);return payload(p,s);
    }
    public static void clear(ServerPlayer p) {
        State s=STATES.remove(p.getUUID());
        if(s!=null) PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,new WhipMotionPayload(p.getUUID(),p.level().dimension().location(),s.item,
                p.level().getGameTime(),WhipMotionPayload.HIDDEN,0,0,-1,0,1,false,p.getX(),p.getY(),p.getZ(),new float[75]));
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking e) {
        if(e.getEntity() instanceof ServerPlayer watcher && e.getTarget() instanceof ServerPlayer owner && OwnersWhipItem.isOwner(owner)) {
            var snapshot=snapshot(owner);if(snapshot!=null) PacketDistributor.sendToPlayer(watcher,snapshot);
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {STATES.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stop(ServerStoppedEvent e) {STATES.clear();}
    private WhipVisuals() {}
}
