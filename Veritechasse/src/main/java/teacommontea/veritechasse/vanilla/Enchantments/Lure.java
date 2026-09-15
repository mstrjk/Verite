package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableFishing;
import teacommontea.veritechasse.vanilla.Era;

public final class Lure {

    public static final String KEY = "lure";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableFishing.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(15.0F, 9.0F),
        new LinearValue(65.0F, 9.0F),
        4);

    public static final int MIN_WAIT_TICKS = 100;
    public static final int MAX_WAIT_TICKS = 600;

    private static final LevelValue TIME_REDUCTION = new LinearValue(5.0F, 5.0F);

    private Lure() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float timeReductionSeconds(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return TIME_REDUCTION.calculate(level);
    }

    public static int minWaitTicks(int level, Era era) {
        int reduced = MIN_WAIT_TICKS - (int) (timeReductionSeconds(level, era) * 20.0F);
        return reduced < 0 ? 0 : reduced;
    }

    public static int maxWaitTicks(int level, Era era) {
        int reduced = MAX_WAIT_TICKS - (int) (timeReductionSeconds(level, era) * 20.0F);
        return reduced < 0 ? 0 : reduced;
    }
}
