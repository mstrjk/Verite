package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class TickFreeze {

    public static final String KEY = "tick_freeze";

    public static final int FREEZE_EXISTS_MAJOR = 1;
    public static final int FREEZE_EXISTS_MINOR = 20;
    public static final int FREEZE_EXISTS_PATCH = 3;

    private TickFreeze() {
    }

    public static boolean freezeExists(Protocol protocol) {
        return protocol.atLeast(
            FREEZE_EXISTS_MAJOR, FREEZE_EXISTS_MINOR, FREEZE_EXISTS_PATCH);
    }

    public static boolean runsGameElements(boolean frozen, int frozenTicksToRun) {
        return !frozen || frozenTicksToRun > 0;
    }

    public static boolean isSteppingForward(int frozenTicksToRun) {
        return frozenTicksToRun > 0;
    }

    public static int afterTick(int frozenTicksToRun) {
        return frozenTicksToRun > 0 ? frozenTicksToRun - 1 : frozenTicksToRun;
    }

    public static boolean playersKeepTicking() {
        return true;
    }

    public static boolean entityIsFrozen(
            boolean runsNormally, boolean isPlayer, int playerPassengers) {
        return !runsNormally && !isPlayer && playerPassengers <= 0;
    }

    public static boolean stepAccepted(boolean frozen) {
        return frozen;
    }

    public static boolean worldTimeAdvances(boolean frozen, int frozenTicksToRun) {
        return runsGameElements(frozen, frozenTicksToRun);
    }

    public static boolean movementIsStillEvaluated(boolean frozen, Protocol protocol) {
        if (!freezeExists(protocol)) {
            return true;
        }
        return !frozen;
    }
}
