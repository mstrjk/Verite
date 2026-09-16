package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientLatencyForgiveness;

import teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerPing.KeepAlive;

public final class PingProbe {

    public static final String KEY = "ping_probe";

    public static final long ROUND_TRIP_TIMEOUT_MILLIS = KeepAlive.KEEPALIVE_INTERVAL_MILLIS;

    public static final int RESPONSIVE_LATENCY_MILLIS = 400;

    private PingProbe() {
    }

    public static boolean challengeIsOutstanding(boolean keepAlivePending) {
        return keepAlivePending;
    }

    public static boolean respondedWithinWindow(
            boolean keepAlivePending, long millisSinceSent) {
        if (keepAlivePending) {
            return false;
        }
        return millisSinceSent < ROUND_TRIP_TIMEOUT_MILLIS;
    }

    public static boolean connectionProvenAlive(
            boolean keepAlivePending, long millisSinceLastResponse) {
        return !keepAlivePending
            && millisSinceLastResponse < ROUND_TRIP_TIMEOUT_MILLIS;
    }

    public static boolean lossIsCorroborated(
            boolean keepAlivePending,
            long millisSinceSent,
            long ticksSinceLastMovePacket) {
        if (ticksSinceLastMovePacket <= 0L) {
            return false;
        }
        return challengeIsOutstanding(keepAlivePending) && millisSinceSent > 0L;
    }

    public static final long REMINDER_TICKS = 20L;

    public static boolean silenceExceedsReminder(long ticksSinceLastMovePacket) {
        return ticksSinceLastMovePacket > REMINDER_TICKS;
    }

    public static boolean silenceIsContradicted(
            boolean keepAlivePending,
            long millisSinceLastResponse,
            long ticksSinceLastMovePacket) {
        if (!silenceExceedsReminder(ticksSinceLastMovePacket)) {
            return false;
        }
        return connectionProvenAlive(keepAlivePending, millisSinceLastResponse);
    }

    public static boolean movementSilenceWhileAnsweringPings(
            boolean keepAlivePending,
            long millisSinceLastResponse,
            long ticksSinceLastMovePacket) {
        return silenceIsContradicted(
            keepAlivePending, millisSinceLastResponse, ticksSinceLastMovePacket);
    }

    public static boolean smoothedLatencyHidesSpikes() {
        return true;
    }

    public static boolean latencyAloneProvesLoss() {
        return false;
    }

    public static boolean roundTripIsServerTimed() {
        return true;
    }
}
