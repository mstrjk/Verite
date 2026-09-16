package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableHeadArmor;
import teacommontea.veritechasse.Vanilla.Era;

public final class Respiration {

    public static final String KEY = "respiration";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableHeadArmor.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(10.0F, 10.0F),
        new LinearValue(40.0F, 10.0F),
        4);

    public static final int BASE_AIR_TICKS = 300;

    private static final LevelValue OXYGEN_BONUS = new LinearValue(1.0F, 1.0F);

    private Respiration() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float oxygenBonus(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return OXYGEN_BONUS.calculate(level);
    }

    public static float airLossChancePerTick(int level, Era era) {
        float bonus = oxygenBonus(level, era);
        if (bonus <= 0.0F) {
            return 1.0F;
        }
        return 1.0F / (bonus + 1.0F);
    }

    public static float expectedTicksUnderwater(int level, Era era) {
        float chance = airLossChancePerTick(level, era);
        if (chance <= 0.0F) {
            return Float.POSITIVE_INFINITY;
        }
        return (float) BASE_AIR_TICKS / chance;
    }
}
