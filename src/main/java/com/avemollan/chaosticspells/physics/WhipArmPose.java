package com.avemollan.chaosticspells.physics;

/** Shared player-arm choreography and Minecraft palm frame. All distances are world blocks. */
public final class WhipArmPose {
    public record Point(double x,double y,double z) {
        public Point add(Point b) { return new Point(x+b.x,y+b.y,z+b.z); }
        public Point subtract(Point b) { return new Point(x-b.x,y-b.y,z-b.z); }
        public Point scale(double n) { return new Point(x*n,y*n,z*n); }
        public double lengthSquared() { return x*x+y*y+z*z; }
    }
    public record Angles(double x,double y,double z) {}
    public record Frame(Point origin,Point x,Point y,Point z) {
        public Point point(double px,double py,double pz) { return origin.add(x.scale(px)).add(y.scale(py)).add(z.scale(pz)); }
        public Point local(Point p) {
            Point d=p.subtract(origin); double s=x.lengthSquared();
            return new Point(dot(d,x)/s,dot(d,y)/s,dot(d,z)/s);
        }
    }

    public static Angles sample(boolean attack,double age,int peakA,int peakB,int duration,int side,boolean left) {
        double ax=-1.5,ay=0,az=-.08;
        if (attack && age>=0 && age<duration) {
            boolean second=peakB>=0 && age>=peakB-16;
            double phase=second ? (age-peakB+16)*30/16 : age-peakA+30;
            int direction=second?-side:side;
            if(phase<=10) {
                double t=smooth(phase/10); ax=mix(-1.5,-3.75,t); ay=-.35*direction*t; az=mix(-.08,-.3,t);
            } else if(phase<=15) {
                double t=smooth((phase-10)/5); ax=mix(-3.75,-.85,t); ay=mix(-.35,.25,t)*direction; az=mix(-.3,.1,t);
            } else {
                double t=smooth((phase-15)/26); ax=mix(-.85,-1.5,t); ay=.25*direction*(1-t); az=mix(.1,-.08,t);
            }
        }
        return new Angles(ax,left?-ay:ay,left?-az:az);
    }

    /** Same fixed perpendicular grip for a packed and a loose whip; the middle stays inside the fist. */
    public static Frame frame(double x,double y,double z,double bodyYaw,boolean left,boolean slim,boolean crouching,Angles arm) {
        Point origin=transform(new Point(0,0,0),x,y,z,bodyYaw,left,slim,crouching,arm);
        return new Frame(origin,transform(new Point(1,0,0),x,y,z,bodyYaw,left,slim,crouching,arm).subtract(origin),
                transform(new Point(0,1,0),x,y,z,bodyYaw,left,slim,crouching,arm).subtract(origin),
                transform(new Point(0,0,1),x,y,z,bodyYaw,left,slim,crouching,arm).subtract(origin));
    }
    private static Point transform(Point p,double px,double py,double pz,double yaw,boolean left,boolean slim,boolean crouch,Angles a) {
        double sign=left?1:-1;
        Point palm=new Point(p.x+sign/16, .695+p.z, .24-p.y);
        Point rotated=rotate(palm,a.x,a.y,a.z).add(new Point(sign*(slim?4.5:5)/16,2.0/16,0));
        Point model=new Point(-rotated.x*.9375,(1.501-rotated.y)*.9375,rotated.z*.9375);
        return rotate(model,0,Math.toRadians(180-yaw),0).add(new Point(px,py-(crouch?.125:0),pz));
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
