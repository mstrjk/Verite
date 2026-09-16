package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.Vanilla.Potions.SlowFalling;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Gravity {

    public static final double DEFAULT_BASE_GRAVITY = 0.08D;

    public static final double ATTRIBUTE_MINIMUM = -1.0D;
    public static final double ATTRIBUTE_MAXIMUM = 1.0D;

    public static final double ANTI_GRAVITY_THRESHOLD = 0.0D;

    public static final int ATTRIBUTE_PROTOCOL_MAJOR = 1;
    public static final int ATTRIBUTE_PROTOCOL_MINOR = 20;
    public static final int ATTRIBUTE_PROTOCOL_PATCH = 5;

    private Gravity() {
    }

    public static boolean attributeExists(Protocol protocol) {
        return protocol.atLeast(
            ATTRIBUTE_PROTOCOL_MAJOR,
            ATTRIBUTE_PROTOCOL_MINOR,
            ATTRIBUTE_PROTOCOL_PATCH);
    }

    public static double sanitize(double gravity) {
        return Attributes.GRAVITY.sanitize(gravity);
    }

    public static boolean risesWithoutInput(double gravity) {
        return gravity < ANTI_GRAVITY_THRESHOLD;
    }

    public static double base(Protocol protocol, double attributeValue) {
        if (!attributeExists(protocol)) {
            return DEFAULT_BASE_GRAVITY;
        }
        return sanitize(attributeValue);
    }

    public static double base() {
        return DEFAULT_BASE_GRAVITY;
    }

    public static boolean falling(double deltaY) {
        return deltaY <= 0.0D;
    }

    public static boolean deltaMustDecreaseAirborne(
            boolean levitating, boolean gliding, boolean flying, boolean inFluid) {
        return !levitating && !gliding && !flying && !inFluid;
    }

    public static double maximumNextDeltaY(
            double currentDeltaY, ActiveEffects effects, float verticalDrag, Protocol protocol) {
        double afterGravity = currentDeltaY - effective(currentDeltaY, effects, protocol);
        return afterGravity * (double) verticalDrag;
    }

    public static boolean riseIsSustained(double previousDeltaY, double currentDeltaY) {
        if (previousDeltaY <= 0.0D || currentDeltaY <= 0.0D) {
            return false;
        }
        return currentDeltaY >= previousDeltaY;
    }

    public static boolean contradictsGravity(
            double previousDeltaY,
            double currentDeltaY,
            ActiveEffects effects,
            float verticalDrag,
            boolean levitating,
            boolean gliding,
            boolean flying,
            boolean inFluid,
            boolean supported,
            Protocol protocol) {
        if (!deltaMustDecreaseAirborne(levitating, gliding, flying, inFluid)) {
            return false;
        }
        if (supported) {
            return false;
        }
        if (currentDeltaY <= 0.0D) {
            return false;
        }
        double permitted = maximumNextDeltaY(
            previousDeltaY, effects, verticalDrag, protocol);
        return currentDeltaY > permitted;
    }

    public static double effective(double baseGravity, double deltaY, boolean slowFalling, Protocol protocol) {
        if (!slowFalling) {
            return baseGravity;
        }
        return SlowFalling.effectiveGravity(baseGravity, deltaY, protocol);
    }

    public static double effective(double deltaY, ActiveEffects effects, Protocol protocol) {
        boolean slowFalling = effects != null && effects.has(SlowFalling.KEY);
        return effective(DEFAULT_BASE_GRAVITY, deltaY, slowFalling, protocol);
    }

    public static double effective(
            double baseGravity,
            double deltaY,
            ActiveEffects effects,
            Protocol protocol) {
        boolean slowFalling = effects != null && effects.has(SlowFalling.KEY);
        return effective(baseGravity, deltaY, slowFalling, protocol);
    }

    public static Reality effectiveFrom(double deltaY, ActiveEffects effects, Protocol protocol) {
        return Reality.of(effective(deltaY, effects, protocol));
    }
}
