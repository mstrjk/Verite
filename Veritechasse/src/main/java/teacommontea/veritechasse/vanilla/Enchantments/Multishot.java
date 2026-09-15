package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableCrossbow;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetCrossbow;
import teacommontea.veritechasse.vanilla.Era;

public final class Multishot {

    public static final String KEY = "multishot";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableCrossbow.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetCrossbow.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(20.0F, 0.0F),
        new LinearValue(50.0F, 0.0F),
        4);

    public static final int BASE_PROJECTILE_COUNT = 1;
    public static final int LEGACY_PROJECTILE_COUNT = 3;

    private static final LevelValue PROJECTILE_COUNT = new LinearValue(2.0F, 2.0F);
    private static final LevelValue PROJECTILE_SPREAD = new LinearValue(10.0F, 10.0F);

    private Multishot() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static int projectileCount(int level, Era era) {
        if (level <= 0) {
            return BASE_PROJECTILE_COUNT;
        }
        if (era == Era.ENCHANTS_AS_CLASSES) {
            return LEGACY_PROJECTILE_COUNT;
        }
        return BASE_PROJECTILE_COUNT + (int) PROJECTILE_COUNT.calculate(level);
    }

    public static float projectileSpreadDegrees(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return PROJECTILE_SPREAD.calculate(level);
    }
}
