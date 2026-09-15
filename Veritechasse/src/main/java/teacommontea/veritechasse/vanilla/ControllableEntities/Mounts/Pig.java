package teacommontea.veritechasse.vanilla.ControllableEntities.Mounts;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support.ControlGate;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support.MovementEnvelope;

public final class Pig {

    public static final String KEY = "pig";

    public static final double SPEED_MULTIPLIER = 0.225D;
    public static final float ENVELOPE_SPEED_MULTIPLIER = 0.225F;
    public static final double MOVEMENT_SPEED_BASE = 0.25D;

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
        new MovementEnvelope(STEERABLE, CAN_JUMP, CAN_FLY, CAN_DASH, ENVELOPE_SPEED_MULTIPLIER, 0.0F);

    private Pig() {
    }

    public static ControlGate controlGate() {
        return ControlGate.SADDLE_AND_CARROT_ON_A_STICK;
    }

    public static boolean canBeSaddled(boolean alive, boolean baby) {
        return alive && !baby;
    }

    public static double maxSpeed(double movementSpeedAttribute, float boostFactor) {
        return movementSpeedAttribute * SPEED_MULTIPLIER * (double) boostFactor;
    }

    public static double maxSpeed(float boostFactor) {
        return maxSpeed(MOVEMENT_SPEED_BASE, boostFactor);
    }

    public static Reality speedFrom(double movementSpeedAttribute, float boostFactor) {
        return Reality.of(maxSpeed(movementSpeedAttribute, boostFactor));
    }

    public static Reality jumpFrom() {
        return Reality.impossible();
    }

    public static boolean acceptsPotionEffects() {
        return ACCEPTS_POTION_EFFECTS;
    }
}
