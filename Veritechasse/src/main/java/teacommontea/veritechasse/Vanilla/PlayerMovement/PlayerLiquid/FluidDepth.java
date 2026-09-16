package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid;

public final class FluidDepth {

    public static final String KEY = "fluid_depth";

    public static final double JUMP_THRESHOLD = 0.4D;

    public static final double SHORT_ENTITY_EYE_HEIGHT = 0.4D;

    public static final double NO_THRESHOLD = 0.0D;

    private FluidDepth() {
    }

    public static double jumpThreshold(double eyeHeight) {
        return eyeHeight < SHORT_ENTITY_EYE_HEIGHT ? NO_THRESHOLD : JUMP_THRESHOLD;
    }

    public static boolean isShallow(double fluidHeight, double eyeHeight) {
        return fluidHeight <= jumpThreshold(eyeHeight);
    }

    public static boolean isShallow(double fluidHeight) {
        return fluidHeight <= JUMP_THRESHOLD;
    }

    public static boolean touchesFluid(double fluidHeight) {
        return fluidHeight > 0.0D;
    }

    public static boolean swimPhysicsApply(double fluidHeight, double eyeHeight) {
        return touchesFluid(fluidHeight) && !isShallow(fluidHeight, eyeHeight);
    }

    public static boolean groundPhysicsApply(double fluidHeight, double eyeHeight) {
        return !swimPhysicsApply(fluidHeight, eyeHeight);
    }

    public static boolean reportsInWaterButUsesGroundPhysics(
            boolean touchingWater, double fluidHeight, double eyeHeight) {
        return touchingWater && isShallow(fluidHeight, eyeHeight);
    }

    public static boolean jumpIsPossible(double fluidHeight, double eyeHeight) {
        return isShallow(fluidHeight, eyeHeight);
    }

    public static boolean currentStillApplies(boolean touchingWater) {
        return touchingWater;
    }
}
