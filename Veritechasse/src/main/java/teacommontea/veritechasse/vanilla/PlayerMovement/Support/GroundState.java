package teacommontea.veritechasse.vanilla.PlayerMovement.Support;

import teacommontea.veritechasse.vanilla.Reality;

public final class GroundState {

    public static final boolean CLIENT_CLAIM_IS_AUTHORITATIVE = false;

    public static final double COLLISION_EPSILON = 1.0E-7D;

    public static final double SUPPORT_SEARCH_DEPTH = 1.0E-3D;

    private GroundState() {
    }

    public static boolean verticalCollisionBelow(double attemptedY, double resolvedY) {
        if (attemptedY >= 0.0D) {
            return false;
        }
        return !equal(attemptedY, resolvedY);
    }

    public static boolean horizontalCollision(
            double attemptedX,
            double resolvedX,
            double attemptedZ,
            double resolvedZ) {
        return !equal(attemptedX, resolvedX) || !equal(attemptedZ, resolvedZ);
    }

    public static boolean onGround(double attemptedY, double resolvedY) {
        return verticalCollisionBelow(attemptedY, resolvedY);
    }

    public static boolean onGround(boolean supportingBlockPresent, double attemptedY, double resolvedY) {
        if (!supportingBlockPresent) {
            return false;
        }
        return onGround(attemptedY, resolvedY);
    }

    public static boolean claimIsConsistent(boolean clientClaim, double attemptedY, double resolvedY) {
        return clientClaim == onGround(attemptedY, resolvedY);
    }

    public static boolean claimIsFalsified(boolean clientClaim, double attemptedY, double resolvedY) {
        return clientClaim && !onGround(attemptedY, resolvedY);
    }

    public static boolean groundPhysicsApply(double attemptedY, double resolvedY) {
        return onGround(attemptedY, resolvedY);
    }

    public static boolean airPhysicsApply(double attemptedY, double resolvedY) {
        return !onGround(attemptedY, resolvedY);
    }

    public static boolean equal(double a, double b) {
        return Math.abs(b - a) < COLLISION_EPSILON;
    }

    public static Reality groundedFrom(boolean clientClaim, double attemptedY, double resolvedY) {
        if (claimIsFalsified(clientClaim, attemptedY, resolvedY)) {
            return Reality.impossible();
        }
        return Reality.of(onGround(attemptedY, resolvedY) ? 1.0D : 0.0D);
    }
}
