package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid;

public final class WaterVertical {

    public static final String KEY = "water_vertical";

    public static final float VERTICAL_DRAG = 0.8F;

    public static final float SWIM_UP_IMPULSE = 0.04F;

    public static final float SWIM_DOWN_IMPULSE = -0.04F;

    public static final double GRAVITY_DIVISOR = 16.0D;

    public static final double STATIONARY_TARGET = 0.005D;

    public static final double STATIONARY_EPSILON = 0.003D;

    public static final double STATIONARY_FALL = -0.003D;

    public static final float JUMP_OUT_IMPULSE = 0.3F;

    private WaterVertical() {
    }

    public static double effectiveGravity(double baseGravity) {
        return baseGravity / GRAVITY_DIVISOR;
    }

    public static double afterDrag(double deltaY) {
        return deltaY * (double) VERTICAL_DRAG;
    }

    public static boolean gravityApplies(double baseGravity, boolean sprinting) {
        return baseGravity != 0.0D && !sprinting;
    }

    public static boolean clampsToStationaryFall(double deltaY, double baseGravity, boolean falling) {
        if (!falling) {
            return false;
        }
        return Math.abs(deltaY - STATIONARY_TARGET) >= STATIONARY_EPSILON
            && Math.abs(deltaY - effectiveGravity(baseGravity)) < STATIONARY_EPSILON;
    }

    public static double afterGravity(
            double deltaY, double baseGravity, boolean falling, boolean sprinting) {
        if (!gravityApplies(baseGravity, sprinting)) {
            return deltaY;
        }
        if (clampsToStationaryFall(deltaY, baseGravity, falling)) {
            return STATIONARY_FALL;
        }
        return deltaY - effectiveGravity(baseGravity);
    }

    public static double next(
            double deltaY,
            double baseGravity,
            boolean jumpHeld,
            boolean sneakHeld,
            boolean sprinting) {
        double dragged = afterDrag(deltaY);
        if (jumpHeld) {
            dragged = dragged + (double) SWIM_UP_IMPULSE;
        }
        if (sneakHeld) {
            dragged = dragged + (double) SWIM_DOWN_IMPULSE;
        }
        boolean falling = deltaY <= 0.0D;
        return afterGravity(dragged, baseGravity, falling, sprinting);
    }

    public static double maximumRise(double deltaY, double baseGravity, boolean sprinting) {
        return next(deltaY, baseGravity, true, false, sprinting);
    }

    public static double minimumFall(double deltaY, double baseGravity, boolean sprinting) {
        return next(deltaY, baseGravity, false, true, sprinting);
    }

    public static double terminalRise(double baseGravity) {
        double impulse = (double) SWIM_UP_IMPULSE - effectiveGravity(baseGravity);
        return impulse / (1.0D - (double) VERTICAL_DRAG);
    }

    public static boolean permitsRise(
            double observedDeltaY,
            double previousDeltaY,
            double baseGravity,
            boolean sprinting,
            double tolerance) {
        return observedDeltaY <= maximumRise(previousDeltaY, baseGravity, sprinting) + tolerance;
    }
}
