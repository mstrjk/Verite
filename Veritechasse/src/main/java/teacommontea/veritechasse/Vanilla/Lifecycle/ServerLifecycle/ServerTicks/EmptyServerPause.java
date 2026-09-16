package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class EmptyServerPause {

    public static final String KEY = "empty_server_pause";

    public static final int PAUSE_EXISTS_MAJOR = 1;
    public static final int PAUSE_EXISTS_MINOR = 21;
    public static final int PAUSE_EXISTS_PATCH = 9;

    public static final int TICKS_PER_SECOND = 20;

    public static final int DISABLED = 0;

    private EmptyServerPause() {
    }

    public static boolean pauseExists(Protocol protocol) {
        return protocol.atLeast(
            PAUSE_EXISTS_MAJOR, PAUSE_EXISTS_MINOR, PAUSE_EXISTS_PATCH);
    }

    public static boolean pauseEnabled(int pauseWhenEmptySeconds, Protocol protocol) {
        return pauseExists(protocol) && pauseWhenEmptySeconds > DISABLED;
    }

    public static int thresholdTicks(int pauseWhenEmptySeconds) {
        return pauseWhenEmptySeconds * TICKS_PER_SECOND;
    }

    public static boolean accumulatesEmptyTicks(int playerCount, boolean sprinting) {
        return playerCount == 0 && !sprinting;
    }

    public static int afterTick(int emptyTicks, int playerCount, boolean sprinting) {
        return accumulatesEmptyTicks(playerCount, sprinting) ? emptyTicks + 1 : 0;
    }

    public static boolean isPaused(
            int emptyTicks, int pauseWhenEmptySeconds, Protocol protocol) {
        if (!pauseEnabled(pauseWhenEmptySeconds, protocol)) {
            return false;
        }
        return emptyTicks >= thresholdTicks(pauseWhenEmptySeconds);
    }

    public static boolean tickCountAdvances(
            int emptyTicks, int pauseWhenEmptySeconds, Protocol protocol) {
        return !isPaused(emptyTicks, pauseWhenEmptySeconds, protocol);
    }

    public static boolean baselineIsStaleAfterPause(
            int emptyTicks, int pauseWhenEmptySeconds, Protocol protocol) {
        return isPaused(emptyTicks, pauseWhenEmptySeconds, protocol);
    }
}
