package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerSleep;

import teacommontea.veritechasse.Vanilla.Reality;

public final class SleepTimer {

    public static final String KEY = "sleep_timer";

    public static final int SLEEP_DURATION = 100;

    public static final int WAKE_RAMP_END = 110;

    public static final int RESET = 0;

    private SleepTimer() {
    }

    public static int afterSleepingTick(int sleepCounter) {
        int next = sleepCounter + 1;
        return next > SLEEP_DURATION ? SLEEP_DURATION : next;
    }

    public static int afterAwakeTick(int sleepCounter) {
        if (sleepCounter <= 0) {
            return RESET;
        }
        int next = sleepCounter + 1;
        return next >= WAKE_RAMP_END ? RESET : next;
    }

    public static boolean sleptLongEnough(boolean sleeping, int sleepCounter) {
        return sleeping && sleepCounter >= SLEEP_DURATION;
    }

    public static int onStartSleeping() {
        return RESET;
    }

    public static int onStopSleeping(boolean forcefulWakeUp) {
        return forcefulWakeUp ? RESET : SLEEP_DURATION;
    }

    public static int ticksUntilSleptLongEnough(int sleepCounter) {
        int remaining = SLEEP_DURATION - sleepCounter;
        return remaining <= 0 ? 0 : remaining;
    }

    public static boolean counterIsInWakeRamp(boolean sleeping, int sleepCounter) {
        return !sleeping && sleepCounter > 0;
    }

    public static Reality earliestSkipTick() {
        return Reality.of(SLEEP_DURATION);
    }
}
