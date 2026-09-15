package teacommontea.veritechasse.vanilla.ControllableEntities.Boats.Support;

import teacommontea.veritechasse.vanilla.Reality;

public final class BoatControl {

    public static final float PADDLE_SPEED = (float) (Math.PI / 8);

    public static final float FORWARD_ACCELERATION = 0.04F;
    public static final float BACKWARD_ACCELERATION = -0.005F;
    public static final float TURNING_ACCELERATION = 0.005F;

    public static final float ROTATION_PER_TICK = 1.0F;

    public static final boolean LOCAL_INSTANCE_AUTHORITATIVE = true;
    public static final boolean SERVER_SIMULATES_WHEN_RIDDEN = false;

    private BoatControl() {
    }

    public static float acceleration(boolean inputUp, boolean inputDown, boolean inputLeft, boolean inputRight) {
        float acceleration = 0.0F;
        if (inputRight != inputLeft && !inputUp && !inputDown) {
            acceleration += TURNING_ACCELERATION;
        }
        if (inputUp) {
            acceleration += FORWARD_ACCELERATION;
        }
        if (inputDown) {
            acceleration += BACKWARD_ACCELERATION;
        }
        return acceleration;
    }

    public static float maxAcceleration() {
        return FORWARD_ACCELERATION;
    }

    public static float deltaRotationAfter(float deltaRotation, boolean inputLeft, boolean inputRight) {
        float rotation = deltaRotation;
        if (inputLeft) {
            rotation = rotation - ROTATION_PER_TICK;
        }
        if (inputRight) {
            rotation = rotation + ROTATION_PER_TICK;
        }
        return rotation;
    }

    public static boolean paddlingLeft(boolean inputLeft, boolean inputRight, boolean inputUp) {
        return inputLeft && !inputRight || inputUp;
    }

    public static boolean paddlingRight(boolean inputLeft, boolean inputRight, boolean inputUp) {
        return inputRight && !inputLeft || inputUp;
    }

    public static boolean serverValidatesPosition(boolean playerControlled) {
        if (!playerControlled) {
            return true;
        }
        return SERVER_SIMULATES_WHEN_RIDDEN;
    }

    public static boolean verticalInputIsPossible() {
        return false;
    }

    public static Reality accelerationFrom(boolean inputUp, boolean inputDown, boolean inputLeft, boolean inputRight) {
        return Reality.of(acceleration(inputUp, inputDown, inputLeft, inputRight));
    }

    public static Reality verticalControlFrom() {
        return Reality.impossible();
    }
}
