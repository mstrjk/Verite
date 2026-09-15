package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import java.util.Locale;
import java.util.Map;

import teacommontea.veritechasse.vanilla.Reality;

public final class BlockMovementFactors {

    public static final float DEFAULT_SPEED_FACTOR = 1.0F;
    public static final float DEFAULT_JUMP_FACTOR = 1.0F;

    public static final float SOUL_SAND_SPEED_FACTOR = 0.4F;
    public static final float HONEY_BLOCK_SPEED_FACTOR = 0.4F;
    public static final float HONEY_BLOCK_JUMP_FACTOR = 0.5F;

    private static final Map<String, Float> SPEED_FACTORS = Map.of(
        "soul_sand", Float.valueOf(SOUL_SAND_SPEED_FACTOR),
        "honey_block", Float.valueOf(HONEY_BLOCK_SPEED_FACTOR));

    private static final Map<String, Float> JUMP_FACTORS = Map.of(
        "honey_block", Float.valueOf(HONEY_BLOCK_JUMP_FACTOR));

    private BlockMovementFactors() {
    }

    public static float speedFactorOf(String blockName) {
        if (blockName == null) {
            return DEFAULT_SPEED_FACTOR;
        }
        Float found = SPEED_FACTORS.get(blockName.toLowerCase(Locale.ROOT));
        return found == null ? DEFAULT_SPEED_FACTOR : found.floatValue();
    }

    public static float jumpFactorOf(String blockName) {
        if (blockName == null) {
            return DEFAULT_JUMP_FACTOR;
        }
        Float found = JUMP_FACTORS.get(blockName.toLowerCase(Locale.ROOT));
        return found == null ? DEFAULT_JUMP_FACTOR : found.floatValue();
    }

    public static float resolveSpeedFactor(String blockHere, String blockBelow, boolean inWaterOrBubble) {
        float here = speedFactorOf(blockHere);
        if (inWaterOrBubble) {
            return here;
        }
        if (here == DEFAULT_SPEED_FACTOR) {
            return speedFactorOf(blockBelow);
        }
        return here;
    }

    public static float resolveJumpFactor(String blockHere, String blockBelow) {
        float here = jumpFactorOf(blockHere);
        if (here == DEFAULT_JUMP_FACTOR) {
            return jumpFactorOf(blockBelow);
        }
        return here;
    }

    public static boolean slowsMovement(String blockName) {
        return speedFactorOf(blockName) < DEFAULT_SPEED_FACTOR;
    }

    public static boolean reducesJump(String blockName) {
        return jumpFactorOf(blockName) < DEFAULT_JUMP_FACTOR;
    }

    public static Reality speedFactorFrom(String blockHere, String blockBelow, boolean inWaterOrBubble) {
        return Reality.of(resolveSpeedFactor(blockHere, blockBelow, inWaterOrBubble));
    }
}
