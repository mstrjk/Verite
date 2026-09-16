package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import java.util.Locale;

import teacommontea.veritechasse.vanilla.Reality;

public final class BubbleColumn {

    public static final String KEY = "bubble_column";
    public static final String SOUL_SAND = "soul_sand";
    public static final String MAGMA_BLOCK = "magma_block";

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

    private static String normalise(String blockName) {
        return blockName == null ? null : blockName.toLowerCase(Locale.ROOT);
    }

    public static boolean dragsDown(String blockBeneathColumn) {
        return MAGMA_BLOCK.equals(normalise(blockBeneathColumn));
    }

    public static boolean pushesUp(String blockBeneathColumn) {
        return SOUL_SAND.equals(normalise(blockBeneathColumn));
    }

    public static boolean formsColumn(String blockBeneathColumn) {
        return dragsDown(blockBeneathColumn) || pushesUp(blockBeneathColumn);
    }

    public static boolean isColumn(String blockHere) {
        return KEY.equals(normalise(blockHere));
    }

    public static boolean resetsFallDistance(boolean above) {
        return above ? false : RESETS_FALL_DISTANCE_INSIDE;
    }

    public static double verticalAfter(
            double verticalMovement,
            String blockBeneathColumn,
            boolean above) {
        boolean dragDown = dragsDown(blockBeneathColumn);
        return above
            ? verticalAfterAbove(verticalMovement, dragDown)
            : verticalAfterInside(verticalMovement, dragDown);
    }

    public static double maximumDownward(String blockBeneathColumn, boolean above) {
        if (!dragsDown(blockBeneathColumn)) {
            return 0.0D;
        }
        return maximumDownward(above);
    }

    public static double maximumUpward(String blockBeneathColumn, boolean above) {
        if (!pushesUp(blockBeneathColumn)) {
            return 0.0D;
        }
        return maximumUpward(above);
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
