package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger.FoodConstants;
import teacommontea.veritechasse.vanilla.Reality;

public final class Hunger {

    public static final String KEY = "hunger";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final float EXHAUSTION_PER_AMPLIFIER = 0.005F;

    public static final boolean APPLIES_EVERY_TICK = true;

    private Hunger() {
    }

    public static float exhaustionPerTick(int amplifier) {
        return EXHAUSTION_PER_AMPLIFIER * (float) (amplifier + 1);
    }

    public static boolean appliesThisTick() {
        return APPLIES_EVERY_TICK;
    }

    public static boolean affectsPlayersOnly() {
        return true;
    }

    public static float exhaustionOver(int amplifier, int durationTicks) {
        return exhaustionPerTick(amplifier) * (float) durationTicks;
    }

    public static float exhaustionAfter(float current, int amplifier, int durationTicks) {
        return FoodConstants.addExhaustion(current, exhaustionOver(amplifier, durationTicks));
    }

    public static int ticksPerFoodPoint(int amplifier) {
        float perTick = exhaustionPerTick(amplifier);
        if (perTick <= 0.0F) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.ceil((double) (FoodConstants.EXHAUSTION_DROP / perTick));
    }

    public static Reality exhaustionFrom(int amplifier, int durationTicks) {
        return Reality.of(exhaustionOver(amplifier, durationTicks));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
