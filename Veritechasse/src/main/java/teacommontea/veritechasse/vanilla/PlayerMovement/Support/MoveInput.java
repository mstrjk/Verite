package teacommontea.veritechasse.vanilla.PlayerMovement.Support;

import teacommontea.veritechasse.vanilla.Protocol;
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

    public static final double ZERO_INPUT_SQUARED_THRESHOLD = 1.0E-7D;
    public static final double NORMALISE_SQUARED_THRESHOLD = 1.0D;

    public static final float INPUT_SCALE_SOURCE = 0.98F;
    public static final double INPUT_SCALE = INPUT_SCALE_SOURCE;

    public static final int BITFIELD_MAJOR = 1;
    public static final int BITFIELD_MINOR = 21;
    public static final int BITFIELD_PATCH = 2;

    public static final int SQUARE_MOVEMENT_MAJOR = 1;
    public static final int SQUARE_MOVEMENT_MINOR = 21;
    public static final int SQUARE_MOVEMENT_PATCH = 5;

    private MoveInput() {
    }

    public static boolean bitfieldExists(Protocol protocol) {
        return protocol.atLeast(BITFIELD_MAJOR, BITFIELD_MINOR, BITFIELD_PATCH);
    }

    public static boolean squareMovementApplies(Protocol protocol) {
        return protocol.atLeast(
            SQUARE_MOVEMENT_MAJOR, SQUARE_MOVEMENT_MINOR, SQUARE_MOVEMENT_PATCH);
    }

    public static boolean inputIsZero(double squaredLength) {
        return squaredLength < ZERO_INPUT_SQUARED_THRESHOLD;
    }

    public static boolean inputIsNormalised(double squaredLength) {
        return squaredLength > NORMALISE_SQUARED_THRESHOLD;
    }

    public static double effectiveLength(double squaredLength) {
        if (inputIsZero(squaredLength)) {
            return 0.0D;
        }
        if (inputIsNormalised(squaredLength)) {
            return 1.0D;
        }
        return Math.sqrt(squaredLength);
    }

    public static double distanceToUnitSquare(double componentA, double componentB) {
        double a = Math.abs(componentA);
        double b = Math.abs(componentB);
        double maximum = Math.max(a, b);
        if (maximum <= 0.0D) {
            return 1.0D;
        }
        double tangent = Math.min(a, b) / maximum;
        return Math.sqrt(1.0D + tangent * tangent);
    }

    public static double squareMovementLength(double forward, double left) {
        double length = Math.sqrt(forward * forward + left * left);
        if (length <= 0.0D) {
            return 0.0D;
        }
        double scaled = length * distanceToUnitSquare(forward / length, left / length);
        return Math.min(scaled, 1.0D);
    }

    public static double maximumInputLength(Protocol protocol, boolean diagonal) {
        if (!squareMovementApplies(protocol)) {
            return LEGAL_LENGTH_CARDINAL;
        }
        if (diagonal) {
            return Math.min(INPUT_SCALE * Math.sqrt(2.0D), 1.0D);
        }
        return INPUT_SCALE;
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

    public static double expectedLength(
            float forwardImpulse,
            float leftImpulse,
            Protocol protocol) {
        if (isStationary(forwardImpulse, leftImpulse)) {
            return LEGAL_LENGTH_NONE;
        }
        return maximumInputLength(protocol, isDiagonal(forwardImpulse, leftImpulse));
    }

    public static int legalVectorCount(Protocol protocol) {
        if (squareMovementApplies(protocol)) {
            return UNBOUNDED_VECTOR_COUNT;
        }
        return 9;
    }

    public static final int UNBOUNDED_VECTOR_COUNT = -1;

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
