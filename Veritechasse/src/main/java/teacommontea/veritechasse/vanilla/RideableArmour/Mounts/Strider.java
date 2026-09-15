package teacommontea.veritechasse.vanilla.RideableArmour.Mounts;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.ControlGate;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.MovementEnvelope;

public final class Strider {

    public static final String KEY = "strider";

    public static final float SPEED_MULTIPLIER = 0.55F;
    public static final float SUFFOCATING_SPEED_MULTIPLIER = 0.35F;
    public static final double MOVEMENT_SPEED_BASE = 0.175D;

    public static final boolean STEERABLE = false;
    public static final boolean CAN_JUMP = false;
    public static final boolean CAN_FLY = false;
    public static final boolean CAN_DASH = false;

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

    public static float speedMultiplier(boolean suffocating) {
        return suffocating ? SUFFOCATING_SPEED_MULTIPLIER : SPEED_MULTIPLIER;
    }

    public static double maxSpeed(double movementSpeedAttribute, boolean suffocating, float boostFactor) {
        return movementSpeedAttribute * speedMultiplier(suffocating) * boostFactor;
    }

    public static double maxSpeed(boolean suffocating, float boostFactor) {
        return maxSpeed(MOVEMENT_SPEED_BASE, suffocating, boostFactor);
    }

    public static Reality speedFrom(double movementSpeedAttribute, boolean suffocating, float boostFactor) {
        return Reality.of(maxSpeed(movementSpeedAttribute, suffocating, boostFactor));
    }

    public static Reality jumpFrom() {
        return Reality.impossible();
    }
}
