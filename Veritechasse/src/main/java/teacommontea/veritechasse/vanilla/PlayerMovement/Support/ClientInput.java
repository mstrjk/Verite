package teacommontea.veritechasse.vanilla.PlayerMovement.Support;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class ClientInput {

    public static final int AVAILABLE_PROTOCOL_MAJOR = 1;
    public static final int AVAILABLE_PROTOCOL_MINOR = 21;
    public static final int AVAILABLE_PROTOCOL_PATCH = 2;

    public static final boolean IS_CLIENT_SUPPLIED = true;

    private ClientInput() {
    }

    public static boolean availableIn(Protocol protocol) {
        return protocol.atLeast(
            AVAILABLE_PROTOCOL_MAJOR,
            AVAILABLE_PROTOCOL_MINOR,
            AVAILABLE_PROTOCOL_PATCH);
    }

    public static float forwardIntent(boolean forward, boolean backward) {
        return MoveInput.impulse(forward, backward);
    }

    public static float leftIntent(boolean left, boolean right) {
        return MoveInput.impulse(left, right);
    }

    public static double intentX(float leftImpulse, float forwardImpulse, float yawDegrees) {
        double length = leftImpulse * leftImpulse + forwardImpulse * forwardImpulse;
        if (length < MoveInput.LENGTH_TOLERANCE) {
            return 0.0D;
        }
        double normalisedLeft = leftImpulse / Math.sqrt(length);
        double normalisedForward = forwardImpulse / Math.sqrt(length);
        double yaw = Math.toRadians(yawDegrees);
        return normalisedLeft * Math.cos(yaw) - normalisedForward * Math.sin(yaw);
    }

    public static double intentZ(float leftImpulse, float forwardImpulse, float yawDegrees) {
        double length = leftImpulse * leftImpulse + forwardImpulse * forwardImpulse;
        if (length < MoveInput.LENGTH_TOLERANCE) {
            return 0.0D;
        }
        double normalisedLeft = leftImpulse / Math.sqrt(length);
        double normalisedForward = forwardImpulse / Math.sqrt(length);
        double yaw = Math.toRadians(yawDegrees);
        return normalisedForward * Math.cos(yaw) + normalisedLeft * Math.sin(yaw);
    }

    public static double intentLength(float leftImpulse, float forwardImpulse) {
        return MoveInput.expectedLength(forwardImpulse, leftImpulse);
    }

    public static boolean movementContradictsInput(
            double observedHorizontal,
            float forwardImpulse,
            float leftImpulse,
            double decay,
            double previousHorizontal) {
        if (!MoveInput.isStationary(forwardImpulse, leftImpulse)) {
            return false;
        }
        double coasting = previousHorizontal * decay;
        return observedHorizontal > coasting + MoveInput.LENGTH_TOLERANCE;
    }

    public static boolean sprintClaimIsConsistent(boolean sprintFlag, float forwardImpulse) {
        if (!sprintFlag) {
            return true;
        }
        return forwardImpulse > 0.0F;
    }

    public static Reality coastingBoundFrom(double previousHorizontal, double decay) {
        return Reality.of(previousHorizontal * decay);
    }
}
