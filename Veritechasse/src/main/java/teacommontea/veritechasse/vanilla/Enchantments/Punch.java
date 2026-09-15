package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableBow;
import teacommontea.veritechasse.vanilla.Era;

public final class Punch {

    public static final String KEY = "punch";
    public static final int VANILLA_MAX_LEVEL = 2;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableBow.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(12.0F, 20.0F),
        new LinearValue(37.0F, 20.0F),
        4);

    private static final LevelValue KNOCKBACK = new LinearValue(1.0F, 1.0F);

    private Punch() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float knockback(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return KNOCKBACK.calculate(level);
        }
        return (float) level;
    }
}
