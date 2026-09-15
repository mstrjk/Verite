package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerConsume;

import teacommontea.veritechasse.vanilla.PlayerInteraction.Tags.ConsumeDuration;
import teacommontea.veritechasse.vanilla.Protocol;

public final class ConsumeState {

    public static final String KEY = "consume_state";

    public static final int NOT_USING = 0;

    public static final int EFFECT_TRIGGER_REMAINING = 7;

    public static final int FINISH_PARTICLE_COUNT = 16;

    private ConsumeState() {
    }

    public static int remainingAfterTick(int remaining) {
        if (remaining <= NOT_USING) {
            return NOT_USING;
        }
        return remaining - 1;
    }

    public static boolean completesOnTick(int remainingBeforeTick, boolean useOnRelease) {
        if (useOnRelease) {
            return false;
        }
        return remainingAfterTick(remainingBeforeTick) == NOT_USING
            && remainingBeforeTick > NOT_USING;
    }

    public static int elapsed(String itemName, int remaining) {
        int total = ConsumeDuration.ticksOf(itemName);
        if (total == ConsumeDuration.NOT_CONSUMABLE) {
            return 0;
        }
        int used = total - remaining;
        return used < 0 ? 0 : used;
    }

    public static boolean interruptedByItemChange(String heldBefore, String heldNow) {
        if (heldBefore == null) {
            return false;
        }
        return !heldBefore.equals(heldNow);
    }

    public static boolean completedTooEarly(
            String itemName,
            int observedElapsedTicks,
            Protocol protocol) {
        int required = ConsumeDuration.ticksOf(itemName);
        if (required == ConsumeDuration.NOT_CONSUMABLE) {
            return false;
        }
        return observedElapsedTicks < required;
    }

    public static boolean isConsuming(String itemName, int remaining, Protocol protocol) {
        return remaining > NOT_USING && UseAnimations.isUsable(itemName, protocol);
    }
}
