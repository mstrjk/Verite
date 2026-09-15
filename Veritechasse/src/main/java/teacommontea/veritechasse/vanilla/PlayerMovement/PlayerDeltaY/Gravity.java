package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.vanilla.Potions.SlowFalling;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

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
