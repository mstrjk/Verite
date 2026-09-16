package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientLatencyForgiveness;

import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents.MovedTooQuickly;

public final class LagAbsorption {

    public static final String KEY = "lag_absorption";

    private LagAbsorption() {
    }

    public static boolean absorbedByPacketBatching(int deltaPackets) {
        return deltaPackets > 1 && !MovedTooQuickly.packetRateExceeded(deltaPackets);
    }

    public static double allowanceForBatchedPackets(boolean fallFlying, int deltaPackets) {
        return MovedTooQuickly.allowance(fallFlying, deltaPackets);
    }

    public static boolean burstIsPunishedRatherThanForgiven(int deltaPackets) {
        return MovedTooQuickly.packetRateExceeded(deltaPackets);
    }

    public static boolean skippedWhileTickRateAbnormal(boolean runsNormally) {
        return !runsNormally;
    }

    public static boolean forgivenByExistingOverlap(
            boolean oldBoxAlreadyIntersected) {
        return oldBoxAlreadyIntersected;
    }

    public static boolean forgivenWhileAwaitingTeleport(boolean awaitingTeleport) {
        return awaitingTeleport;
    }

    public static boolean forgivenWhileChangingDimension(boolean changingDimension) {
        return changingDimension;
    }

    public static boolean anyAbsorptionApplies(
            boolean runsNormally,
            boolean awaitingTeleport,
            boolean changingDimension,
            boolean oldBoxAlreadyIntersected) {
        return skippedWhileTickRateAbnormal(runsNormally)
            || forgivenWhileAwaitingTeleport(awaitingTeleport)
            || forgivenWhileChangingDimension(changingDimension)
            || forgivenByExistingOverlap(oldBoxAlreadyIntersected);
    }

    public static boolean absorptionIsStateBasedNotTimeBased() {
        return true;
    }

    public static boolean anyGracePeriodIsGrantedForLag() {
        return false;
    }

    public static boolean impulseGraceIsTheOnlyTickCountedGrace() {
        return true;
    }

    public static boolean impulseGraceIsGrantedByLagOrLatency() {
        return false;
    }
}
