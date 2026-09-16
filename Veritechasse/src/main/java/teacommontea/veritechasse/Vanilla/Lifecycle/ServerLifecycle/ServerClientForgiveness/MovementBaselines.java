package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness;

public final class MovementBaselines {

    public static final String KEY = "movement_baselines";

    private MovementBaselines() {
    }

    public static boolean firstGoodIsResetEveryTick() {
        return true;
    }

    public static boolean lastGoodAdvancesOnlyOnAcceptance() {
        return true;
    }

    public static double speedCheckDistanceSquared(
            double targetX,
            double targetY,
            double targetZ,
            double firstGoodX,
            double firstGoodY,
            double firstGoodZ) {
        double dx = targetX - firstGoodX;
        double dy = targetY - firstGoodY;
        double dz = targetZ - firstGoodZ;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double appliedDeltaX(double targetX, double lastGoodX) {
        return targetX - lastGoodX;
    }

    public static double appliedDeltaY(double targetY, double lastGoodY) {
        return targetY - lastGoodY;
    }

    public static double appliedDeltaZ(double targetZ, double lastGoodZ) {
        return targetZ - lastGoodZ;
    }

    public static boolean sameMoveIsMeasuredTwice() {
        return true;
    }

    public static boolean baselineResetsOnTeleportAcknowledgement() {
        return true;
    }

    public static boolean lastGoodFollowsServerPositionNotClientClaim() {
        return true;
    }

    public static boolean accumulatesWithinTick(int packetsThisTick) {
        return packetsThisTick > 1;
    }
}
