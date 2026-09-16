package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerTicks;

import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickFreeze;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickRate;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class PlayerTickCounter {

    public static final String KEY = "player_tick_counter";

    private PlayerTickCounter() {
    }

    public static boolean counterAdvances(boolean serverPaused, boolean playerTicked) {
        return !serverPaused && playerTicked;
    }

    public static int afterTick(int tickCount, boolean serverPaused, boolean playerTicked) {
        return counterAdvances(serverPaused, playerTicked) ? tickCount + 1 : tickCount;
    }

    public static boolean countsRealTime() {
        return false;
    }

    public static double expectedTicks(
            double elapsedRealMillis, float tickrate, Protocol protocol) {
        return TickRate.expectedTicksInRealMillis(elapsedRealMillis, tickrate, protocol);
    }

    public static double expectedRealMillis(
            long ticks, float tickrate, Protocol protocol) {
        return TickRate.realMillisecondsForTicks(ticks, tickrate, protocol);
    }

    public static boolean elapsedTimeIsComparable(
            boolean sprinting,
            boolean frozen,
            boolean ticksSkipped,
            boolean emptyPaused) {
        return !sprinting && !frozen && !ticksSkipped && !emptyPaused;
    }

    public static boolean playerTicksWhileWorldFrozen() {
        return TickFreeze.playersKeepTicking();
    }

    public static long ticksBetween(long previousTick, long currentTick) {
        return currentTick - previousTick;
    }

    public static boolean baselineIsUsable(long previousTick, long currentTick) {
        long gap = ticksBetween(previousTick, currentTick);
        return gap > 0L;
    }
}
