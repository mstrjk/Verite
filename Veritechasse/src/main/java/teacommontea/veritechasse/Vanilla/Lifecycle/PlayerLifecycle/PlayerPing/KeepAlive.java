package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerPing;

public final class KeepAlive {

    public static final String KEY = "keep_alive";

    public static final long KEEPALIVE_INTERVAL_MILLIS = 15000L;

    public static final int LATENCY_SMOOTHING_WEIGHT = 3;

    public static final int LATENCY_SMOOTHING_DIVISOR = 4;

    private KeepAlive() {
    }

    public static boolean challengeDue(long nowMillis, long keepAliveTimeMillis) {
        return nowMillis - keepAliveTimeMillis >= KEEPALIVE_INTERVAL_MILLIS;
    }

    public static boolean timedOut(boolean keepAlivePending, long nowMillis, long keepAliveTimeMillis) {
        return keepAlivePending && challengeDue(nowMillis, keepAliveTimeMillis);
    }

    public static boolean responseMatches(long packetId, long challenge) {
        return packetId == challenge;
    }

    public static int smoothedLatency(int previousLatency, int sampleMillis) {
        return (previousLatency * LATENCY_SMOOTHING_WEIGHT + sampleMillis)
            / LATENCY_SMOOTHING_DIVISOR;
    }

    public static boolean latencyIsInstantaneous() {
        return false;
    }

    public static int samplesToConverge() {
        return LATENCY_SMOOTHING_DIVISOR;
    }

    public static double weightOfNewestSample() {
        return 1.0D / (double) LATENCY_SMOOTHING_DIVISOR;
    }

    public static boolean singleplayerOwnerExempt(boolean isSingleplayerOwner) {
        return isSingleplayerOwner;
    }

    public static int latencyTicks(int latencyMillis, double millisecondsPerTick) {
        if (millisecondsPerTick <= 0.0D) {
            return 0;
        }
        return (int) Math.ceil((double) latencyMillis / millisecondsPerTick);
    }
}
