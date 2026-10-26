package com.avemollan.chaosticspells.physics;

/** Shared player choreography and Minecraft palm frame. All distances are world blocks. */
public final class WhipArmPose {
    public record Point(double x,double y,double z) {
        public Point add(Point b) { return new Point(x+b.x,y+b.y,z+b.z); }
        public Point subtract(Point b) { return new Point(x-b.x,y-b.y,z-b.z); }
        public Point scale(double n) { return new Point(x*n,y*n,z*n); }
        public double lengthSquared() { return x*x+y*y+z*z; }
    }
    public record Angles(double x,double y,double z) {}
    /** Arm rotation plus Player Animator's whole-body rotation, which pivots 0.7 blocks above the feet. */
    public record Pose(Angles arm,Angles body) {}
    public record Frame(Point origin,Point x,Point y,Point z) {
        public Point point(double px,double py,double pz) { return origin.add(x.scale(px)).add(y.scale(py)).add(z.scale(pz)); }
        public Point local(Point p) {
            Point d=p.subtract(origin); double s=x.lengthSquared();
            return new Point(dot(d,x)/s,dot(d,y)/s,dot(d,z)/s);
        }
    }

    /** Ticks from a stroke's start to the tip snapping out in front, and to the arm's return to rest; RELEASE frees the coil. */
    public static final int CRACK=16, LENGTH=30, RELEASE=1;
    /** Vanilla's arm pose for a held item, so a stroke starts and ends where the idle arm is. */
    public static final Angles REST=new Angles(-Math.PI/10,0,0), STILL=new Angles(0,0,0);
    public static final Pose IDLE=new Pose(REST,STILL);
    // tick, arm x/y/z, body lean/twist, ease into this key (0 smooth, 1 accelerating, 2 decelerating).
    // Arm y/z follow the stroke side; +lean is backwards, -twist turns the whip shoulder back.
    private static final double[][] KEYS={
            {0, -Math.PI/10,0,0, 0,0, 0},
            {5, -3.3,-.08,-.1, .06,-.22, 0},    // lift up and back over the shoulder, torso turns away
            {8, -3.75,-.1,-.12, .1,-.28, 0},    // brief hold: the lash keeps flying back by its own inertia
            {11,-1.65,.04,.03, -.1,.2, 1},      // whip-through, fastest at the end, stopping level with the eyes
            {13,-1.4,.06,.04, -.13,.24, 2},     // hard stop and slight wrist drop: the loop rolls out to the tip
            {LENGTH,-Math.PI/10,0,0, 0,0, 0}};  // follow-through back to the idle arm

    public static Pose sample(boolean attack,double age,int peakA,int peakB,int duration,int side,boolean left) {
        if(!attack || age<0 || age>=duration) return IDLE;
        double[] pose=stroke(age-(peakA-CRACK),side);
        if(peakB>=0) {
            double start=peakB-CRACK;
            if(age>=start) {
                // The backhand mirrors the stroke and grows out of wherever the first follow-through is.
                double[] back=stroke(age-start,-side);double t=smooth((age-start)/3);
                for(int i=0;i<pose.length;i++) pose[i]=mix(pose[i],back[i],t);
            }
        }
        double m=left?-1:1;
        return new Pose(new Angles(pose[0],pose[1]*m,pose[2]*m),new Angles(pose[3],pose[4]*m,0));
    }
    /** Push along the lash for this tick: back and up on the backswing, out ahead on the whip-through, then the crack's loss. */
    public static WorldWhipRope.Drive drive(double age,int peakA,int peakB,int duration,double bodyYaw) {
        if(age<0 || age>=duration) return null;
        double phase=peakB>=0 && age>=peakB-CRACK?age-(peakB-CRACK):age-(peakA-CRACK);
        double yaw=Math.toRadians(bodyYaw),fx=-Math.sin(yaw),fz=Math.cos(yaw);
        if(phase>=1 && phase<9) return new WorldWhipRope.Drive(unit(-fx,1.4,-fz),.3,Math.clamp((phase-1)/6,0,1),1.2,0);
        if(phase>=10 && phase<CRACK) return new WorldWhipRope.Drive(unit(fx,.4,fz),.8,Math.clamp((phase-10)/(CRACK-11),0,1),3,0);
        if(phase>=CRACK && phase<CRACK+2) return new WorldWhipRope.Drive(new Point(0,1,0),0,1,1,.75);
        return null;
    }
    private static Point unit(double x,double y,double z) { double l=Math.sqrt(x*x+y*y+z*z); return new Point(x/l,y/l,z/l); }
    /** 0..1 blend from the vanilla arm into the stroke and back, so neither end pops. */
    public static double weight(boolean attack,double age,int duration) {
        return attack && age>=0 && age<duration ? Math.min(smooth(age/2),smooth((duration-age)/4)) : 0;
    }
    private static double[] stroke(double phase,int side) {
        double[] a=KEYS[0],b=KEYS[KEYS.length-1];
        if(phase<=0 || phase>=LENGTH) return new double[]{b[1],0,0,0,0};
        for(int i=1;i<KEYS.length;i++) if(phase<=KEYS[i][0]) { a=KEYS[i-1];b=KEYS[i];break; }
        double t=Math.clamp((phase-a[0])/(b[0]-a[0]),0,1);
        t=b[6]==1?t*t*t:b[6]==2?1-(1-t)*(1-t):smooth(t);
        return new double[]{mix(a[1],b[1],t),mix(a[2],b[2],t)*side,mix(a[3],b[3],t)*side,mix(a[4],b[4],t),mix(a[5],b[5],t)};
    }

    /** Same fixed perpendicular grip for a packed and a loose whip; the middle stays inside the fist. */
    public static Frame frame(double x,double y,double z,double bodyYaw,boolean left,boolean slim,boolean crouching,Pose pose) {
        Point origin=transform(new Point(0,0,0),x,y,z,bodyYaw,left,slim,crouching,pose);
        return new Frame(origin,transform(new Point(1,0,0),x,y,z,bodyYaw,left,slim,crouching,pose).subtract(origin),
                transform(new Point(0,1,0),x,y,z,bodyYaw,left,slim,crouching,pose).subtract(origin),
                transform(new Point(0,0,1),x,y,z,bodyYaw,left,slim,crouching,pose).subtract(origin));
    }
    private static Point transform(Point p,double px,double py,double pz,double yaw,boolean left,boolean slim,boolean crouch,Pose pose) {
        double sign=left?1:-1;Angles a=pose.arm(),b=pose.body();
        Point palm=new Point(p.x+sign/16, .695+p.z, .24-p.y);
        Point rotated=rotate(palm,a.x,a.y,a.z).add(new Point(sign*(slim?4.5:5)/16,2.0/16,0));
        Point model=new Point(-rotated.x*.9375,(1.501-rotated.y)*.9375,rotated.z*.9375);
        Point body=rotate(model.subtract(new Point(0,.7,0)),b.x,b.y,b.z).add(new Point(0,.7,0));
        return rotate(body,0,Math.toRadians(180-yaw),0).add(new Point(px,py-(crouch?.125:0),pz));
    }
    public static Point rotate(Point p,double ax,double ay,double az) {
        double sy=p.y*Math.cos(ax)-p.z*Math.sin(ax),sz=p.y*Math.sin(ax)+p.z*Math.cos(ax);
        double sx=p.x*Math.cos(ay)+sz*Math.sin(ay); sz=-p.x*Math.sin(ay)+sz*Math.cos(ay);
        return new Point(sx*Math.cos(az)-sy*Math.sin(az),sx*Math.sin(az)+sy*Math.cos(az),sz);
    }
    public static double dot(Point a,Point b) { return a.x*b.x+a.y*b.y+a.z*b.z; }
    private static double smooth(double t) { t=Math.clamp(t,0,1); return t*t*(3-2*t); }
    private static double mix(double a,double b,double t) { return a+(b-a)*t; }
    private WhipArmPose() {}
}
