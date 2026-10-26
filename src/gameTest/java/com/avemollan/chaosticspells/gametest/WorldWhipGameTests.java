package com.avemollan.chaosticspells.gametest;

import com.avemollan.chaosticspells.events.WhipCombat;
import com.avemollan.chaosticspells.events.WhipVisuals;
import com.avemollan.chaosticspells.item.OwnersWhipItem;
import com.avemollan.chaosticspells.network.WhipActionPayload;
import com.avemollan.chaosticspells.network.WhipMotionPayload;
import com.avemollan.chaosticspells.physics.*;
import com.avemollan.chaosticspells.registry.ChaosticItemRegistry;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
import org.joml.Matrix4d;
import org.joml.Vector3d;

@GameTestHolder("chaosticspells_tests")
@PrefixGameTestTemplate(false)
public final class WorldWhipGameTests {
    private static final int CRACK=WhipArmPose.CRACK,LENGTH=WhipArmPose.LENGTH;
    private static boolean attacking(int tick){return tick>=0 && tick<LENGTH;}
    private static WhipArmPose.Frame frame(int tick) {
        return WhipArmPose.frame(0,0,0,0,false,false,false,WhipArmPose.sample(attacking(tick),tick,CRACK,-1,LENGTH,1,false));
    }
    private static void step(WorldWhipRope rope,int tick) {
        for(int i=2;i<25;i++)rope.ground(i,0);
        rope.step(frame(tick),attacking(tick) && tick>=WhipArmPose.RELEASE,attacking(tick),attacking(tick)?WhipArmPose.drive(tick,CRACK,-1,LENGTH,0):null);
    }
    @GameTest(template="empty")
    public static void worldGravitySettlesTheLooseTipOnTheFloor(GameTestHelper h) {
        var rope=new WorldWhipRope(frame(-1));double stretch=0;
        for(int tick=-10;tick<260;tick++) {
            step(rope,tick);
            for(int i=2;i<25;i++) {
                var p=rope.point(i);
                h.assertTrue(Double.isFinite(p.x()+p.y()+p.z()) && p.y()>=.0249,"World gravity/contact must remain finite and above the floor");
                if(tick>=WhipArmPose.RELEASE)stretch=Math.max(stretch,Math.abs(Math.sqrt(p.subtract(rope.point(i-1)).lengthSquared())-WhipRope.length(i-1)));
            }
        }
        h.assertTrue(Math.abs(rope.point(24).y()-.025)<.002,"The loose tip must lie on the floor, not hover in the air");
        h.assertTrue(rope.movement()<.001,"The rope must settle after the cast, movement="+rope.movement());
        h.assertTrue(stretch<.1,"World-space constraints must preserve rope length; error="+stretch);
        h.succeed();
    }
    @GameTest(template="empty")
    public static void backswingThrowsTheLashBehindAndTheCrackSnapsItOutInFront(GameTestHelper h) {
        var rope=new WorldWhipRope(frame(-1));double behind=0,high=0,speed=0;
        for(int tick=-10;tick<LENGTH+20;tick++) {
            var old=rope.point(24);step(rope,tick);var tip=rope.point(24);
            if(tick>=0 && tick<CRACK-3) {behind=Math.min(behind,tip.z());high=Math.max(high,tip.y());}
            if(tick>=CRACK-4 && tick<=CRACK)speed=Math.max(speed,Math.sqrt(tip.subtract(old).lengthSquared()));
            if(tick==CRACK) {
                h.assertTrue(tip.z()>4.5,"At the crack the tip must be far out in front of the player, z="+tip.z());
                h.assertTrue(tip.z()>rope.point(12).z(),"At the crack the tip must lead the lash, not trail it");
            }
            if(tick==LENGTH+19)h.assertTrue(tip.z()>4,"After the crack the lash must fall in front, not recoil to the player; z="+tip.z());
        }
        h.assertTrue(behind<-2.5 && high>2.5,"The backswing must carry the lash up and behind the player; behind="+behind+", high="+high);
        h.assertTrue(speed>2,"The tip must snap forward at the crack; speed="+speed);
        h.assertTrue(WhipArmPose.sample(true,8,CRACK,-1,LENGTH,1,false).arm().x()<-Math.PI,"The player arm must visibly pass behind the shoulder");
        h.succeed();
    }
    @GameTest(template="empty")
    public static void theArmOnlyRisesDuringAStroke(GameTestHelper h) {
        h.assertTrue(WhipArmPose.sample(false,0,CRACK,-1,LENGTH,1,false)==WhipArmPose.IDLE && WhipArmPose.IDLE.arm().x()>-.5,
                "Holding the whip must keep the vanilla held-item arm, not a raised one");
        h.assertTrue(WhipArmPose.weight(false,5,LENGTH)==0 && WhipArmPose.weight(true,0,LENGTH)==0 && WhipArmPose.weight(true,LENGTH,LENGTH)==0
                && WhipArmPose.weight(true,CRACK,LENGTH)==1,"The stroke must blend in from and back out to the vanilla arm");
        // A queued backhand must not make the arm jump when it takes over from the first follow-through.
        int peakB=CRACK+2+CRACK;double jump=0;var last=WhipArmPose.sample(true,0,CRACK,peakB,peakB+LENGTH-CRACK,1,false);
        for(double t=.25;t<peakB+LENGTH-CRACK;t+=.25) {
            var next=WhipArmPose.sample(true,t,CRACK,peakB,peakB+LENGTH-CRACK,1,false);
            jump=Math.max(jump,Math.abs(next.arm().x()-last.arm().x()));last=next;
        }
        h.assertTrue(jump<.75,"Arm motion must stay continuous at quarter-tick sampling; largest step="+jump);
        h.succeed();
    }
    @GameTest(template="empty")
    public static void theGripMatchesMinecraftPalmInsteadOfTheForearm(GameTestHelper h) {
        for(boolean left:new boolean[]{false,true}) for(double yaw:new double[]{0,67,174}) for(int tick:new int[]{-1,4,8,12,20}) {
            var pose=WhipArmPose.sample(tick>=0,tick,CRACK,-1,LENGTH,1,left);var a=pose.arm();var b=pose.body();
            var f=WhipArmPose.frame(10,20,30,yaw,left,false,false,pose);
            // Independent reconstruction of the vanilla held-item pipeline and the fixed fist grip.
            Matrix4d m=new Matrix4d().translate(10,20,30).rotateY(Math.toRadians(180-yaw))
                    .translate(0,.7,0).rotateZ(b.z()).rotateY(b.y()).rotateX(b.x()).translate(0,-.7,0).scale(-.9375,-.9375,.9375)
                    .translate(0,-1.501,0).translate((left?5:-5)/16.0,2.0/16,0).rotateZ(a.z()).rotateY(a.y()).rotateX(a.x())
                    .rotateX(-Math.PI/2).rotateY(Math.PI).translate((left?-1:1)/16.0,.125,-.625)
                    .translate(0,-.125+.01,0).translate(0,-.25,-.07).rotateY(Math.PI);
            Vector3d point=m.transformPosition(new Vector3d(0,.25,0));var actual=f.point(0,.25,0);
            h.assertTrue(point.distance(actual.x(),actual.y(),actual.z())<1e-9,"Physics grip must match the rendered middle of the handle in the palm");
            Vector3d forearm=new Vector3d(0,1,0);new Matrix4d().rotateZ(a.z()).rotateY(a.y()).rotateX(a.x()).transformDirection(forearm);
            Vector3d rod=new Vector3d(0,0,-1);new Matrix4d().rotateZ(a.z()).rotateY(a.y()).rotateX(a.x()).transformDirection(rod);
            h.assertTrue(Math.abs(forearm.dot(rod))<1e-9,"The handle must be across the fist, not a continuation of the forearm");
        }
        h.succeed();
    }
    @GameTest(template="empty",batch="whip_world_camera",timeoutTicks=240)
    public static void lookingUpAndTurningDoesNotRotateTheLooseWorldTail(GameTestHelper h) {
        ServerPlayer owner=owner(h);start(owner);advance(h,owner,1,220);
        WhipMotionPayload[] before={null};
        h.runAfterDelay(200,()->{before[0]=WhipVisuals.snapshot(owner);owner.setYRot(135);owner.setXRot(-80);owner.yBodyRot=0;});
        h.runAfterDelay(205,()->{
            var after=WhipVisuals.snapshot(owner);var a=before[0];
            double dx=(after.x()+after.points()[72])-(a.x()+a.points()[72]);
            double dz=(after.z()+after.points()[74])-(a.z()+a.points()[74]);
            h.assertTrue(dx*dx+dz*dz<.0004,"Camera yaw/pitch must not rotate the settled tail in world space");
            h.assertTrue(Math.abs(after.points()[73]-.025)<.02,"Looking up must not turn gravity upward; relative tip y="+after.points()[73]);
        });
        h.runAfterDelay(221,()->finish(h,owner));
    }
    @GameTest(template="empty",batch="whip_world_network",timeoutTicks=100)
    public static void lateObserversReceiveTheSameCompleteLooseSnapshot(GameTestHelper h) {
        ServerPlayer owner=owner(h);start(owner);advance(h,owner,1,80);
        h.runAfterDelay(65,()->{
            var p=WhipVisuals.snapshot(owner);
            h.assertTrue(p.kind()==WhipMotionPayload.REST && p.loose(),"A tracker arriving after the attack must receive the loose state");
            h.assertTrue(p.item()>=0 && p.points().length==75,"Snapshot must identify the held item and contain all 25 world points");
            var bytes=new RegistryFriendlyByteBuf(Unpooled.buffer(),h.getLevel().registryAccess());
            RegistryFriendlyByteBuf first=null,second=null;
            try {
                WhipMotionPayload.CODEC.encode(bytes,p);
                h.assertTrue(bytes.readableBytes()<512,"One complete snapshot must remain bounded below 512 bytes");
                first=new RegistryFriendlyByteBuf(bytes.copy(),h.getLevel().registryAccess());
                second=new RegistryFriendlyByteBuf(bytes.copy(),h.getLevel().registryAccess());
                var a=WhipMotionPayload.CODEC.decode(first);var b=WhipMotionPayload.CODEC.decode(second);
                h.assertTrue(a.player().equals(owner.getUUID()) && a.loose() && a.item()==b.item() && a.tick()==b.tick()
                        && Arrays.equals(a.points(),b.points()) && Arrays.equals(p.points(),a.points()),"Two recipients must decode identical authoritative world coordinates");
            } finally {bytes.release();if(first!=null)first.release();if(second!=null)second.release();}
            var profile=new GameProfile(UUID.randomUUID(),"WhipObserver");
            var observer=FakePlayerFactory.get(h.getLevel(),profile);
            WhipVisuals.tracking(new PlayerEvent.StartTracking(observer,owner));
        });
        h.runAfterDelay(81,()->finish(h,owner));
    }
    private static ServerPlayer owner(GameTestHelper h) {
        for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++) {
            h.setBlock(new BlockPos(x,0,z),Blocks.STONE);
            for(int y=1;y<=4;y++)h.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        }
        var profile=new GameProfile(OwnersWhipItem.OWNER_UUID,"AveMollan");
        var p=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),profile,ClientInformation.createDefault());
        p.connection=FakePlayerFactory.get(h.getLevel(),profile).connection;p.moveTo(h.absolutePos(new BlockPos(0,1,0)).getBottomCenter());p.yBodyRot=0;
        p.setItemSlot(EquipmentSlot.MAINHAND,ChaosticItemRegistry.OWNERS_WHIP.get().getDefaultInstance());return p;
    }
    private static void start(ServerPlayer p){WhipCombat.handle(p,WhipActionPayload.START);WhipCombat.handle(p,WhipActionPayload.RELEASE);}
    private static void advance(GameTestHelper h,ServerPlayer p,int first,int last){for(int t=first;t<=last;t++)h.runAfterDelay(t,()->{p.getCooldowns().tick();WhipCombat.tick(new PlayerTickEvent.Post(p));});}
    private static void finish(GameTestHelper h,ServerPlayer p){WhipCombat.cancel(p);WhipVisuals.clear(p);h.succeed();}
}
