package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class TickOverload {

    public static final String KEY = "tick_overload";

    public static final int NANOSECOND_LOOP_MAJOR = 1;
    public static final int NANOSECOND_LOOP_MINOR = 20;
    public static final int NANOSECOND_LOOP_PATCH = 3;

    public static final long OVERLOADED_THRESHOLD_NANOS =
        20L * TickRate.NANOSECONDS_PER_SECOND / 20L;

    public static final int OVERLOADED_TICKS_THRESHOLD = 20;

    public static final int MAX_TICK_LATENCY = 3;

    public static final long LEGACY_THRESHOLD_MILLIS = 2000L;

    public static final long LEGACY_WARNING_INTERVAL_MILLIS = 15000L;

    private TickOverload() {
    }

    public static boolean loopUsesNanoseconds(Protocol protocol) {
        return protocol.atLeast(
            NANOSECOND_LOOP_MAJOR, NANOSECOND_LOOP_MINOR, NANOSECOND_LOOP_PATCH);
    }

    public static long overloadThresholdNanos(long nanosecondsPerTick, Protocol protocol) {
        if (!loopUsesNanoseconds(protocol)) {
            return LEGACY_THRESHOLD_MILLIS * TickRate.NANOSECONDS_PER_MILLISECOND;
        }
        return OVERLOADED_THRESHOLD_NANOS
            + (long) OVERLOADED_TICKS_THRESHOLD * nanosecondsPerTick;
    }

    public static boolean isOverloaded(
            long behindTimeNanos, long nanosecondsPerTick, Protocol protocol) {
        return behindTimeNanos > overloadThresholdNanos(nanosecondsPerTick, protocol);
    }

    public static long skippedTicks(
            long behindTimeNanos, long nanosecondsPerTick, Protocol protocol) {
        if (!isOverloaded(behindTimeNanos, nanosecondsPerTick, protocol)) {
            return 0L;
        }
        if (nanosecondsPerTick <= 0L) {
            return 0L;
        }
        return behindTimeNanos / nanosecondsPerTick;
    }

    public static boolean ticksWereSkipped(
            long behindTimeNanos, long nanosecondsPerTick, Protocol protocol) {
        return skippedTicks(behindTimeNanos, nanosecondsPerTick, protocol) > 0L;
    }

    public static long serverTicksForElapsedRealTime(
            long elapsedNanos, long nanosecondsPerTick) {
        if (nanosecondsPerTick <= 0L) {
            return 0L;
        }
        return elapsedNanos / nanosecondsPerTick;
    }

    public static boolean elapsedTimeUnderestimatesTicks(
            long behindTimeNanos, long nanosecondsPerTick, Protocol protocol) {
        return ticksWereSkipped(behindTimeNanos, nanosecondsPerTick, protocol);
    }

    public static boolean timerEvidenceIsValid(
            long behindTimeNanos, long nanosecondsPerTick, Protocol protocol) {
        return !ticksWereSkipped(behindTimeNanos, nanosecondsPerTick, protocol);
    }
}
