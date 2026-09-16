package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientLatencyForgiveness;

import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness.Verdict;

public final class ClientHealth {

    public static final String KEY = "client_health";

    public static final int KEEPALIVE_MISSES_BEFORE_DEGRADED = 1;

    public static final long SILENCE_TICKS_BEFORE_DEGRADED = 20L;

    public static final long SILENCE_TICKS_BEFORE_UNUSABLE = 100L;

    private ClientHealth() {
    }

    public static boolean clientCanInfluence() {
        return true;
    }

    public static boolean silent(long ticksSinceLastPacket) {
        return ticksSinceLastPacket >= SILENCE_TICKS_BEFORE_DEGRADED;
    }

    public static boolean unusable(long ticksSinceLastPacket) {
        return ticksSinceLastPacket >= SILENCE_TICKS_BEFORE_UNUSABLE;
    }

    public static boolean keepAliveOutstanding(boolean keepAlivePending, long ticksSincePending) {
        return keepAlivePending && ticksSincePending > 0L;
    }

    public static boolean degraded(
            long ticksSinceLastPacket,
            boolean keepAlivePending,
            long ticksSincePending) {
        return silent(ticksSinceLastPacket)
            || keepAliveOutstanding(keepAlivePending, ticksSincePending);
    }

    public static String route(
            long ticksSinceLastPacket,
            boolean keepAlivePending,
            long ticksSincePending) {
        if (degraded(ticksSinceLastPacket, keepAlivePending, ticksSincePending)) {
            return Verdict.DISCARD;
        }
        return Verdict.EVALUATE;
    }

    public static boolean baselineIsStale(long ticksSinceLastPacket) {
        return silent(ticksSinceLastPacket);
    }

    public static boolean degradationGrantsMovementBudget() {
        return false;
    }

    public static boolean silenceIsFreeDistance() {
        return false;
    }

    public static boolean latencyValueIsUsedAsEvidence() {
        return false;
    }

    public static boolean degradationIsSelfReported() {
        return false;
    }
}
