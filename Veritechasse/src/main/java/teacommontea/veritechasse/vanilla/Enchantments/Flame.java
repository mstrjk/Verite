package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableBow;

public final class Flame {

    public static final String KEY = "flame";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableBow.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(20.0F, 0.0F),
        new LinearValue(50.0F, 0.0F),
        4);

    private static final LevelValue IGNITE_SECONDS = new LinearValue(100.0F, 0.0F);

    private Flame() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float igniteSeconds(int level) {
        if (level <= 0) {
            return 0.0F;
        }
        return IGNITE_SECONDS.calculate(level);
    }

    public static int igniteTicks(int level) {
        return (int) (igniteSeconds(level) * 20.0F);
    }
}
