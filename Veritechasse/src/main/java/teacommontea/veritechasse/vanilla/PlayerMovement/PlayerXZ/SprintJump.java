package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ;

import teacommontea.veritechasse.vanilla.Reality;

public final class SprintJump {

    public static final double HORIZONTAL_IMPULSE = 0.2D;

    public static final boolean REQUIRES_SPRINTING = true;
    public static final boolean REQUIRES_GROUND = true;

    public static final float MINIMUM_JUMP_POWER = 1.0E-5F;

    private SprintJump() {
    }

    public static boolean applies(boolean sprinting, boolean onGround, float jumpPower) {
        if (!sprinting || !onGround) {
            return false;
        }
        return jumpPower > MINIMUM_JUMP_POWER;
    }

    public static double impulseX(float yawDegrees) {
        return -Math.sin(Math.toRadians(yawDegrees)) * HORIZONTAL_IMPULSE;
    }

    public static double impulseZ(float yawDegrees) {
        return Math.cos(Math.toRadians(yawDegrees)) * HORIZONTAL_IMPULSE;
    }

    public static double horizontalAfter(double currentHorizontal, boolean sprinting, boolean onGround, float jumpPower) {
        if (!applies(sprinting, onGround, jumpPower)) {
            return currentHorizontal;
        }
        return currentHorizontal + HORIZONTAL_IMPULSE;
    }

    public static double maximumImpulse() {
        return HORIZONTAL_IMPULSE;
    }

    public static Reality impulseFrom(boolean sprinting, boolean onGround, float jumpPower) {
        if (!applies(sprinting, onGround, jumpPower)) {
            return Reality.impossible();
        }
        return Reality.of(HORIZONTAL_IMPULSE);
    }
}
