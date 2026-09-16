package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness;

import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.EmptyServerPause;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickFreeze;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickOverload;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickRate;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class ServerHealth {

    public static final String KEY = "server_health";

    public static final double STALL_TICK_MULTIPLIER = 2.0D;

    private ServerHealth() {
    }

    public static boolean clientCanInfluence() {
        return false;
    }

    public static double expectedTickMillis(float tickrate) {
        return (double) TickRate.nanosecondsPerTick(tickrate)
            / (double) TickRate.NANOSECONDS_PER_MILLISECOND;
    }

    public static boolean stalled(double worstStallMillis, float tickrate) {
        return worstStallMillis > expectedTickMillis(tickrate) * STALL_TICK_MULTIPLIER;
    }

    public static boolean degraded(
            boolean sprinting,
            boolean frozen,
            long behindTimeNanos,
            long nanosecondsPerTick,
            int emptyTicks,
            int pauseWhenEmptySeconds,
            double worstStallMillis,
            float tickrate,
            Protocol protocol) {
        if (sprinting) {
            return true;
        }
        if (!TickFreeze.movementIsStillEvaluated(frozen, protocol)) {
            return true;
        }
        if (TickOverload.ticksWereSkipped(behindTimeNanos, nanosecondsPerTick, protocol)) {
            return true;
        }
        if (EmptyServerPause.isPaused(emptyTicks, pauseWhenEmptySeconds, protocol)) {
            return true;
        }
        return stalled(worstStallMillis, tickrate);
    }

    public static String route(
            boolean sprinting,
            boolean frozen,
            long behindTimeNanos,
            long nanosecondsPerTick,
            int emptyTicks,
            int pauseWhenEmptySeconds,
            double worstStallMillis,
            float tickrate,
            Protocol protocol) {
        if (degraded(sprinting, frozen, behindTimeNanos, nanosecondsPerTick,
                emptyTicks, pauseWhenEmptySeconds, worstStallMillis, tickrate, protocol)) {
            return Verdict.DISCARD;
        }
        return Verdict.EVALUATE;
    }

    public static boolean affectsEveryPlayerSimultaneously() {
        return true;
    }

    public static boolean isolatedToOnePlayer() {
        return false;
    }
}
