package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import teacommontea.veritechasse.vanilla.Reality;

public final class BlockEjection {

    public static final float SPEED_BASE = 0.1F;
    public static final float SPEED_RANDOM_RANGE = 0.2F;

    public static final float MINIMUM_SPEED = SPEED_BASE;
    public static final float MAXIMUM_SPEED = SPEED_BASE + SPEED_RANDOM_RANGE;

    public static final double RETAINED_MOVEMENT_SCALE = 0.75D;

    public static final boolean PREFERS_HORIZONTAL = true;

    private BlockEjection() {
    }

    public static double maximumHorizontalImpulse() {
        return MAXIMUM_SPEED;
    }

    public static double minimumHorizontalImpulse() {
        return MINIMUM_SPEED;
    }

    public static boolean speedIsPossible(double observed) {
        double magnitude = Math.abs(observed);
        return magnitude >= 0.0D && magnitude <= MAXIMUM_SPEED;
    }

    public static double retainedOtherAxis(double otherAxisMovement) {
        return otherAxisMovement * RETAINED_MOVEMENT_SCALE;
    }

    public static Reality horizontalFrom() {
        return Reality.of(MAXIMUM_SPEED);
    }
}
