package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableTrident;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetRiptide;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Era;

public final class Riptide {

    private static final LevelValue DATA_DRIVEN = new LinearValue(1.5F, 0.75F);

    private static final float LEGACY_SCALE = 3.0F;
    private static final float LEGACY_DIVISOR = 4.0F;
    private static final float LEGACY_THROW_POWER_PER_LEVEL = 0.5F;

    public static final int SPIN_ATTACK_TICKS = 20;
    public static final float SPIN_ATTACK_DAMAGE = 8.0F;
    public static final double GROUND_LAUNCH_LIFT = 1.1999999D;

    public static final String KEY = "riptide";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableTrident.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetRiptide.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(17.0F, 7.0F),
        new LinearValue(50.0F, 0.0F),
        4);

    private Riptide() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float spinAttackStrength(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return DATA_DRIVEN.calculate(level);
        }
        return LEGACY_SCALE * ((1.0F + (float) level) / LEGACY_DIVISOR);
    }

    public static float additionalThrowPower(int level, Era era) {
        if (level <= 0 || era == Era.ENCHANTS_AS_DATA) {
            return 0.0F;
        }
        return (float) level * LEGACY_THROW_POWER_PER_LEVEL;
    }
}
