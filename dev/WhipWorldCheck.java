import com.avemollan.chaosticspells.physics.*;
import java.nio.file.*;
import java.util.*;

public final class WhipWorldCheck {
 public static void main(String[] args)throws Exception {
  int crack=WhipArmPose.CRACK,length=WhipArmPose.LENGTH;
  var frame=WhipArmPose.frame(0,0,0,0,false,false,false,WhipArmPose.IDLE);
  var rope=new WorldWhipRope(frame);List<String> frames=new ArrayList<>();
  double maximumForward=0,back=0,maxError=0,early=0,late=0,settled=0;
  for(int tick=-10;tick<240;tick++) {
   boolean attack=tick>=0&&tick<length;
   var pose=WhipArmPose.sample(attack,tick,crack,-1,length,1,false);
   frame=WhipArmPose.frame(0,0,0,0,false,false,false,pose);
   for(int i=2;i<25;i++)rope.ground(i,0);
   var before=rope.point(24);rope.step(frame,attack&&tick>=WhipArmPose.RELEASE,attack,attack?WhipArmPose.drive(tick,crack,-1,length,0):null);var tip=rope.point(24);
   if(tick>=0&&tick<crack-4)early=Math.max(early,tip.z()-before.z());
   if(tick>=crack-4&&tick<=crack)late=Math.max(late,tip.z()-before.z());
   if(tick>180)settled=Math.max(settled,tip.y());
   StringBuilder json=new StringBuilder("{\"tick\":"+tick+",\"arm\":["+pose.arm().x()+","+pose.arm().y()+","+pose.arm().z()+"],\"body\":["+pose.body().x()+","+pose.body().y()+"],\"points\":[");
   for(int i=0;i<25;i++) {
    var p=rope.point(i);if(i>0)json.append(',');json.append(String.format(Locale.ROOT,"[%.7f,%.7f,%.7f]",p.x(),p.y(),p.z()));
    if(tick>=0&&tick<length)maximumForward=Math.max(maximumForward,p.z());
    if(tick>=0&&tick<crack-3)back=Math.min(back,p.z());
    if(i>1)maxError=Math.max(maxError,Math.abs(Math.sqrt(p.subtract(rope.point(i-1)).lengthSquared())-WhipRope.length(i-1)));
    if(!Double.isFinite(p.x()+p.y()+p.z()))throw new AssertionError("Non-finite rope");
   }
   frames.add(json.append("]}").toString());
  }
  Files.createDirectories(Path.of("build/whip-assets"));Files.writeString(Path.of("build/whip-assets/world-frames.json"),"["+String.join(",",frames)+"]");
  System.out.printf(Locale.ROOT,"World rope: forward %.3f, back %.3f, error %.4f; early %.3f / cast %.3f; settled tip %.4f; movement %.6f%n",maximumForward,back,maxError,early,late,settled,rope.movement());
 }
}
