package com.avemollan.chaosticspells.physics;

import java.util.Arrays;
import com.avemollan.chaosticspells.physics.WhipArmPose.Point;
import com.avemollan.chaosticspells.physics.WhipArmPose.Frame;

/** Authoritative world-space rope. Looking around never rotates its free particles or gravity. */
public final class WorldWhipRope {
    public static final int POINTS=25, SEGMENTS=24, SUBSTEPS=8, ITERATIONS=12;
    private final double[] x=new double[POINTS],y=new double[POINTS],z=new double[POINTS];
    private final double[] ox=new double[POINTS],oy=new double[POINTS],oz=new double[POINTS],ground=new double[POINTS];
    private static final double[] MASS=new double[POINTS];
    private Frame previous;
    private boolean released;
    private double movement;
    private double floorDrag;
    static { for(int i=2;i<POINTS;i++) MASS[i]=1/(.008+.992*Math.pow(1-i/24.0,2)); }
    public WorldWhipRope(Frame frame) { Arrays.fill(ground,Double.NaN); reset(frame); }
    public void ground(int i,double height) { ground[i]=height; }
    public boolean released() { return released; }
    public double movement() { return movement; }
    public Point point(int i) { return new Point(x[i],y[i],z[i]); }
    public double[] coordinates() {
        double[] result=new double[POINTS*3];
        for(int i=0;i<POINTS;i++) { result[i*3]=x[i];result[i*3+1]=y[i];result[i*3+2]=z[i]; }
        return result;
    }
    public void reset(Frame frame) {
        previous=frame; released=false;
        for(int i=0;i<POINTS;i++) {
            Point p=frame.point(WhipRope.restX(i),WhipRope.restY(i),WhipRope.restZ(i));
            x[i]=ox[i]=p.x();y[i]=oy[i]=p.y();z[i]=oz[i]=p.z();
        }
    }
    public void step(Frame frame,boolean release,boolean attacking) {
        if(!released && !release) {
            for(int i=0;i<POINTS;i++) {
                Point p=frame.point(WhipRope.restX(i),WhipRope.restY(i),WhipRope.restZ(i));
                ox[i]=x[i];oy[i]=y[i];oz[i]=z[i];x[i]=p.x();y[i]=p.y();z[i]=p.z();
            }
            previous=frame; return;
        }
        if(!released) {
            released=true;
            for(int i=2;i<POINTS;i++) { ox[i]=x[i]-(x[i]-ox[i])/SUBSTEPS;oy[i]=y[i]-(y[i]-oy[i])/SUBSTEPS;oz[i]=z[i]-(z[i]-oz[i])/SUBSTEPS; }
        }
        double[] start=coordinates();
        double damping=attacking?.995:.978;
        floorDrag=attacking?.9995:.995;
        for(int sub=0;sub<SUBSTEPS;sub++) {
            for(int i=2;i<POINTS;i++) {
                double vx=(x[i]-ox[i])*damping,vy=(y[i]-oy[i])*damping,vz=(z[i]-oz[i])*damping;
                ox[i]=x[i];oy[i]=y[i];oz[i]=z[i];
                x[i]+=vx;y[i]+=vy-.035/(SUBSTEPS*SUBSTEPS);z[i]+=vz;
            }
            double t=(sub+1.0)/SUBSTEPS;
            for(int it=0;it<ITERATIONS;it++) {
                pin(frame,t);
                for(int i=1;i<SEGMENTS;i++) link(i,i+1,WhipRope.length(i),1);
                for(int i=SEGMENTS-1;i>=1;i--) link(i,i+1,WhipRope.length(i),1);
                if(attacking) for(int i=1;i<SEGMENTS-1;i++) link(i,i+2,WhipRope.length(i)+WhipRope.length(i+1),.025*(1-i/26.0));
                floor();
            }
            pin(frame,t);
        }
        previous=frame;movement=0;
        for(int i=2;i<POINTS;i++) {
            movement=Math.max(movement,Math.sqrt(Math.pow(x[i]-start[i*3],2)+Math.pow(y[i]-start[i*3+1],2)+Math.pow(z[i]-start[i*3+2],2)));
        }
    }
    private void pin(Frame next,double t) {
        for(int i=0;i<2;i++) {
            double distance=.5+(i==1?WhipRope.length(0):0);
            Point a=previous.point(0,distance,0),b=next.point(0,distance,0),p=a.add(b.subtract(a).scale(t));
            x[i]=ox[i]=p.x();y[i]=oy[i]=p.y();z[i]=oz[i]=p.z();
        }
    }
    private void link(int i,int j,double length,double stiffness) {
        double dx=x[j]-x[i],dy=y[j]-y[i],dz=z[j]-z[i],d=Math.sqrt(dx*dx+dy*dy+dz*dz),w=MASS[i]+MASS[j];
        if(d<1e-9 || w==0) return;
        double k=(d-length)/d*stiffness/w,a=k*MASS[i],b=k*MASS[j];
        x[i]+=dx*a;y[i]+=dy*a;z[i]+=dz*a;x[j]-=dx*b;y[j]-=dy*b;z[j]-=dz*b;
    }
    private void floor() {
        for(int i=2;i<POINTS;i++) if(!Double.isNaN(ground[i]) && y[i]<ground[i]+.025) {
            y[i]=ground[i]+.025;
            if(oy[i]>y[i]) oy[i]=y[i];
            ox[i]=x[i]-(x[i]-ox[i])*floorDrag;oz[i]=z[i]-(z[i]-oz[i])*floorDrag;
        }
    }
}
