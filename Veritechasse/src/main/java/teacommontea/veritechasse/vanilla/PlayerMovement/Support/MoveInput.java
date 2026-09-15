package teacommontea.veritechasse.vanilla.PlayerMovement.Support;

import teacommontea.veritechasse.vanilla.Reality;

public final class MoveInput {

    public static final byte FLAG_FORWARD = 1;
    public static final byte FLAG_BACKWARD = 2;
    public static final byte FLAG_LEFT = 4;
    public static final byte FLAG_RIGHT = 8;
    public static final byte FLAG_JUMP = 16;
    public static final byte FLAG_SHIFT = 32;
    public static final byte FLAG_SPRINT = 64;

    public static final float IMPULSE_POSITIVE = 1.0F;
    public static final float IMPULSE_NEUTRAL = 0.0F;
    public static final float IMPULSE_NEGATIVE = -1.0F;

    public static final double DIAGONAL_COMPONENT = Math.sqrt(0.5D);

    public static final double LEGAL_LENGTH_CARDINAL = 1.0D;
    public static final double LEGAL_LENGTH_DIAGONAL = 1.0D;
    public static final double LEGAL_LENGTH_NONE = 0.0D;

    public static final double LENGTH_TOLERANCE = 1.0E-5D;

    private MoveInput() {
    }

    public static float impulse(boolean positive, boolean negative) {
        if (positive == negative) {
            return IMPULSE_NEUTRAL;
        }
        return positive ? IMPULSE_POSITIVE : IMPULSE_NEGATIVE;
    }

    public static float forwardImpulse(boolean forward, boolean backward) {
        return impulse(forward, backward);
    }

    public static float leftImpulse(boolean left, boolean right) {
        return impulse(left, right);
    }

    public static double normalisedComponent(float ownImpulse, float otherImpulse) {
        if (ownImpulse == IMPULSE_NEUTRAL) {
            return 0.0D;
        }
        if (otherImpulse == IMPULSE_NEUTRAL) {
            return ownImpulse;
        }
        return ownImpulse * DIAGONAL_COMPONENT;
    }

    public static boolean isDiagonal(float forwardImpulse, float leftImpulse) {
        return forwardImpulse != IMPULSE_NEUTRAL && leftImpulse != IMPULSE_NEUTRAL;
    }

    public static boolean isStationary(float forwardImpulse, float leftImpulse) {
        return forwardImpulse == IMPULSE_NEUTRAL && leftImpulse == IMPULSE_NEUTRAL;
    }

    public static double expectedLength(float forwardImpulse, float leftImpulse) {
        if (isStationary(forwardImpulse, leftImpulse)) {
            return LEGAL_LENGTH_NONE;
        }
        return LEGAL_LENGTH_CARDINAL;
    }

    public static boolean lengthIsLegal(double observedLength, float forwardImpulse, float leftImpulse) {
        double expected = expectedLength(forwardImpulse, leftImpulse);
        return Math.abs(observedLength - expected) <= LENGTH_TOLERANCE;
    }

    public static boolean componentIsLegal(double observedComponent) {
        double magnitude = Math.abs(observedComponent);
        if (magnitude <= LENGTH_TOLERANCE) {
            return true;
        }
        if (Math.abs(magnitude - 1.0D) <= LENGTH_TOLERANCE) {
            return true;
        }
        return Math.abs(magnitude - DIAGONAL_COMPONENT) <= LENGTH_TOLERANCE;
    }

    public static int legalVectorCount() {
        return 9;
    }

    public static Reality lengthFrom(float forwardImpulse, float leftImpulse) {
        return Reality.of(expectedLength(forwardImpulse, leftImpulse));
    }
}
