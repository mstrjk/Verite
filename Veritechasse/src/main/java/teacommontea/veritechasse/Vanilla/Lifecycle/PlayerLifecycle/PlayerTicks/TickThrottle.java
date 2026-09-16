package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerTicks;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class TickThrottle {

    public static final String KEY = "tick_throttle";

    public static final int THROTTLER_CLASS_MAJOR = 1;
    public static final int THROTTLER_CLASS_MINOR = 21;
    public static final int THROTTLER_CLASS_PATCH = 2;

    public static final int INCREMENT_STEP = 20;

    public static final int DROP_THRESHOLD = 200;

    public static final int CHAT_THRESHOLD_LEGACY = 200;

    public static final int DEFAULT_CHAT_SPAM_THRESHOLD_SECONDS = 10;

    private TickThrottle() {
    }

    public static boolean usesThrottlerClass(Protocol protocol) {
        return protocol.atLeast(
            THROTTLER_CLASS_MAJOR, THROTTLER_CLASS_MINOR, THROTTLER_CLASS_PATCH);
    }

    public static int afterIncrement(int count) {
        return count + INCREMENT_STEP;
    }

    public static int afterTick(int count) {
        return count > 0 ? count - 1 : count;
    }

    public static boolean isUnderThreshold(int count, int threshold) {
        return threshold <= 0 || count < threshold;
    }

    public static int chatThreshold(int chatSpamThresholdSeconds, Protocol protocol) {
        if (!usesThrottlerClass(protocol)) {
            return CHAT_THRESHOLD_LEGACY;
        }
        return INCREMENT_STEP * chatSpamThresholdSeconds;
    }

    public static boolean chatIsUnderThreshold(
            int count, int chatSpamThresholdSeconds, Protocol protocol) {
        return isUnderThreshold(count, chatThreshold(chatSpamThresholdSeconds, protocol));
    }

    public static boolean dropIsUnderThreshold(int count) {
        return isUnderThreshold(count, DROP_THRESHOLD);
    }

    public static int sustainableEventsPerTick() {
        return 1;
    }

    public static int ticksToDrain(int count) {
        return count <= 0 ? 0 : count;
    }

    public static int maximumBurst(int threshold) {
        if (threshold <= 0) {
            return Integer.MAX_VALUE;
        }
        return (threshold + INCREMENT_STEP - 1) / INCREMENT_STEP;
    }

    public static boolean throttlerDrainsOnlyWhenPlayerTicks() {
        return true;
    }
}
