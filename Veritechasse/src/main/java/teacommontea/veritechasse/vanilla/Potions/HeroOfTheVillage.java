package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Reality;

public final class HeroOfTheVillage {

    public static final String KEY = "hero_of_the_village";
    public static final int VANILLA_MAX_AMPLIFIER = 4;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final int RAID_REWARD_DURATION_TICKS = 48000;

    public static final double DISCOUNT_BASE = 0.3D;
    public static final double DISCOUNT_PER_AMPLIFIER = 0.0625D;
    public static final int MINIMUM_COST_REDUCTION = 1;
    public static final int MINIMUM_TRADE_COST = 1;

    private HeroOfTheVillage() {
    }

    public static double discountModifier(int amplifier) {
        return DISCOUNT_BASE + DISCOUNT_PER_AMPLIFIER * (double) amplifier;
    }

    public static int costReduction(int baseCostCount, int amplifier) {
        int reduction = (int) Math.floor(discountModifier(amplifier) * (double) baseCostCount);
        return Math.max(reduction, MINIMUM_COST_REDUCTION);
    }

    public static int discountedCost(int baseCostCount, int amplifier, int maxStackSize) {
        int reduced = baseCostCount - costReduction(baseCostCount, amplifier);
        if (reduced < MINIMUM_TRADE_COST) {
            return MINIMUM_TRADE_COST;
        }
        return reduced > maxStackSize ? maxStackSize : reduced;
    }

    public static boolean attractsVillagerGifts() {
        return true;
    }

    public static Reality reductionFrom(int baseCostCount, int amplifier) {
        return Reality.of(costReduction(baseCostCount, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
