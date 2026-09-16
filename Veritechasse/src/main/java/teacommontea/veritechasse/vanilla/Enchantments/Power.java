package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableBow;
import teacommontea.veritechasse.Vanilla.Era;

public final class Power {

    public static final String KEY = "power";
    public static final int VANILLA_MAX_LEVEL = 5;
    public static final int WEIGHT = 10;

    public static final String SUPPORTED_ITEMS = EnchantableBow.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(1.0F, 10.0F),
        new LinearValue(16.0F, 10.0F),
        1);

    private static final LevelValue DAMAGE = new LinearValue(1.0F, 0.5F);

    private Power() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float bonusDamage(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return DAMAGE.calculate(level);
        }
        return (float) level * 0.5F + 0.5F;
    }
}
