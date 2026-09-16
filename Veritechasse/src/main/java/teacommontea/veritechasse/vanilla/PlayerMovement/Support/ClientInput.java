package teacommontea.veritechasse.vanilla.PlayerMovement.Support;

import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak.LookGeometry;
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

    public static double scaledComponent(float component, double squaredLength) {
        if (MoveInput.inputIsZero(squaredLength)) {
            return 0.0D;
        }
        if (MoveInput.inputIsNormalised(squaredLength)) {
            return component / Math.sqrt(squaredLength);
        }
        return component;
    }

    public static double intentX(float leftImpulse, float forwardImpulse, float yawDegrees) {
        double squaredLength = leftImpulse * leftImpulse + forwardImpulse * forwardImpulse;
        if (MoveInput.inputIsZero(squaredLength)) {
            return 0.0D;
        }
        double left = scaledComponent(leftImpulse, squaredLength);
        double forward = scaledComponent(forwardImpulse, squaredLength);
        float radians = yawDegrees * LookGeometry.DEGREES_TO_RADIANS;
        double sin = LookGeometry.sin((double) radians);
        double cos = LookGeometry.cos((double) radians);
        return left * cos - forward * sin;
    }

    public static double intentZ(float leftImpulse, float forwardImpulse, float yawDegrees) {
        double squaredLength = leftImpulse * leftImpulse + forwardImpulse * forwardImpulse;
        if (MoveInput.inputIsZero(squaredLength)) {
            return 0.0D;
        }
        double left = scaledComponent(leftImpulse, squaredLength);
        double forward = scaledComponent(forwardImpulse, squaredLength);
        float radians = yawDegrees * LookGeometry.DEGREES_TO_RADIANS;
        double sin = LookGeometry.sin((double) radians);
        double cos = LookGeometry.cos((double) radians);
        return forward * cos + left * sin;
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

    public static final float SPRINT_FOOD_THRESHOLD = 6.0F;

    public static boolean sprintClaimIsConsistent(boolean sprintFlag, float forwardImpulse) {
        if (!sprintFlag) {
            return true;
        }
        return forwardImpulse > 0.0F;
    }

    public static boolean foodPermitsSprintStart(int foodLevel, boolean mayFly) {
        return (float) foodLevel > SPRINT_FOOD_THRESHOLD || mayFly;
    }

    public static boolean sprintStartIsPossible(
            int foodLevel,
            boolean mayFly,
            boolean onGround,
            boolean underWater,
            boolean movingSlowly,
            boolean usingItem,
            boolean blind,
            boolean fallFlying,
            float forwardImpulse) {
        if (forwardImpulse <= 0.0F) {
            return false;
        }
        if (!foodPermitsSprintStart(foodLevel, mayFly)) {
            return false;
        }
        if (usingItem || blind) {
            return false;
        }
        if (movingSlowly && !underWater) {
            return false;
        }
        if (fallFlying && !underWater) {
            return false;
        }
        return onGround || underWater;
    }

    public static boolean sprintClaimIsImpossible(
            boolean sprintFlag,
            int foodLevel,
            boolean mayFly,
            boolean onGround,
            boolean underWater,
            boolean movingSlowly,
            boolean usingItem,
            boolean blind,
            boolean fallFlying,
            float forwardImpulse) {
        if (!sprintFlag) {
            return false;
        }
        return !sprintStartIsPossible(
            foodLevel, mayFly, onGround, underWater,
            movingSlowly, usingItem, blind, fallFlying, forwardImpulse);
    }

    public static Reality coastingBoundFrom(double previousHorizontal, double decay) {
        return Reality.of(previousHorizontal * decay);
    }
}
