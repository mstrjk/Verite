package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableMiningLoot;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetMining;

public final class Fortune {

    public static final String KEY = "fortune";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableMiningLoot.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetMining.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(15.0F, 9.0F),
        new LinearValue(65.0F, 9.0F),
        4);

    private Fortune() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static int oreDropsMax(int baseCount, int level) {
        if (level <= 0) {
            return baseCount;
        }
        return baseCount * (level + 1);
    }

    public static double oreDropsExpected(int baseCount, int level) {
        if (level <= 0) {
            return baseCount;
        }

        int outcomes = level + 2;
        double total = 0.0D;
        for (int roll = 0; roll < outcomes; roll++) {
            int bonus = roll - 1;
            if (bonus < 0) {
                bonus = 0;
            }
            total += (double) (bonus + 1);
        }
        return (double) baseCount * total / (double) outcomes;
    }

    public static int uniformBonusMax(int baseCount, int level, int bonusMultiplier) {
        if (level <= 0) {
            return baseCount;
        }
        return baseCount + bonusMultiplier * level;
    }

    public static double uniformBonusExpected(int baseCount, int level, int bonusMultiplier) {
        if (level <= 0) {
            return baseCount;
        }
        return (double) baseCount + (double) (bonusMultiplier * level) / 2.0D;
    }

    public static int binomialMax(int baseCount, int level, int extraRounds) {
        if (level + extraRounds <= 0) {
            return baseCount;
        }
        return baseCount + level + extraRounds;
    }

    public static double binomialExpected(int baseCount, int level, int extraRounds, float probability) {
        int rounds = level + extraRounds;
        if (rounds <= 0) {
            return baseCount;
        }
        return (double) baseCount + (double) rounds * (double) probability;
    }
}
