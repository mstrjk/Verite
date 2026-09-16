package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ;

import teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak.LookGeometry;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class SprintJump {

    public static final double HORIZONTAL_IMPULSE = 0.2D;
    public static final float HORIZONTAL_IMPULSE_SOURCE = 0.2F;

    public static final boolean REQUIRES_SPRINTING = true;
    public static final boolean REQUIRES_GROUND = true;

    public static final float MINIMUM_JUMP_POWER = 1.0E-5F;

    public static final int DOUBLE_IMPULSE_MAJOR = 1;
    public static final int DOUBLE_IMPULSE_MINOR = 20;
    public static final int DOUBLE_IMPULSE_PATCH = 5;

    private SprintJump() {
    }

    public static boolean impulseComputedInDouble(Protocol protocol) {
        return protocol.atLeast(
            DOUBLE_IMPULSE_MAJOR, DOUBLE_IMPULSE_MINOR, DOUBLE_IMPULSE_PATCH);
    }

    public static boolean applies(boolean sprinting, boolean onGround, float jumpPower) {
        if (!sprinting || !onGround) {
            return false;
        }
        return jumpPower > MINIMUM_JUMP_POWER;
    }

    public static double impulseX(float yawDegrees, Protocol protocol) {
        float radians = yawDegrees * LookGeometry.DEGREES_TO_RADIANS;
        float sin = LookGeometry.sin((double) radians);
        if (impulseComputedInDouble(protocol)) {
            return (double) (-sin) * HORIZONTAL_IMPULSE;
        }
        return (double) (-sin * HORIZONTAL_IMPULSE_SOURCE);
    }

    public static double impulseZ(float yawDegrees, Protocol protocol) {
        float radians = yawDegrees * LookGeometry.DEGREES_TO_RADIANS;
        float cos = LookGeometry.cos((double) radians);
        if (impulseComputedInDouble(protocol)) {
            return (double) cos * HORIZONTAL_IMPULSE;
        }
        return (double) (cos * HORIZONTAL_IMPULSE_SOURCE);
    }

    public static double impulseX(float yawDegrees) {
        return impulseX(yawDegrees, Protocol.current());
    }

    public static double impulseZ(float yawDegrees) {
        return impulseZ(yawDegrees, Protocol.current());
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
