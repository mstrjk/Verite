package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak;

import teacommontea.veritechasse.Vanilla.Reality;

public final class BreakProgress {

    public static final String KEY = "break_progress";

    public static final float SERVER_COMPLETION_THRESHOLD = 0.7F;
    public static final float CLIENT_COMPLETION_THRESHOLD = 1.0F;
    public static final float INSTANT_BREAK_THRESHOLD = 1.0F;

    public static final int CORRECT_TOOL_DIVISOR = 30;
    public static final int INCORRECT_TOOL_DIVISOR = 100;

    public static final float UNBREAKABLE_DESTROY_SPEED = -1.0F;

    public static final int DESTROY_DELAY_TICKS = 5;
    public static final int DESTROY_STAGE_COUNT = 10;

    public static final float PROGRESS_TOLERANCE = 1.0E-4F;

    private BreakProgress() {
    }

    public static int toolDivisor(boolean hasCorrectTool) {
        return hasCorrectTool ? CORRECT_TOOL_DIVISOR : INCORRECT_TOOL_DIVISOR;
    }

    public static boolean isUnbreakable(float blockDestroySpeed) {
        return blockDestroySpeed == UNBREAKABLE_DESTROY_SPEED;
    }

    public static float perTickProgress(
            float playerDestroySpeed,
            float blockDestroySpeed,
            boolean hasCorrectTool) {
        if (isUnbreakable(blockDestroySpeed)) {
            return 0.0F;
        }
        return playerDestroySpeed / blockDestroySpeed / (float) toolDivisor(hasCorrectTool);
    }

    public static boolean breaksInstantly(
            float playerDestroySpeed,
            float blockDestroySpeed,
            boolean hasCorrectTool) {
        return perTickProgress(playerDestroySpeed, blockDestroySpeed, hasCorrectTool)
            >= INSTANT_BREAK_THRESHOLD;
    }

    public static float serverProgress(float perTickProgress, int ticksSpentDestroying) {
        if (ticksSpentDestroying < 0) {
            return 0.0F;
        }
        return perTickProgress * (float) (ticksSpentDestroying + 1);
    }

    public static boolean serverAcceptsBreak(float perTickProgress, int ticksSpentDestroying) {
        return serverProgress(perTickProgress, ticksSpentDestroying)
            >= SERVER_COMPLETION_THRESHOLD;
    }

    public static int minimumTicksForServerBreak(float perTickProgress) {
        if (perTickProgress <= 0.0F) {
            return Integer.MAX_VALUE;
        }
        if (perTickProgress >= INSTANT_BREAK_THRESHOLD) {
            return 0;
        }
        int ticks = (int) Math.ceil(
            (double) SERVER_COMPLETION_THRESHOLD / (double) perTickProgress) - 1;
        return ticks < 0 ? 0 : ticks;
    }

    public static int minimumTicksForClientBreak(float perTickProgress) {
        if (perTickProgress <= 0.0F) {
            return Integer.MAX_VALUE;
        }
        if (perTickProgress >= INSTANT_BREAK_THRESHOLD) {
            return 0;
        }
        return (int) Math.ceil(
            (double) CLIENT_COMPLETION_THRESHOLD / (double) perTickProgress);
    }

    public static boolean breakWasTooFast(
            float perTickProgress,
            int ticksSpentDestroying) {
        if (perTickProgress <= 0.0F) {
            return true;
        }
        return serverProgress(perTickProgress, ticksSpentDestroying)
            < SERVER_COMPLETION_THRESHOLD - PROGRESS_TOLERANCE;
    }

    public static int destroyStage(float progress) {
        return (int) (progress * (float) DESTROY_STAGE_COUNT);
    }

    public static boolean delayElapsed(int ticksSinceLastBreak) {
        return ticksSinceLastBreak >= DESTROY_DELAY_TICKS;
    }

    public static Reality progressFrom(float perTickProgress, int ticksSpentDestroying) {
        return Reality.of(serverProgress(perTickProgress, ticksSpentDestroying));
    }

    public static Reality minimumTicksFrom(float perTickProgress) {
        int ticks = minimumTicksForServerBreak(perTickProgress);
        if (ticks == Integer.MAX_VALUE) {
            return Reality.impossible();
        }
        return Reality.of(ticks);
    }
}
