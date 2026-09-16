package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class TickSprint {

    public static final String KEY = "tick_sprint";

    public static final int SPRINT_EXISTS_MAJOR = 1;
    public static final int SPRINT_EXISTS_MINOR = 20;
    public static final int SPRINT_EXISTS_PATCH = 3;

    public static final long SPRINT_NANOSECONDS_PER_TICK = 0L;

    private TickSprint() {
    }

    public static boolean sprintExists(Protocol protocol) {
        return protocol.atLeast(
            SPRINT_EXISTS_MAJOR, SPRINT_EXISTS_MINOR, SPRINT_EXISTS_PATCH);
    }

    public static boolean isSprinting(long scheduledCurrentSprintTicks) {
        return scheduledCurrentSprintTicks > 0L;
    }

    public static boolean tickIsSprinted(long thisTickNanos) {
        return thisTickNanos == SPRINT_NANOSECONDS_PER_TICK;
    }

    public static long remainingAfterTick(long remainingSprintTicks) {
        return remainingSprintTicks > 0L ? remainingSprintTicks - 1L : 0L;
    }

    public static boolean sprintContinues(long remainingSprintTicks, boolean runsGameElements) {
        return runsGameElements && remainingSprintTicks > 0L;
    }

    public static long completedTicks(long scheduled, long remaining) {
        return scheduled - remaining;
    }

    public static boolean rateIsUnbounded(boolean sprinting) {
        return sprinting;
    }

    public static boolean elapsedTimeIsMeaningless(boolean sprinting) {
        return sprinting;
    }

    public static boolean freezeIsSuspendedDuringSprint() {
        return true;
    }

    public static boolean timerEvidenceIsValid(boolean sprinting, Protocol protocol) {
        if (!sprintExists(protocol)) {
            return true;
        }
        return !sprinting;
    }
}
