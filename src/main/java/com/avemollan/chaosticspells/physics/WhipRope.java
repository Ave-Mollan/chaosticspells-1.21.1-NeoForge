package com.avemollan.chaosticspells.physics;

/** Tapered, inextensible leather rope driven at the wrist. Coordinates are blocks in the hand's frame. */
public final class WhipRope {
    public static final int SEGMENTS = 24, POINTS = SEGMENTS + 1;
    public static final int SUBSTEPS = 8, ITERATIONS = 12;
    public static final double GRIP_LENGTH = .5, ANCHOR_HEIGHT = .5;
    private static final double[] LENGTH = new double[SEGMENTS], INVERSE_MASS = new double[POINTS];
    private static final double[] REST_X = new double[POINTS], REST_Y = new double[POINTS], REST_Z = new double[POINTS];
    private final double[] x = new double[POINTS], y = new double[POINTS], z = new double[POINTS];
    private final double[] oldX = new double[POINTS], oldY = new double[POINTS], oldZ = new double[POINTS];
    private final double[] renderX = new double[POINTS], renderY = new double[POINTS], renderZ = new double[POINTS];
    private double handleAngle, oldHandleAngle, handleYaw, oldHandleYaw;
    private double handX, handY, handZ, oldHandX, oldHandY, oldHandZ;
    private final double[] ground = new double[POINTS];
    private double groundY = 1, groundZ;
    private boolean released;
    private int simulatedTicks;

    static {
        for (int i = 0; i < SEGMENTS; i++) LENGTH[i] = i < 2 ? .1875 : (i < 20 ? 5 : 5.5) / 16.0;
        for (int i = 2; i < POINTS; i++) INVERSE_MASS[i] = 1 / (.008 + .992 * Math.pow(1 - i / (double) SEGMENTS, 2));
        REST_Y[0] = ANCHOR_HEIGHT;
        REST_X[1] = .16; REST_Y[1] = .60; REST_Z[1] = -.055;
        REST_X[2] = .31; REST_Y[2] = .50; REST_Z[2] = -.10;
        double angle = Math.toRadians(135), radius = .28;
        double centerX = REST_X[2] - radius * Math.cos(angle), centerY = REST_Y[2] - radius * Math.sin(angle);
        for (int i = 2; i < SEGMENTS; i++) {
            angle -= 2 * Math.asin(LENGTH[i] / (2 * radius));
            REST_X[i + 1] = centerX + radius * Math.cos(angle);
            REST_Y[i + 1] = centerY + radius * Math.sin(angle);
            REST_Z[i + 1] = -.10 - (Math.toRadians(135) - angle) * .024;
        }
    }

    public WhipRope() { reset(); }

    public void reset() {
        System.arraycopy(REST_X, 0, x, 0, POINTS); System.arraycopy(REST_Y, 0, y, 0, POINTS); System.arraycopy(REST_Z, 0, z, 0, POINTS);
        System.arraycopy(x, 0, oldX, 0, POINTS); System.arraycopy(y, 0, oldY, 0, POINTS); System.arraycopy(z, 0, oldZ, 0, POINTS);
        handleAngle = oldHandleAngle = handleYaw = oldHandleYaw = 0;
        handX = handY = handZ = oldHandX = oldHandY = oldHandZ = 0;
        released = false; simulatedTicks = 0; snapshot();
        java.util.Arrays.fill(ground, Double.NaN);
    }

    /** Cached block-top contacts are sampled by the client once per tick, outside the solver loop. */
    public void groundNormal(double y, double z) { groundY = y; groundZ = z; }
    public void ground(int point, double offset) { ground[point] = offset; }

    /** A new stroke moves the wrist; it never injects velocity into the tip or resets an already loose rope. */
    public void beginAttack() {}

    public void turn(double yawRadians, double pitchRadians) {
        double yaw = Math.clamp(yawRadians, -.65, .65), pitch = Math.clamp(pitchRadians, -.45, .45);
        double cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(pitch), sp = Math.sin(pitch);
        for (int i = 0; i < POINTS; i++) {
            double nx = x[i] * cy - z[i] * sy, nz = x[i] * sy + z[i] * cy, ny = y[i] - ANCHOR_HEIGHT;
            x[i] = nx; y[i] = ny * cp - nz * sp + ANCHOR_HEIGHT; z[i] = ny * sp + nz * cp;
            nx = oldX[i] * cy - oldZ[i] * sy; nz = oldX[i] * sy + oldZ[i] * cy; ny = oldY[i] - ANCHOR_HEIGHT;
            oldX[i] = nx; oldY[i] = ny * cp - nz * sp + ANCHOR_HEIGHT; oldZ[i] = ny * sp + nz * cp;
        }
    }

    public void tick(int age, int peakA, int peakB, int duration, int side, boolean charging,
                     double inertiaX, double inertiaY, double inertiaZ, double gravityY, double gravityZ) {
        snapshot(); oldHandleAngle = handleAngle; oldHandleYaw = handleYaw;
        oldHandX = handX; oldHandY = handY; oldHandZ = handZ;
        boolean active = age >= 0 && age < duration;
        boolean second = active && peakB >= 0 && age >= peakB - 16;
        int peak = second ? peakB : peakA, direction = second ? -side : side;
        double phase = second ? (age-peak+16)*30.0/16 : age-peak+30;
        if (active) {
            // Only the hand is prescribed: draw back, accelerate through, then turn the wrist in follow-through.
            if (phase <= 8) {
                double t = smooth(Math.clamp(phase / 8, 0, 1));
                handleAngle = .08 + 1.17 * t; handleYaw = direction * (.12 + .28 * t);
                handX = direction * .15 * t; handY = .22 * t; handZ = .65 * t;
            } else if (phase <= 13) {
                double t = smooth((phase - 8) / 5);
                handleAngle = 1.25 - 2.95 * t; handleYaw = direction * (.4 - .95 * t);
                handX = direction * (.15 - .25 * t); handY = .22 + .1 * t; handZ = .65 - 1.55 * t;
            } else if (phase <= 23) {
                double t = smooth((phase - 13) / 10);
                handleAngle = -1.7 - .65 * t; handleYaw = direction * (-.55 + 1.65 * t);
                handX = direction * (-.1 + .28 * t); handY = .32 - .5 * t; handZ = -.9 + .3 * t;
            } else {
                double t = smooth(Math.clamp((phase - 23) / 18, 0, 1));
                handleAngle = -2.35 * (1 - t); handleYaw = direction * 1.1 * (1 - t);
                handX = direction * .18 * (1 - t); handY = -.18 * (1 - t); handZ = -.6 * (1 - t);
            }
        } else {
            handleAngle += ((charging ? .30 : .04) - handleAngle) * .18;
            handleYaw *= .82;
            handX *= .82; handY *= .82; handZ *= .82;
        }
        // Bound the wrist's acceleration across a queued second stroke instead of teleporting it.
        handleAngle = oldHandleAngle + Math.clamp(handleAngle-oldHandleAngle, -.65, .65);
        handleYaw = oldHandleYaw + Math.clamp(handleYaw-oldHandleYaw, -.4, .4);
        handX = oldHandX + Math.clamp(handX-oldHandX, -.3, .3);
        handY = oldHandY + Math.clamp(handY-oldHandY, -.15, .15);
        handZ = oldHandZ + Math.clamp(handZ-oldHandZ, -.3, .3);
        // The coil is held as a bundle until the back cast releases it. Once loose, no spring
        // pulls its particles back to the rest pose: the travelling loop carries on around the wrist.
        if (!released && active && phase >= 3) {
            released = true;
            // Held-pose history spans one game tick; Verlet history spans one substep.
            for (int i = 2; i < POINTS; i++) {
                oldX[i] = x[i] - (x[i] - oldX[i]) / SUBSTEPS;
                oldY[i] = y[i] - (y[i] - oldY[i]) / SUBSTEPS;
                oldZ[i] = z[i] - (z[i] - oldZ[i]) / SUBSTEPS;
            }
        }
        if (!released) {
            heldCoil(); simulatedTicks++; return;
        }
        double dt = 1.0 / SUBSTEPS;
        for (int sub = 0; sub < SUBSTEPS; sub++) {
            for (int i = 2; i < POINTS; i++) {
                double vx = (x[i] - oldX[i]) * .996, vy = (y[i] - oldY[i]) * .996, vz = (z[i] - oldZ[i]) * .996;
                oldX[i] = x[i]; oldY[i] = y[i]; oldZ[i] = z[i];
                x[i] += vx + Math.clamp(inertiaX, -.12, .12) * dt * dt;
                y[i] += vy + (gravityY + Math.clamp(inertiaY, -.12, .12)) * dt * dt;
                z[i] += vz + (gravityZ + Math.clamp(inertiaZ, -.12, .12)) * dt * dt;
            }
            double alpha = (sub + 1.0) / SUBSTEPS;
            for (int it = 0; it < ITERATIONS; it++) {
                pinNeck(alpha);
                for (int i = 1; i < SEGMENTS; i++) constrain(i, i + 1, LENGTH[i], 1);
                for (int i = SEGMENTS - 1; i >= 1; i--) constrain(i, i + 1, LENGTH[i], 1);
                // Leather has a little bending resistance, tapering away towards the flexible cracker.
                for (int i = 1; i < SEGMENTS - 1; i++) constrain(i, i + 2, LENGTH[i] + LENGTH[i + 1], .05 * (1 - i / 26.0));
                keepOutsideGrip(alpha);
                keepAboveGround();
            }
            pinNeck(alpha);
        }
        simulatedTicks++;
    }

    private void heldCoil() {
        double c = Math.cos(handleAngle), s = Math.sin(handleAngle), cy = Math.cos(handleYaw), sy = Math.sin(handleYaw);
        for (int i = 0; i < POINTS; i++) {
            oldX[i] = x[i]; oldY[i] = y[i]; oldZ[i] = z[i];
            double ry = REST_Y[i] * c - REST_Z[i] * s, rz = REST_Y[i] * s + REST_Z[i] * c;
            x[i] = handX + REST_X[i] * cy + rz * sy; y[i] = handY + ry; z[i] = handZ - REST_X[i] * sy + rz * cy;
        }
    }

    private void pinNeck(double alpha) {
        double a = oldHandleAngle + (handleAngle - oldHandleAngle) * alpha, b = oldHandleYaw + (handleYaw - oldHandleYaw) * alpha;
        for (int i = 0; i <= 1; i++) {
            double length = ANCHOR_HEIGHT + (i == 1 ? LENGTH[0] : 0);
            x[i] = oldHandX + (handX-oldHandX)*alpha + length * Math.sin(a) * Math.sin(b);
            y[i] = oldHandY + (handY-oldHandY)*alpha + length * Math.cos(a);
            z[i] = oldHandZ + (handZ-oldHandZ)*alpha + length * Math.sin(a) * Math.cos(b);
            oldX[i] = x[i]; oldY[i] = y[i]; oldZ[i] = z[i];
        }
    }

    private void constrain(int i, int j, double length, double stiffness) {
        double dx = x[j] - x[i], dy = y[j] - y[i], dz = z[j] - z[i], distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double weight = INVERSE_MASS[i] + INVERSE_MASS[j];
        if (distance < 1e-9 || weight == 0) return;
        double correction = (distance - length) / distance * stiffness / weight;
        double a = correction * INVERSE_MASS[i], b = correction * INVERSE_MASS[j];
        x[i] += dx * a; y[i] += dy * a; z[i] += dz * a;
        x[j] -= dx * b; y[j] -= dy * b; z[j] -= dz * b;
    }

    private void keepOutsideGrip(double alpha) {
        double a = oldHandleAngle + (handleAngle - oldHandleAngle) * alpha, b = oldHandleYaw + (handleYaw - oldHandleYaw) * alpha;
        double c = Math.cos(a), s = Math.sin(a), cy = Math.cos(b), sy = Math.sin(b);
        double hx = oldHandX + (handX-oldHandX)*alpha, hy = oldHandY + (handY-oldHandY)*alpha, hz = oldHandZ + (handZ-oldHandZ)*alpha;
        for (int i = 2; i < POINTS; i++) {
            double px = (x[i]-hx) * cy - (z[i]-hz) * sy, rz = (x[i]-hx) * sy + (z[i]-hz) * cy;
            double py = (y[i]-hy) * c + rz * s, pz = -(y[i]-hy) * s + rz * c, half = .125;
            if (Math.abs(px) < half && py > -.09 && py < GRIP_LENGTH + .06 && Math.abs(pz) < half) {
                if (half - Math.abs(px) < half - Math.abs(pz)) px = Math.copySign(half, px == 0 ? 1 : px);
                else pz = Math.copySign(half, pz == 0 ? 1 : pz);
                double ry = py * c - pz * s; rz = py * s + pz * c;
                x[i] = hx + px * cy + rz * sy; y[i] = hy + ry; z[i] = hz - px * sy + rz * cy;
            }
        }
    }

    private static double smooth(double t) { return t * t * (3 - 2 * t); }
    private void keepAboveGround() {
        for (int i=2;i<POINTS;i++) {
            if (Double.isNaN(ground[i])) continue;
            double penetration = .025 - (y[i]*groundY + z[i]*groundZ + ground[i]);
            if (penetration > 0) {
                y[i] += penetration*groundY; z[i] += penetration*groundZ;
                // Remove only incoming normal velocity; preserve tangential motion of the travelling loop.
                double velocity = (y[i]-oldY[i])*groundY + (z[i]-oldZ[i])*groundZ;
                if (velocity < 0) { oldY[i] += velocity*groundY; oldZ[i] += velocity*groundZ; }
            }
        }
    }
    private void snapshot() {
        System.arraycopy(x, 0, renderX, 0, POINTS); System.arraycopy(y, 0, renderY, 0, POINTS); System.arraycopy(z, 0, renderZ, 0, POINTS);
    }
    public double x(int i, double partial) { return renderX[i] + (x[i] - renderX[i]) * partial; }
    public double y(int i, double partial) { return renderY[i] + (y[i] - renderY[i]) * partial; }
    public double z(int i, double partial) { return renderZ[i] + (z[i] - renderZ[i]) * partial; }
    public double handleAngle(double partial) { return oldHandleAngle + (handleAngle - oldHandleAngle) * partial; }
    public double handleYaw(double partial) { return oldHandleYaw + (handleYaw - oldHandleYaw) * partial; }
    public double handX(double partial) { return oldHandX + (handX-oldHandX)*partial; }
    public double handY(double partial) { return oldHandY + (handY-oldHandY)*partial; }
    public double handZ(double partial) { return oldHandZ + (handZ-oldHandZ)*partial; }
    public int simulatedTicks() { return simulatedTicks; }
    public boolean isReleased() { return released; }
    public static double restX(int i) { return REST_X[i]; }
    public static double restY(int i) { return REST_Y[i]; }
    public static double restZ(int i) { return REST_Z[i]; }
    public static double length(int i) { return LENGTH[i]; }
}
