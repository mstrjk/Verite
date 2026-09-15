package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerClimb;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class ClimbReality {

    public static final double TOLERANCE = 1.0E-9D;

    private ClimbReality() {
    }

    public static boolean permitsHorizontal(double observedComponent) {
        return Math.abs(observedComponent) <= ClimbMotion.CLAMP + TOLERANCE;
    }

    public static boolean permitsAscent(
            double observedDeltaY,
            boolean horizontalCollision,
            boolean jumping) {
        double bound = ClimbMotion.maximumAscent(horizontalCollision, jumping);
        return observedDeltaY <= bound + TOLERANCE;
    }

    public static boolean permitsDescent(double observedDeltaY) {
        return observedDeltaY >= -ClimbMotion.CLAMP - TOLERANCE;
    }

    public static boolean permitsVertical(
            double observedDeltaY,
            boolean horizontalCollision,
            boolean jumping) {
        return permitsAscent(observedDeltaY, horizontalCollision, jumping)
            && permitsDescent(observedDeltaY);
    }

    public static boolean descendsWhileSuppressed(
            double observedDeltaY,
            String blockAtFeet,
            boolean shiftKeyDown,
            boolean isPlayer) {
        if (!ClimbMotion.slideIsSuppressed(blockAtFeet, shiftKeyDown, isPlayer)) {
            return false;
        }
        return observedDeltaY < -TOLERANCE;
    }

    public static boolean climbsWithoutClimbable(
            double observedDeltaY,
            boolean onClimbable,
            boolean onGround) {
        if (onClimbable || onGround) {
            return false;
        }
        return observedDeltaY > TOLERANCE;
    }

    public static double fallDistanceAfter(boolean onClimbable, double currentFallDistance) {
        return onClimbable ? 0.0D : currentFallDistance;
    }

    public static boolean glidingIgnoresClimbable(
            boolean fallFlying,
            boolean blockAllowsGlideThrough,
            Protocol protocol) {
        if (!fallFlying) {
            return false;
        }
        return ClimbMotion.glideThroughApplies(protocol) && blockAllowsGlideThrough;
    }

    public static Reality horizontalFrom() {
        return ClimbMotion.horizontalFrom();
    }

    public static Reality ascentFrom(boolean horizontalCollision, boolean jumping) {
        return ClimbMotion.ascentFrom(horizontalCollision, jumping);
    }
}
