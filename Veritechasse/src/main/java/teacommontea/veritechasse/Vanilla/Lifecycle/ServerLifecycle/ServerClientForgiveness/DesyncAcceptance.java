package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness;

public final class DesyncAcceptance {

    public static final String KEY = "desync_acceptance";

    public static final double COLLISION_DEFLATE_SOURCE = 1.0E-5F;

    public static final double COLLISION_DEFLATE = COLLISION_DEFLATE_SOURCE;

    private DesyncAcceptance() {
    }

    public static boolean accepted(
            boolean noPhysics,
            boolean sleeping,
            boolean failedDistanceGate,
            boolean oldPositionWasFree,
            boolean collidingWithSomethingNew) {
        if (noPhysics || sleeping) {
            return true;
        }
        if (collidingWithSomethingNew) {
            return false;
        }
        return !failedDistanceGate || !oldPositionWasFree;
    }

    public static boolean rejected(
            boolean noPhysics,
            boolean sleeping,
            boolean failedDistanceGate,
            boolean oldPositionWasFree,
            boolean collidingWithSomethingNew) {
        return !accepted(noPhysics, sleeping, failedDistanceGate,
            oldPositionWasFree, collidingWithSomethingNew);
    }

    public static boolean gateFailureAloneRejectsFromFreeSpace() {
        return true;
    }

    public static boolean passingTheGateCanStillReject() {
        return true;
    }

    public static boolean forgivenBecauseAlreadyStuck(
            boolean failedDistanceGate,
            boolean oldPositionWasFree,
            boolean collidingWithSomethingNew) {
        return failedDistanceGate
            && !oldPositionWasFree
            && !collidingWithSomethingNew;
    }

    public static boolean failingTheGateFromFreeSpaceRejects(
            boolean failedDistanceGate, boolean oldPositionWasFree) {
        return failedDistanceGate && oldPositionWasFree;
    }

    public static boolean rejectionRewindsToStartOfTick() {
        return true;
    }

    public static boolean rejectionStillChecksFallDamage() {
        return true;
    }

    public static boolean rejectionDiscardsMovementRecording() {
        return true;
    }

    public static boolean vehicleUsesSameRule() {
        return true;
    }

    public static boolean vehicleAccepted(
            boolean failedDistanceGate,
            boolean oldPositionWasFree,
            boolean collidingWithSomethingNew) {
        return !(failedDistanceGate && oldPositionWasFree)
            && !collidingWithSomethingNew;
    }
}
