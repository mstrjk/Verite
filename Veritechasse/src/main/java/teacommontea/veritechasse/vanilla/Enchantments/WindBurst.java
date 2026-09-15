package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LookupValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableMace;
import teacommontea.veritechasse.vanilla.Era;

public final class WindBurst {

    public static final String KEY = "wind_burst";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableMace.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(15.0F, 9.0F),
        new LinearValue(65.0F, 9.0F),
        4);

    public static final float EXPLOSION_RADIUS = 3.5F;

    private static final LevelValue KNOCKBACK_MULTIPLIER = new LookupValue(
        new float[] {1.2F, 1.75F, 2.2F},
        new LinearValue(1.5F, 0.35F));

    private WindBurst() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float knockbackMultiplier(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return KNOCKBACK_MULTIPLIER.calculate(level);
    }

    public static double launchImpulse(double baseImpulse, int level, Era era) {
        if (level <= 0) {
            return baseImpulse;
        }
        return baseImpulse * (double) knockbackMultiplier(level, era);
    }
}
