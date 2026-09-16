package teacommontea.veritechasse.Vanilla.ControllableEntities.Support;

import teacommontea.veritechasse.Vanilla.Reality;

public final class CollisionPush {

    public static final double PUSH_SCALE = 0.05D;
    public static final double MINIMUM_SEPARATION = 0.01D;
    public static final double MAXIMUM_POWER = 1.0D;

    public static final boolean VEHICLES_ARE_PUSHED = false;

    private CollisionPush() {
    }

    public static boolean separated(double absMaxAxisDistance) {
        return absMaxAxisDistance >= MINIMUM_SEPARATION;
    }

    public static double power(double absMaxAxisDistance) {
        if (!separated(absMaxAxisDistance)) {
            return 0.0D;
        }
        double root = Math.sqrt(absMaxAxisDistance);
        if (root <= 0.0D) {
            return 0.0D;
        }
        double power = 1.0D / root;
        return power > MAXIMUM_POWER ? MAXIMUM_POWER : power;
    }

    public static double impulse(double axisDistance, double absMaxAxisDistance) {
        if (!separated(absMaxAxisDistance)) {
            return 0.0D;
        }
        double root = Math.sqrt(absMaxAxisDistance);
        if (root <= 0.0D) {
            return 0.0D;
        }
        return axisDistance / root * power(absMaxAxisDistance) * PUSH_SCALE;
    }

    public static double maximumImpulse() {
        return PUSH_SCALE;
    }

    public static double maximumHorizontalImpulse() {
        return PUSH_SCALE * Math.sqrt(2.0D);
    }

    public static double maximumHorizontalImpulse(int simultaneousPushers) {
        if (simultaneousPushers <= 0) {
            return 0.0D;
        }
        return maximumHorizontalImpulse() * (double) simultaneousPushers;
    }

    public static boolean canPush(boolean isVehicle, boolean pushable) {
        if (isVehicle) {
            return VEHICLES_ARE_PUSHED;
        }
        return pushable;
    }

    public static boolean verticalPushIsPossible() {
        return false;
    }

    public static Reality impulseFrom(double axisDistance, double absMaxAxisDistance) {
        return Reality.of(impulse(axisDistance, absMaxAxisDistance));
    }

    public static Reality maximumFrom() {
        return Reality.of(maximumImpulse());
    }
}
