package teacommontea.veritechasse.vanilla.RideableArmour.Mounts;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.ControlGate;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.MovementEnvelope;

public final class HappyGhast {

    public static final String KEY = "happy_ghast";

    public static final int PROTOCOL_MAJOR = 1;
    public static final int PROTOCOL_MINOR = 21;
    public static final int PROTOCOL_PATCH = 6;

    public static final double FLYING_SPEED_BASE = 0.05D;
    public static final double MOVEMENT_SPEED_BASE = 0.05D;
    public static final double MAX_HEALTH = 20.0D;
    public static final double CAMERA_DISTANCE = 8.0D;

    public static final float RIDDEN_INPUT_SCALE = 3.9F;
    public static final float TRAVEL_SPEED_RATIO = 5.0F / 3.0F;
    public static final float JUMP_UPWARD_INPUT = 0.5F;
    public static final float BACKWARD_LOOK_FACTOR = -0.5F;
    public static final float ROTATION_PITCH_FACTOR = 0.5F;

    public static final int MAX_STILL_TIMEOUT = 10;
    public static final int STILL_TIMEOUT_ON_LOAD_GRACE_PERIOD = 60;

    public static final boolean STEERABLE = true;
    public static final boolean CAN_JUMP = false;
    public static final boolean CAN_FLY = true;
    public static final boolean CAN_DASH = false;

    public static final MovementEnvelope ENVELOPE =
        new MovementEnvelope(STEERABLE, CAN_JUMP, CAN_FLY, CAN_DASH, RIDDEN_INPUT_SCALE, BACKWARD_LOOK_FACTOR);

    private HappyGhast() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(PROTOCOL_MAJOR, PROTOCOL_MINOR, PROTOCOL_PATCH);
    }

    public static ControlGate controlGate() {
        return ControlGate.HARNESS_AND_NOT_STILL;
    }

    public static boolean grantsControl(boolean harnessed, boolean stillTimeout) {
        return harnessed && !stillTimeout;
    }

    public static double maxRiddenSpeed(double flyingSpeedAttribute) {
        return flyingSpeedAttribute * RIDDEN_INPUT_SCALE;
    }

    public static double maxRiddenSpeed() {
        return maxRiddenSpeed(FLYING_SPEED_BASE);
    }

    public static double travelSpeed(double flyingSpeedAttribute) {
        return flyingSpeedAttribute * TRAVEL_SPEED_RATIO;
    }

    public static double verticalInput(boolean jumping, double pitchComponent) {
        return jumping ? pitchComponent + JUMP_UPWARD_INPUT : pitchComponent;
    }

    public static Reality riddenSpeedFrom(double flyingSpeedAttribute) {
        return Reality.of(maxRiddenSpeed(flyingSpeedAttribute));
    }

    public static Reality riddenSpeedFrom() {
        return Reality.of(maxRiddenSpeed());
    }
}
