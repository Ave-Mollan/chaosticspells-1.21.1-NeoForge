import com.avemollan.chaosticspells.physics.WhipRope;
import java.nio.file.*;
import java.util.*;

/** Exports the rest seed and legacy local-space solver frames for comparisons. Production uses WorldWhipRope. */
public final class WhipPhysicsCheck {
    public static void main(String[] args) throws Exception {
        WhipRope rope = new WhipRope();
        for(int i=0;i<WhipRope.POINTS;i++) rope.ground(i,1.1);
        Files.createDirectories(Path.of("build/whip-assets"));
        StringBuilder rest = new StringBuilder("[");
        for (int i=0;i<WhipRope.POINTS;i++) {
            if(i>0) rest.append(',');
            rest.append(String.format(Locale.ROOT,"[%.8f,%.8f,%.8f]",WhipRope.restX(i),WhipRope.restY(i),WhipRope.restZ(i)));
        }
        Files.writeString(Path.of("build/whip-assets/physics-rest.json"),rest.append(']').toString());
        List<String> frames = new ArrayList<>();
        double maxStretch = 0, minZ = 0;
        for (int tick = -20; tick < 95; tick++) {
            if (tick == 0) rope.beginAttack();
            rope.tick(tick < 0 ? -1 : tick, 30, 50, 70, 1, false, 0, 0, 0, -.035, 0);
            StringBuilder frame = new StringBuilder("{\"tick\":" + tick + ",\"handle\":" + rope.handleAngle(1)
                    + ",\"yaw\":"+rope.handleYaw(1)+",\"hand\":["+rope.handX(1)+","+rope.handY(1)+","+rope.handZ(1)+"],\"points\":[");
            for (int i = 0; i < WhipRope.POINTS; i++) {
                if (i > 0) frame.append(',');
                frame.append(String.format(Locale.ROOT, "[%.6f,%.6f,%.6f]", rope.x(i,1),rope.y(i,1),rope.z(i,1)));
                minZ = Math.min(minZ, rope.z(i,1));
                if (i > 0) {
                    double dx=rope.x(i,1)-rope.x(i-1,1),dy=rope.y(i,1)-rope.y(i-1,1),dz=rope.z(i,1)-rope.z(i-1,1);
                    maxStretch=Math.max(maxStretch,Math.abs(Math.sqrt(dx*dx+dy*dy+dz*dz)-WhipRope.length(i-1)));
                }
                if (!Double.isFinite(rope.x(i,1)+rope.y(i,1)+rope.z(i,1))) throw new AssertionError("Non-finite rope state");
            }
            frames.add(frame.append("]}").toString());
        }
        Files.writeString(Path.of("build/whip-assets/physics-frames.json"),"["+String.join(",",frames)+"]");
        if (maxStretch>.20) throw new AssertionError("Rope stretches too far: "+maxStretch);
        if (minZ>-2) throw new AssertionError("Whip does not extend forward: "+minZ);
        long start=System.nanoTime();
        for(int i=0;i<10000;i++) rope.tick(-1,10,-1,20,1,false,0,0,0,-.035,0);
        System.out.printf(Locale.ROOT,"Physics: max segment error %.4f blocks; forward extension %.2f blocks; mean tick %.3f ms%n",
                maxStretch,-minZ,(System.nanoTime()-start)/1e10);
    }
}
