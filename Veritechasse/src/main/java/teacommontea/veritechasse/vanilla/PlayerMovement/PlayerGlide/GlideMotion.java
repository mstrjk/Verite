package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerGlide;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class GlideMotion {

    public static final float HORIZONTAL_DRAG = 0.99F;
    public static final float VERTICAL_DRAG = 0.98F;

    public static final double LIFT_COEFFICIENT = 0.75D;
    public static final double DIVE_CONVERSION = -0.1D;
    public static final double PITCH_UP_CONVERSION = 0.04D;
    public static final double PITCH_UP_VERTICAL_SCALE = 3.2D;
    public static final double AIM_CORRECTION = 0.1D;

    public static final double LEGACY_LOOK_LENGTH_DIVISOR = 0.4D;

    public static final double FALL_DISTANCE_WHILE_GLIDING = 1.0D;
    public static final double FALL_DISTANCE_RESET_THRESHOLD = -0.5D;

    public static final int SQUARED_LIFT_PROTOCOL_MAJOR = 1;
    public static final int SQUARED_LIFT_PROTOCOL_MINOR = 21;
    public static final int SQUARED_LIFT_PROTOCOL_PATCH = 2;

    private GlideMotion() {
    }

    public static boolean liftIsUndamped(Protocol protocol) {
        return protocol.atLeast(
            SQUARED_LIFT_PROTOCOL_MAJOR,
            SQUARED_LIFT_PROTOCOL_MINOR,
            SQUARED_LIFT_PROTOCOL_PATCH);
    }

    public static double liftForce(float pitchDegrees, double lookLength, Protocol protocol) {
        double lean = Math.toRadians(pitchDegrees);
        double cosine = Math.cos(lean);
        double squared = cosine * cosine;
        if (liftIsUndamped(protocol)) {
            return squared;
        }
        double damping = lookLength / LEGACY_LOOK_LENGTH_DIVISOR;
        return squared * Math.min(1.0D, damping);
    }

    public static double gravityContribution(double effectiveGravity, double liftForce) {
        return effectiveGravity * (-1.0D + liftForce * LIFT_COEFFICIENT);
    }

    public static double diveConversion(double deltaY, double liftForce) {
        if (deltaY >= 0.0D) {
            return 0.0D;
        }
        return deltaY * DIVE_CONVERSION * liftForce;
    }

    public static double pitchUpConversion(float pitchDegrees, double horizontalSpeed) {
        double lean = Math.toRadians(pitchDegrees);
        if (lean >= 0.0D) {
            return 0.0D;
        }
        return horizontalSpeed * -Math.sin(lean) * PITCH_UP_CONVERSION;
    }

    public static double pitchUpVertical(float pitchDegrees, double horizontalSpeed) {
        return pitchUpConversion(pitchDegrees, horizontalSpeed) * PITCH_UP_VERTICAL_SCALE;
    }

    public static double aimCorrection(double lookComponent, double lookHorizontalLength,
            double horizontalSpeed, double currentComponent) {
        if (lookHorizontalLength <= 0.0D) {
            return 0.0D;
        }
        return (lookComponent / lookHorizontalLength * horizontalSpeed - currentComponent)
            * AIM_CORRECTION;
    }

    public static double horizontalAfterDrag(double component) {
        return component * (double) HORIZONTAL_DRAG;
    }

    public static double verticalAfterDrag(double component) {
        return component * (double) VERTICAL_DRAG;
    }

    public static double nextVerticalIgnoringLook(
            double deltaY,
            double effectiveGravity,
            float pitchDegrees,
            double lookLength,
            Protocol protocol) {
        double lift = liftForce(pitchDegrees, lookLength, protocol);
        double raised = deltaY + gravityContribution(effectiveGravity, lift);
        return verticalAfterDrag(raised);
    }

    public static double terminalDescent(double effectiveGravity, double liftForce) {
        double perTick = gravityContribution(effectiveGravity, liftForce);
        double drag = (double) VERTICAL_DRAG;
        if (drag >= 1.0D) {
            return Double.NEGATIVE_INFINITY;
        }
        return perTick * drag / (1.0D - drag);
    }

    public static boolean fallDistanceHeld(double deltaY) {
        return deltaY > FALL_DISTANCE_RESET_THRESHOLD;
    }

    public static Reality verticalDragFrom() {
        return Reality.of(VERTICAL_DRAG);
    }
}
