package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelsSquaredValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableMining;
import teacommontea.veritechasse.Vanilla.Era;

public final class Efficiency {

    public static final String KEY = "efficiency";
    public static final int VANILLA_MAX_LEVEL = 5;
    public static final int WEIGHT = 10;

    public static final String SUPPORTED_ITEMS = EnchantableMining.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(1.0F, 10.0F),
        new LinearValue(51.0F, 10.0F),
        1);

    private static final LevelValue MINING_EFFICIENCY = new LevelsSquaredValue(1.0F);

    private Efficiency() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float miningEfficiency(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return MINING_EFFICIENCY.calculate(level);
    }
}
