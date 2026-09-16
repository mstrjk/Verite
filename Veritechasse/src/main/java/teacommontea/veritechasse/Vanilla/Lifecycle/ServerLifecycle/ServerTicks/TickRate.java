package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class TickRate {

    public static final String KEY = "tick_rate";

    public static final int VARIABLE_RATE_MAJOR = 1;
    public static final int VARIABLE_RATE_MINOR = 20;
    public static final int VARIABLE_RATE_PATCH = 3;

    public static final float DEFAULT_TICKRATE = 20.0F;

    public static final float MINIMUM_TICKRATE = 1.0F;

    public static final long NANOSECONDS_PER_SECOND = 1000000000L;

    public static final long NANOSECONDS_PER_MILLISECOND = 1000000L;

    public static final long DEFAULT_NANOSECONDS_PER_TICK =
        NANOSECONDS_PER_SECOND / 20L;

    public static final long LEGACY_MILLISECONDS_PER_TICK = 50L;

    private TickRate() {
    }

    public static boolean rateIsVariable(Protocol protocol) {
        return protocol.atLeast(
            VARIABLE_RATE_MAJOR, VARIABLE_RATE_MINOR, VARIABLE_RATE_PATCH);
    }

    public static float clampRate(float requested) {
        return Math.max(requested, MINIMUM_TICKRATE);
    }

    public static long nanosecondsPerTick(float tickrate) {
        return (long) ((double) NANOSECONDS_PER_SECOND / (double) clampRate(tickrate));
    }

    public static long nanosecondsPerTick(float tickrate, Protocol protocol) {
        if (!rateIsVariable(protocol)) {
            return LEGACY_MILLISECONDS_PER_TICK * NANOSECONDS_PER_MILLISECOND;
        }
        return nanosecondsPerTick(tickrate);
    }

    public static float millisecondsPerTick(float tickrate) {
        return (float) nanosecondsPerTick(tickrate) / (float) NANOSECONDS_PER_MILLISECOND;
    }

    public static float effectiveRate(float tickrate, Protocol protocol) {
        return rateIsVariable(protocol) ? clampRate(tickrate) : DEFAULT_TICKRATE;
    }

    public static boolean rateIsDefault(float tickrate) {
        return Float.compare(clampRate(tickrate), DEFAULT_TICKRATE) == 0;
    }

    public static double ticksPerRealSecond(float tickrate, Protocol protocol) {
        return (double) NANOSECONDS_PER_SECOND
            / (double) nanosecondsPerTick(tickrate, protocol);
    }

    public static double realMillisecondsForTicks(long ticks, float tickrate, Protocol protocol) {
        return (double) (ticks * nanosecondsPerTick(tickrate, protocol))
            / (double) NANOSECONDS_PER_MILLISECOND;
    }

    public static double expectedTicksInRealMillis(
            double realMillis, float tickrate, Protocol protocol) {
        double nanos = realMillis * (double) NANOSECONDS_PER_MILLISECOND;
        return nanos / (double) nanosecondsPerTick(tickrate, protocol);
    }
}
