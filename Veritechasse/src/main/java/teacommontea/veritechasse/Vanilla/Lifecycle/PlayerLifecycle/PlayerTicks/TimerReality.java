package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerTicks;

import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.EmptyServerPause;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickFreeze;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickOverload;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickRate;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickSprint;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class TimerReality {

    public static final String KEY = "timer";

    public static final double DEFAULT_TOLERANCE = 0.2D;

    private TimerReality() {
    }

    public static boolean evidenceIsAdmissible(
            boolean sprinting,
            boolean frozen,
            long behindTimeNanos,
            long nanosecondsPerTick,
            int emptyTicks,
            int pauseWhenEmptySeconds,
            Protocol protocol) {
        if (!TickSprint.timerEvidenceIsValid(sprinting, protocol)) {
            return false;
        }
        if (!TickFreeze.movementIsStillEvaluated(frozen, protocol)) {
            return false;
        }
        if (!TickOverload.timerEvidenceIsValid(
                behindTimeNanos, nanosecondsPerTick, protocol)) {
            return false;
        }
        return !EmptyServerPause.baselineIsStaleAfterPause(
            emptyTicks, pauseWhenEmptySeconds, protocol);
    }

    public static double expectedPackets(
            double elapsedRealMillis, float tickrate, Protocol protocol) {
        return TickRate.expectedTicksInRealMillis(elapsedRealMillis, tickrate, protocol);
    }

    public static Reality permittedPackets(
            double elapsedRealMillis,
            float tickrate,
            double tolerance,
            Protocol protocol) {
        double expected = expectedPackets(elapsedRealMillis, tickrate, protocol);
        return Reality.of(expected * (1.0D + tolerance));
    }

    public static Reality permittedPackets(
            double elapsedRealMillis, float tickrate, Protocol protocol) {
        return permittedPackets(elapsedRealMillis, tickrate, DEFAULT_TOLERANCE, protocol);
    }

    public static double observedRateRatio(
            int observedPackets,
            double elapsedRealMillis,
            float tickrate,
            Protocol protocol) {
        double expected = expectedPackets(elapsedRealMillis, tickrate, protocol);
        if (expected <= 0.0D) {
            return 0.0D;
        }
        return (double) observedPackets / expected;
    }

    public static boolean rateExceedsReality(
            int observedPackets,
            double elapsedRealMillis,
            float tickrate,
            double tolerance,
            Protocol protocol) {
        return permittedPackets(elapsedRealMillis, tickrate, tolerance, protocol)
            .exceededBy((double) observedPackets);
    }

    public static boolean timerSuspected(
            int observedPackets,
            double elapsedRealMillis,
            float tickrate,
            double tolerance,
            boolean sprinting,
            boolean frozen,
            long behindTimeNanos,
            long nanosecondsPerTick,
            int emptyTicks,
            int pauseWhenEmptySeconds,
            Protocol protocol) {
        if (!evidenceIsAdmissible(sprinting, frozen, behindTimeNanos,
                nanosecondsPerTick, emptyTicks, pauseWhenEmptySeconds, protocol)) {
            return false;
        }
        return rateExceedsReality(
            observedPackets, elapsedRealMillis, tickrate, tolerance, protocol);
    }
}
