package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.vanilla.Potions.Levitation;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class VerticalTick {

    public static final double TERMINAL_VELOCITY = -3.9200038147009035D;

    public static final double CONVERGENCE_TOLERANCE = 1.0E-9D;

    private VerticalTick() {
    }

    public static double terminalVelocity(double gravity, float drag) {
        if (drag >= 1.0F) {
            return Double.NEGATIVE_INFINITY;
        }
        return -gravity * (double) drag / (1.0D - (double) drag);
    }

    public static double afterGravity(double deltaY, double effectiveGravity) {
        return deltaY - effectiveGravity;
    }

    public static double afterLevitation(double deltaY, int amplifier) {
        return Levitation.nextVelocity(deltaY, amplifier);
    }

    public static double next(
            double deltaY,
            ActiveEffects effects,
            float verticalDrag,
            Protocol protocol) {
        int levitation = effects == null
            ? ActiveEffects.ABSENT
            : effects.amplifierOf(Levitation.KEY);

        double raised;
        if (levitation != ActiveEffects.ABSENT) {
            raised = afterLevitation(deltaY, levitation);
        } else {
            raised = afterGravity(deltaY, Gravity.effective(deltaY, effects, protocol));
        }
        return VerticalDrag.after(raised, verticalDrag);
    }

    public static double next(double deltaY, ActiveEffects effects, Protocol protocol) {
        return next(deltaY, effects, VerticalDrag.forPlayer(), protocol);
    }

    public static double nextFalling(double deltaY) {
        return VerticalDrag.after(
            afterGravity(deltaY, Gravity.DEFAULT_BASE_GRAVITY),
            VerticalDrag.BASE_VERTICAL_AIR_DRAG);
    }

    public static double distanceAfter(int ticks, double initialDeltaY) {
        double delta = initialDeltaY;
        double travelled = 0.0D;
        for (int tick = 0; tick < ticks; tick++) {
            delta = nextFalling(delta);
            travelled = travelled + delta;
        }
        return travelled;
    }

    public static boolean descendsFasterThanPossible(
            double observedDeltaY,
            double previousDeltaY,
            ActiveEffects effects,
            Protocol protocol) {
        double expected = next(previousDeltaY, effects, protocol);
        return observedDeltaY < expected - CONVERGENCE_TOLERANCE;
    }

    public static boolean risesWithoutCause(
            double observedDeltaY,
            double previousDeltaY,
            ActiveEffects effects,
            Protocol protocol) {
        double expected = next(previousDeltaY, effects, protocol);
        return observedDeltaY > expected + CONVERGENCE_TOLERANCE;
    }

    public static Reality nextFrom(double deltaY, ActiveEffects effects, Protocol protocol) {
        return Reality.of(next(deltaY, effects, protocol));
    }

    public static Reality terminalFrom(double gravity, float drag) {
        return Reality.of(Math.abs(terminalVelocity(gravity, drag)));
    }
}
