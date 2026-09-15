package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerMisc;

import teacommontea.veritechasse.vanilla.Reality;

public final class SwimSteering {

    public static final double STEEP_LOOK_THRESHOLD = -0.2D;

    public static final double STEEP_MULTIPLIER = 0.085D;
    public static final double SHALLOW_MULTIPLIER = 0.06D;

    public static final double FLUID_PROBE_OFFSET = 1.0D - 0.1D;

    private SwimSteering() {
    }

    public static double multiplierFor(double lookAngleY) {
        return lookAngleY < STEEP_LOOK_THRESHOLD ? STEEP_MULTIPLIER : SHALLOW_MULTIPLIER;
    }

    public static boolean applies(
            boolean swimming,
            double lookAngleY,
            boolean jumping,
            boolean fluidAboveHead) {
        if (!swimming) {
            return false;
        }
        if (lookAngleY <= 0.0D) {
            return true;
        }
        return jumping || fluidAboveHead;
    }

    public static double verticalAfter(
            double deltaY,
            double lookAngleY,
            boolean swimming,
            boolean jumping,
            boolean fluidAboveHead) {
        if (!applies(swimming, lookAngleY, jumping, fluidAboveHead)) {
            return deltaY;
        }
        return deltaY + (lookAngleY - deltaY) * multiplierFor(lookAngleY);
    }

    public static double convergedVertical(double lookAngleY) {
        return lookAngleY;
    }

    public static double maximumAscent(double lookAngleY, double deltaY) {
        if (lookAngleY <= 0.0D) {
            return deltaY;
        }
        return deltaY + (lookAngleY - deltaY) * SHALLOW_MULTIPLIER;
    }

    public static Reality verticalFrom(double deltaY, double lookAngleY) {
        return Reality.of(maximumAscent(lookAngleY, deltaY));
    }
}
