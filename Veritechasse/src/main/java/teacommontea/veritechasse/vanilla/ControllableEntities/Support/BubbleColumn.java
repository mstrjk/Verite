package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import teacommontea.veritechasse.vanilla.Reality;

public final class BubbleColumn {

    public static final double ABOVE_UPWARD_STEP = 0.1D;
    public static final double ABOVE_UPWARD_CAP = 1.8D;
    public static final double ABOVE_DOWNWARD_STEP = -0.03D;
    public static final double ABOVE_DOWNWARD_CAP = -0.9D;

    public static final double INSIDE_UPWARD_STEP = 0.06D;
    public static final double INSIDE_UPWARD_CAP = 0.7D;
    public static final double INSIDE_DOWNWARD_STEP = -0.03D;
    public static final double INSIDE_DOWNWARD_CAP = -0.3D;

    public static final int CHECK_PERIOD = 5;

    public static final boolean RESETS_FALL_DISTANCE_INSIDE = true;

    private BubbleColumn() {
    }

    public static double verticalAfterAbove(double verticalMovement, boolean dragDown) {
        if (dragDown) {
            double next = verticalMovement + ABOVE_DOWNWARD_STEP;
            return next < ABOVE_DOWNWARD_CAP ? ABOVE_DOWNWARD_CAP : next;
        }
        double next = verticalMovement + ABOVE_UPWARD_STEP;
        return next > ABOVE_UPWARD_CAP ? ABOVE_UPWARD_CAP : next;
    }

    public static double verticalAfterInside(double verticalMovement, boolean dragDown) {
        if (dragDown) {
            double next = verticalMovement + INSIDE_DOWNWARD_STEP;
            return next < INSIDE_DOWNWARD_CAP ? INSIDE_DOWNWARD_CAP : next;
        }
        double next = verticalMovement + INSIDE_UPWARD_STEP;
        return next > INSIDE_UPWARD_CAP ? INSIDE_UPWARD_CAP : next;
    }

    public static double maximumUpward(boolean above) {
        return above ? ABOVE_UPWARD_CAP : INSIDE_UPWARD_CAP;
    }

    public static double maximumDownward(boolean above) {
        return above ? ABOVE_DOWNWARD_CAP : INSIDE_DOWNWARD_CAP;
    }

    public static boolean verticalIsPossible(double observedVertical, boolean above) {
        return observedVertical <= maximumUpward(above);
    }

    public static Reality upwardFrom(boolean above) {
        return Reality.of(maximumUpward(above));
    }
}
