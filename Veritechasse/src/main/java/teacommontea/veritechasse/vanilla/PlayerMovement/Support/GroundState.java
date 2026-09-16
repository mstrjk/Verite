package teacommontea.veritechasse.Vanilla.PlayerMovement.Support;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class GroundState {

    public static final boolean CLIENT_CLAIM_IS_AUTHORITATIVE = false;

    public static final float COLLISION_EPSILON_SOURCE = 1.0E-5F;

    public static final double COLLISION_EPSILON = COLLISION_EPSILON_SOURCE;

    public static final double SUPPORT_SEARCH_DEPTH = 1.0E-6D;

    public static final int SUPPORTING_BLOCK_MAJOR = 1;
    public static final int SUPPORTING_BLOCK_MINOR = 20;
    public static final int SUPPORTING_BLOCK_PATCH = 0;

    public static final int HORIZONTAL_COLLISION_FLAG_MAJOR = 1;
    public static final int HORIZONTAL_COLLISION_FLAG_MINOR = 21;
    public static final int HORIZONTAL_COLLISION_FLAG_PATCH = 2;

    private GroundState() {
    }

    public static boolean supportingBlockProbeExists(Protocol protocol) {
        return protocol.atLeast(
            SUPPORTING_BLOCK_MAJOR, SUPPORTING_BLOCK_MINOR, SUPPORTING_BLOCK_PATCH);
    }

    public static boolean carriesHorizontalCollisionFlag(Protocol protocol) {
        return protocol.atLeast(
            HORIZONTAL_COLLISION_FLAG_MAJOR,
            HORIZONTAL_COLLISION_FLAG_MINOR,
            HORIZONTAL_COLLISION_FLAG_PATCH);
    }

    public static boolean movedVertically(double attemptedY) {
        return Math.abs(attemptedY) > 0.0D;
    }

    public static boolean groundStateIsUpdated(double attemptedY, boolean locallyAuthoritative) {
        return movedVertically(attemptedY) || locallyAuthoritative;
    }

    public static boolean verticalCollision(double attemptedY, double resolvedY) {
        return attemptedY != resolvedY;
    }

    public static boolean verticalCollisionBelow(double attemptedY, double resolvedY) {
        return verticalCollision(attemptedY, resolvedY) && attemptedY < 0.0D;
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

    public static boolean onGroundAfterMove(
            boolean previousOnGround,
            double attemptedY,
            double resolvedY,
            boolean locallyAuthoritative) {
        if (!groundStateIsUpdated(attemptedY, locallyAuthoritative)) {
            return previousOnGround;
        }
        return verticalCollisionBelow(attemptedY, resolvedY);
    }

    public static double probeMinY(double boundingBoxMinY) {
        return boundingBoxMinY - SUPPORT_SEARCH_DEPTH;
    }

    public static boolean serverTrustsClientClaim() {
        return true;
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
