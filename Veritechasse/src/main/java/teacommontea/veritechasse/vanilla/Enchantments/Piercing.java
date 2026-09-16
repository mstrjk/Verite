package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableCrossbow;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.ExclusiveSetCrossbow;
import teacommontea.veritechasse.Vanilla.Era;

public final class Piercing {

    public static final String KEY = "piercing";
    public static final int VANILLA_MAX_LEVEL = 4;
    public static final int WEIGHT = 10;

    public static final String SUPPORTED_ITEMS = EnchantableCrossbow.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetCrossbow.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(1.0F, 10.0F),
        new LinearValue(50.0F, 0.0F),
        1);

    private static final LevelValue PROJECTILE_PIERCING = new LinearValue(1.0F, 1.0F);

    private Piercing() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static int piercingCount(int level, Era era) {
        if (level <= 0) {
            return 0;
        }
        return (int) PROJECTILE_PIERCING.calculate(level);
    }

    public static int entitiesPassedThrough(int level, Era era) {
        return piercingCount(level, era);
    }
}
