package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.FractionValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableDurability;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Era;

public final class Unbreaking {

    private static final LevelValue DATA_DRIVEN_ARMOR_CHANCE = new FractionValue(
        new LinearValue(2.0F, 2.0F),
        new LinearValue(10.0F, 5.0F));

    private static final LevelValue DATA_DRIVEN_CHANCE = new FractionValue(
        new LinearValue(1.0F, 1.0F),
        new LinearValue(2.0F, 1.0F));

    private static final float LEGACY_ARMOR_CONSUME_CHANCE = 0.6F;

    public static final String KEY = "unbreaking";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableDurability.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(5.0F, 8.0F),
        new LinearValue(55.0F, 8.0F),
        2);

    private Unbreaking() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float skipChance(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return DATA_DRIVEN_CHANCE.calculate(level);
        }
        return (float) level / (float) (level + 1);
    }

    public static float armorSkipChance(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return DATA_DRIVEN_ARMOR_CHANCE.calculate(level);
        }
        return skipChance(level, era) * (1.0F - LEGACY_ARMOR_CONSUME_CHANCE);
    }

    public static double expectedDurabilityPerUse(int level, Era era) {
        return 1.0D - (double) skipChance(level, era);
    }

    public static double expectedArmorDurabilityPerUse(int level, Era era) {
        return 1.0D - (double) armorSkipChance(level, era);
    }

    public static double expectedUsesFor(int durability, int level, Era era) {
        double perUse = expectedDurabilityPerUse(level, era);
        if (perUse <= 0.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) durability / perUse;
    }

    public static double expectedArmorUsesFor(int durability, int level, Era era) {
        double perUse = expectedArmorDurabilityPerUse(level, era);
        if (perUse <= 0.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) durability / perUse;
    }

    public static float skipChanceFor(int level, boolean armor, Era era) {
        return armor ? armorSkipChance(level, era) : skipChance(level, era);
    }

    public static double expectedUsesFor(int durability, int level, boolean armor, Era era) {
        return armor
            ? expectedArmorUsesFor(durability, level, era)
            : expectedUsesFor(durability, level, era);
    }
}
