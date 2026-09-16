package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableLunge;
import teacommontea.veritechasse.Vanilla.Era;

public final class Lunge {

    public static final String KEY = "lunge";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableLunge.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(5.0F, 8.0F),
        new LinearValue(25.0F, 8.0F),
        2);

    public static final int DURABILITY_COST = 1;

    private static final LevelValue EXHAUSTION = new LinearValue(4.0F, 4.0F);
    private static final LevelValue IMPULSE_MAGNITUDE = new LinearValue(0.458F, 0.458F);

    private Lunge() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float exhaustion(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return EXHAUSTION.calculate(level);
    }

    public static float impulseMagnitude(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return IMPULSE_MAGNITUDE.calculate(level);
    }

    public static double horizontalImpulse(int level, Era era) {
        return (double) impulseMagnitude(level, era);
    }

    public static boolean applies(int level, boolean riding) {
        return level > 0 && !riding;
    }
}
