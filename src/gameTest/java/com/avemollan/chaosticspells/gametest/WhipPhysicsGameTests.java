package com.avemollan.chaosticspells.gametest;

import com.avemollan.chaosticspells.physics.WhipRope;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder("chaosticspells_tests")
@PrefixGameTestTemplate(false)
public final class WhipPhysicsGameTests {
    @GameTest(template="empty")
    public static void restingCoilClearsHandle(GameTestHelper helper) {
        for(int i=0;i<WhipRope.SEGMENTS;i++) {
            for(int j=0;j<=32;j++) {
                double t=j/32.0;
                double x=WhipRope.restX(i)+(WhipRope.restX(i+1)-WhipRope.restX(i))*t;
                double y=WhipRope.restY(i)+(WhipRope.restY(i+1)-WhipRope.restY(i))*t;
                double z=WhipRope.restZ(i)+(WhipRope.restZ(i+1)-WhipRope.restZ(i))*t;
                // The first neck is attached to the top ferrule. Only that deliberate joint may touch it.
                helper.assertTrue((i==0 && y>=WhipRope.GRIP_LENGTH-.005)
                        || !(Math.abs(x)<.145 && y>-.1 && y<.55 && Math.abs(z)<.145),
                        "No resting leather segment may intersect the grip/ferrule, including its thickness");
            }
        }
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void ropeExtendsForwardAndStaysFiniteUnderMotion(GameTestHelper helper) {
        WhipRope rope=new WhipRope();
        for(int i=0;i<WhipRope.POINTS;i++) rope.ground(i,1.1);
        for(int i=0;i<20;i++) rope.tick(-1,10,-1,20,1,false,0,0,0,-.035,0);
        rope.beginAttack();
        double forward=0,maxStretch=0;
        for(int tick=0;tick<90;tick++) {
            rope.tick(tick,30,50,70,1,false,0,0,0,-.035,0);
            for(int i=0;i<WhipRope.POINTS;i++) {
                helper.assertTrue(Double.isFinite(rope.x(i,1)+rope.y(i,1)+rope.z(i,1)),"The rope must not generate NaN/infinite positions");
                forward=Math.min(forward,rope.z(i,1));
                if(i>0) {
                    double dx=rope.x(i,1)-rope.x(i-1,1),dy=rope.y(i,1)-rope.y(i-1,1),dz=rope.z(i,1)-rope.z(i-1,1);
                    maxStretch=Math.max(maxStretch,Math.abs(Math.sqrt(dx*dx+dy*dy+dz*dz)-WhipRope.length(i-1)));
                }
            }
        }
        helper.assertTrue(forward < -5,"The lash must extend forward, rather than point upward");
        helper.assertTrue(maxStretch<.12,"Distance constraints must keep the chain connected; error="+maxStretch);
        rope.turn(.4,.25);
        for(int i=0;i<30;i++) rope.tick(-1,10,-1,20,1,false,.05,-.04,.03,-.03,.01);
        helper.assertTrue(Double.isFinite(rope.x(24,1)+rope.y(24,1)+rope.z(24,1)),"Turning/accelerating must not destabilize the rope");
        helper.succeed();
    }

    @GameTest(template="empty")
    public static void tipAcceleratesAndLooseRopeDoesNotSpringBackIntoCoil(GameTestHelper helper) {
        WhipRope rope=new WhipRope();
        for(int i=0;i<WhipRope.POINTS;i++) rope.ground(i,1.1);
        double earlySpeed=0,castSpeed=0,backswing=0,forward=0;
        for(int tick=0;tick<85;tick++) {
            double px=rope.x(24,1),py=rope.y(24,1),pz=rope.z(24,1);
            rope.tick(tick<50?tick:-1,30,-1,50,1,false,0,0,0,-.035,0);
            double dx=rope.x(24,1)-px,dy=rope.y(24,1)-py,dz=rope.z(24,1)-pz;
            // Compare forward motion after the wrist turns forward, not the sideways uncoiling
            // velocity when the held bundle is first released during the back cast.
            double speed=Math.max(0,-dz);
            if(tick>=8 && tick<=13) earlySpeed=Math.max(earlySpeed,speed);
            if(tick>=18 && tick<=34) castSpeed=Math.max(castSpeed,speed);
            if(tick<10) for(int i=0;i<WhipRope.POINTS;i++) backswing=Math.max(backswing,rope.z(i,1));
            if(tick<40) for(int i=0;i<WhipRope.POINTS;i++) forward=Math.max(forward,-rope.z(i,1));
            for(int i=2;i<WhipRope.POINTS;i++) helper.assertTrue(rope.y(i,1)>=-1.076,"The loose lash must not fall through its cached ground contact");
        }
        helper.assertTrue(backswing>.4 && forward>5,"The rope must first cast behind the hand, then extend forward");
        helper.assertTrue(castSpeed>.5 && castSpeed>earlySpeed*1.5,"The tapered tip must gain forward speed during the cast; early="+earlySpeed+", cast="+castSpeed);
        double dx=rope.x(24,1)-WhipRope.restX(24),dy=rope.y(24,1)-WhipRope.restY(24),dz=rope.z(24,1)-WhipRope.restZ(24);
        // A loose end may settle near the player's feet. It must remain separated from the raised
        // held bundle, without imposing an arbitrary two-block minimum horizontal displacement.
        helper.assertTrue(rope.isReleased() && dx*dx+dy*dy+dz*dz>1,"Follow-through must leave a loose physical rope, rather than attract it back into the coil");
        helper.succeed();
    }
}
