package teacommontea.veritechasse.Vanilla.Tools.Utility;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Brush {

    public static final String KEY = "brush";

    public static final int MAX_DAMAGE = 64;
    public static final int DAMAGE_PER_SUCCESSFUL_BRUSH = 1;

    public static final int USE_DURATION_TICKS = 200;
    public static final int ANIMATION_DURATION_TICKS = 10;
    public static final int BRUSH_TICK_INTERVAL = 10;
    public static final int BRUSH_TICK_OFFSET = 5;

    public static final boolean ENCHANTABLE = false;
    public static final boolean FIRE_RESISTANT = false;

    public static final Set<String> BRUSHABLE_BLOCKS = Set.of(
        "suspicious_sand",
        "suspicious_gravel");

    public static final boolean USE_PERMITS_SPRINTING = false;
    public static final float USE_SPEED_MULTIPLIER = 0.2F;

    public static boolean slowsWhileUsed() {
        return USE_SPEED_MULTIPLIER < 1.0F;
    }

    public static float useSpeedMultiplier() {
        return USE_SPEED_MULTIPLIER;
    }

    public static boolean permitsSprintingWhileUsed() {
        return USE_PERMITS_SPRINTING;
    }

    private Brush() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 19, 4);
    }

    public static boolean brushable(String blockName) {
        if (blockName == null) {
            return false;
        }
        return BRUSHABLE_BLOCKS.contains(blockName.toLowerCase(Locale.ROOT));
    }

    public static int elapsedTicks(int ticksRemaining) {
        return USE_DURATION_TICKS - ticksRemaining + 1;
    }

    public static boolean brushesThisTick(int ticksRemaining) {
        if (ticksRemaining < 0) {
            return false;
        }
        return elapsedTicks(ticksRemaining) % BRUSH_TICK_INTERVAL == BRUSH_TICK_OFFSET;
    }

    public static int brushCountOver(int heldTicks) {
        if (heldTicks <= 0) {
            return 0;
        }
        int capped = heldTicks > USE_DURATION_TICKS ? USE_DURATION_TICKS : heldTicks;
        return (capped + BRUSH_TICK_INTERVAL - BRUSH_TICK_OFFSET) / BRUSH_TICK_INTERVAL;
    }

    public static boolean consumesDurability(boolean brushingUpdatedState) {
        return brushingUpdatedState;
    }

    public static int remaining(int damage) {
        int left = MAX_DAMAGE - damage;
        return left < 0 ? 0 : left;
    }

    public static boolean broken(int damage) {
        return damage >= MAX_DAMAGE;
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(remaining(damage));
    }
}
