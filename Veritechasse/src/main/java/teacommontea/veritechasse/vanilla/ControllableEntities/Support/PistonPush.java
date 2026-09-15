package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import teacommontea.veritechasse.vanilla.Reality;

public final class PistonPush {

    public static final double TICK_MOVEMENT = 0.51D;

    public static final int EXTEND_TICKS = 2;
    public static final float SHORT_HEAD_PROGRESS = 0.25F;

    public static final int MAX_PUSHED_BLOCKS = 12;

    public static final boolean MOVES_ANY_ENTITY = true;

    private PistonPush() {
    }

    public static float extendedProgress(float progress, boolean extending) {
        return extending ? progress - 1.0F : 1.0F - progress;
    }

    public static double displacement(float progress, float previousProgress) {
        return (progress - previousProgress) * TICK_MOVEMENT;
    }

    public static double maximumDisplacementPerTick() {
        return TICK_MOVEMENT;
    }

    public static boolean displacementIsPossible(double observed) {
        return Math.abs(observed) <= TICK_MOVEMENT;
    }

    public static boolean blockCountIsLegal(int pushedBlocks) {
        return pushedBlocks >= 0 && pushedBlocks <= MAX_PUSHED_BLOCKS;
    }

    public static Reality displacementFrom() {
        return Reality.of(TICK_MOVEMENT);
    }
}
