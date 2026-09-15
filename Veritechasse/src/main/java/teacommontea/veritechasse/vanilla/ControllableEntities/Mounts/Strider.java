package teacommontea.veritechasse.vanilla.ControllableEntities.Mounts;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support.ControlGate;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support.MovementEnvelope;

public final class Strider {

    public static final String KEY = "strider";

    public static final float SPEED_MULTIPLIER = 0.55F;
    public static final float SUFFOCATING_SPEED_MULTIPLIER = 0.35F;
    public static final float LEGACY_SUFFOCATING_SPEED_MULTIPLIER = 0.23F;
    public static final double MOVEMENT_SPEED_BASE = 0.175D;

    public static final int SUFFOCATING_ATTRIBUTE_MAJOR = 1;
    public static final int SUFFOCATING_ATTRIBUTE_MINOR = 19;
    public static final int SUFFOCATING_ATTRIBUTE_PATCH = 4;

    public static final double SUFFOCATING_MODIFIER_AMOUNT = -0.34D;

    public static final boolean STEERABLE = false;
    public static final boolean CAN_JUMP = false;
    public static final boolean CAN_FLY = false;
    public static final boolean CAN_DASH = false;
    public static final boolean ACCEPTS_POTION_EFFECTS = true;

    public static final float FORWARD_INPUT_X = 0.0F;
    public static final float FORWARD_INPUT_Y = 0.0F;
    public static final float FORWARD_INPUT_Z = 1.0F;

    public static final boolean REQUIRES_ADULT = true;
    public static final boolean REQUIRES_TAMED = false;

    public static final MovementEnvelope ENVELOPE =
        new MovementEnvelope(STEERABLE, CAN_JUMP, CAN_FLY, CAN_DASH, SPEED_MULTIPLIER, 0.0F);

    private Strider() {
    }

    public static ControlGate controlGate() {
        return ControlGate.SADDLE_AND_WARPED_FUNGUS_ON_A_STICK;
    }

    public static boolean canBeSaddled(boolean alive, boolean baby) {
        return alive && !baby;
    }

    public static boolean suffocatingIsAttributeDriven(Protocol protocol) {
        return protocol.atLeast(
            SUFFOCATING_ATTRIBUTE_MAJOR,
            SUFFOCATING_ATTRIBUTE_MINOR,
            SUFFOCATING_ATTRIBUTE_PATCH);
    }

    public static float speedMultiplier(boolean suffocating, Protocol protocol) {
        if (!suffocating) {
            return SPEED_MULTIPLIER;
        }
        return suffocatingIsAttributeDriven(protocol)
            ? SUFFOCATING_SPEED_MULTIPLIER
            : LEGACY_SUFFOCATING_SPEED_MULTIPLIER;
    }

    public static double suffocatingAttributeSpeed(
            double baseMovementSpeed,
            boolean suffocating,
            Protocol protocol) {
        if (!suffocating || !suffocatingIsAttributeDriven(protocol)) {
            return baseMovementSpeed;
        }
        return baseMovementSpeed + baseMovementSpeed * SUFFOCATING_MODIFIER_AMOUNT;
    }

    public static double maxSpeed(
            double baseMovementSpeed,
            boolean suffocating,
            float boostFactor,
            Protocol protocol) {
        return suffocatingAttributeSpeed(baseMovementSpeed, suffocating, protocol)
            * (double) speedMultiplier(suffocating, protocol)
            * (double) boostFactor;
    }

    public static double maxSpeed(boolean suffocating, float boostFactor, Protocol protocol) {
        return maxSpeed(MOVEMENT_SPEED_BASE, suffocating, boostFactor, protocol);
    }

    public static Reality speedFrom(
            double baseMovementSpeed,
            boolean suffocating,
            float boostFactor,
            Protocol protocol) {
        return Reality.of(maxSpeed(baseMovementSpeed, suffocating, boostFactor, protocol));
    }

    public static Reality jumpFrom() {
        return Reality.impossible();
    }

    public static boolean acceptsPotionEffects() {
        return ACCEPTS_POTION_EFFECTS;
    }
}
