package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.vanilla.Potions.Levitation;
import teacommontea.veritechasse.vanilla.Potions.SlowFalling;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class FallDistance {

    public static final double NONE = 0.0D;

    public static final int DOUBLE_PRECISION_PROTOCOL_MAJOR = 1;
    public static final int DOUBLE_PRECISION_PROTOCOL_MINOR = 21;
    public static final int DOUBLE_PRECISION_PROTOCOL_PATCH = 5;

    public static final int EFFECT_RESET_PROTOCOL_MAJOR = 1;
    public static final int EFFECT_RESET_PROTOCOL_MINOR = 20;
    public static final int EFFECT_RESET_PROTOCOL_PATCH = 0;

    private FallDistance() {
    }

    public static boolean storedAsDouble(Protocol protocol) {
        return protocol.atLeast(
            DOUBLE_PRECISION_PROTOCOL_MAJOR,
            DOUBLE_PRECISION_PROTOCOL_MINOR,
            DOUBLE_PRECISION_PROTOCOL_PATCH);
    }

    public static boolean accumulatesBeforeGroundCheck(Protocol protocol) {
        return storedAsDouble(protocol);
    }

    public static boolean waterSuppressesAccumulation(Protocol protocol) {
        return storedAsDouble(protocol);
    }

    public static boolean accumulates(double deltaY, boolean inWater, Protocol protocol) {
        if (deltaY >= 0.0D) {
            return false;
        }
        if (waterSuppressesAccumulation(protocol) && inWater) {
            return false;
        }
        return true;
    }

    public static double narrow(double value, Protocol protocol) {
        if (storedAsDouble(protocol)) {
            return value;
        }
        return (float) value;
    }

    public static double accumulate(
            double currentFallDistance,
            double deltaY,
            boolean inWater,
            Protocol protocol) {
        if (!accumulates(deltaY, inWater, protocol)) {
            return currentFallDistance;
        }
        double added = currentFallDistance - (double) ((float) deltaY);
        return narrow(added, protocol);
    }

    public static double afterTick(
            double currentFallDistance,
            double deltaY,
            boolean onGround,
            boolean inWater,
            Protocol protocol) {
        if (accumulatesBeforeGroundCheck(protocol)) {
            double accumulated = accumulate(currentFallDistance, deltaY, inWater, protocol);
            return onGround ? NONE : accumulated;
        }
        if (onGround) {
            return NONE;
        }
        return accumulate(currentFallDistance, deltaY, inWater, protocol);
    }

    public static double landingDistance(
            double currentFallDistance,
            double deltaY,
            boolean inWater,
            Protocol protocol) {
        if (!accumulatesBeforeGroundCheck(protocol)) {
            return currentFallDistance;
        }
        return accumulate(currentFallDistance, deltaY, inWater, protocol);
    }

    public static boolean effectResetIsUnconditional(Protocol protocol) {
        return protocol.atLeast(
            EFFECT_RESET_PROTOCOL_MAJOR,
            EFFECT_RESET_PROTOCOL_MINOR,
            EFFECT_RESET_PROTOCOL_PATCH);
    }

    public static boolean levitationResets(Protocol protocol) {
        return effectResetIsUnconditional(protocol);
    }

    public static boolean resetBy(ActiveEffects effects, double deltaY, Protocol protocol) {
        if (effects == null) {
            return false;
        }
        if (effectResetIsUnconditional(protocol)) {
            return effects.has(SlowFalling.KEY) || effects.has(Levitation.KEY);
        }
        return effects.has(SlowFalling.KEY) && SlowFalling.applies(deltaY);
    }

    public static double afterTick(
            double currentFallDistance,
            double deltaY,
            boolean onGround,
            boolean inWater,
            ActiveEffects effects,
            Protocol protocol) {
        if (resetBy(effects, deltaY, protocol)) {
            return NONE;
        }
        return afterTick(currentFallDistance, deltaY, onGround, inWater, protocol);
    }

    public static Reality afterTickFrom(
            double currentFallDistance,
            double deltaY,
            boolean onGround,
            boolean inWater,
            Protocol protocol) {
        return Reality.of(afterTick(currentFallDistance, deltaY, onGround, inWater, protocol));
    }
}
