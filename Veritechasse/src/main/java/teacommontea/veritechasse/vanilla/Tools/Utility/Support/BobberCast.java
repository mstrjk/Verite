package teacommontea.veritechasse.Vanilla.Tools.Utility.Support;

import teacommontea.veritechasse.Vanilla.Reality;

public final class BobberCast {

    public static final double MAX_DISTANCE_SQR = 1024.0D;
    public static final double MAX_DISTANCE = 32.0D;

    public static final float VERTICAL_CLAMP = 5.0F;
    public static final double SPEED_NUMERATOR = 0.6D;
    public static final double JITTER_CENTRE = 0.5D;
    public static final double JITTER_SPREAD = 0.0103365D;

    public static final double SPAWN_OFFSET = 0.3D;

    public static final double GRAVITY_PER_TICK = 0.03D;
    public static final double AIR_INERTIA = 0.92D;
    public static final double WATER_SURFACE_DRAG_XZ = 0.9D;
    public static final double LAND_DRAG_XZ = 0.3D;
    public static final double LAND_DRAG_Y = 0.2D;

    public static final int MAX_OUT_OF_WATER_TIME = 10;

    private BobberCast() {
    }

    public static double verticalComponent(double xSin, double xCos) {
        double raw = -(xSin / xCos);
        if (raw < -VERTICAL_CLAMP) {
            return -VERTICAL_CLAMP;
        }
        return raw > VERTICAL_CLAMP ? VERTICAL_CLAMP : raw;
    }

    public static double speedScale(double vectorLength, double jitter) {
        return SPEED_NUMERATOR / vectorLength + jitter;
    }

    public static double minJitter() {
        return JITTER_CENTRE - JITTER_SPREAD;
    }

    public static double maxJitter() {
        return JITTER_CENTRE + JITTER_SPREAD;
    }

    public static double maxLaunchSpeed() {
        double unitVector = 1.0D;
        return (SPEED_NUMERATOR / unitVector + maxJitter()) * Math.sqrt(1.0D + VERTICAL_CLAMP * VERTICAL_CLAMP + 1.0D);
    }

    public static boolean withinLeash(double distanceSqr) {
        return distanceSqr <= MAX_DISTANCE_SQR;
    }

    public static boolean despawns(double distanceSqr, boolean holdingRod) {
        if (!holdingRod) {
            return true;
        }
        return !withinLeash(distanceSqr);
    }

    public static Reality maxDistance() {
        return Reality.of(MAX_DISTANCE);
    }

    public static Reality distanceFrom(double observedDistance) {
        if (observedDistance > MAX_DISTANCE) {
            return Reality.impossible();
        }
        return Reality.of(observedDistance);
    }
}
