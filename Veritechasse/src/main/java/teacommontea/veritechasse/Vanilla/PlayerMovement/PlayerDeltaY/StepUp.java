package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.Vanilla.Reality;

public final class StepUp {

    public static final String KEY = "step_up";

    public static final double DEFAULT_STEP_HEIGHT = 0.6D;

    public static final double ATTRIBUTE_MINIMUM = 0.0D;

    public static final double ATTRIBUTE_MAXIMUM = 10.0D;

    public static final double RIDDEN_MINIMUM = 1.0D;

    private StepUp() {
    }

    public static double clampAttribute(double value) {
        if (value < ATTRIBUTE_MINIMUM) {
            return ATTRIBUTE_MINIMUM;
        }
        return value > ATTRIBUTE_MAXIMUM ? ATTRIBUTE_MAXIMUM : value;
    }

    public static double maxUpStep(double attributeValue) {
        return clampAttribute(attributeValue);
    }

    public static double maxUpStep() {
        return DEFAULT_STEP_HEIGHT;
    }

    public static double maxUpStepWhenRidden(double attributeValue) {
        return Math.max(clampAttribute(attributeValue), RIDDEN_MINIMUM);
    }

    public static boolean stepIsPossible(
            double stepHeight, boolean onGround, boolean horizontalCollision) {
        return stepHeight > 0.0D && onGround && horizontalCollision;
    }

    public static boolean withinStepHeight(double risen, double stepHeight) {
        return risen <= clampAttribute(stepHeight);
    }

    public static boolean explainsRise(
            double risen,
            double stepHeight,
            boolean onGround,
            boolean horizontalCollision) {
        if (!stepIsPossible(stepHeight, onGround, horizontalCollision)) {
            return false;
        }
        return risen > 0.0D && withinStepHeight(risen, stepHeight);
    }

    public static boolean appliedAsDisplacementNotVelocity() {
        return true;
    }

    public static boolean leavesDeltaYUnchanged() {
        return true;
    }

    public static Reality maximumRise(double stepHeight) {
        return Reality.of(clampAttribute(stepHeight));
    }
}
