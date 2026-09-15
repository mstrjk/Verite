package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableCrossbow;
import teacommontea.veritechasse.vanilla.Era;

public final class QuickCharge {

    public static final String KEY = "quick_charge";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableCrossbow.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(12.0F, 20.0F),
        new LinearValue(50.0F, 0.0F),
        2);

    public static final float BASE_CHARGE_SECONDS = 1.25F;
    public static final int LEGACY_BASE_TICKS = 25;
    public static final int LEGACY_TICKS_PER_LEVEL = 5;

    private static final LevelValue CHARGE_TIME = new LinearValue(-0.25F, -0.25F);

    private QuickCharge() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float chargeTimeModifier(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return CHARGE_TIME.calculate(level);
    }

    public static int chargeDurationTicks(int level, Era era) {
        if (level <= 0) {
            return LEGACY_BASE_TICKS;
        }

        if (era == Era.ENCHANTS_AS_CLASSES) {
            int ticks = LEGACY_BASE_TICKS - LEGACY_TICKS_PER_LEVEL * level;
            return ticks < 0 ? 0 : ticks;
        }

        float seconds = BASE_CHARGE_SECONDS + chargeTimeModifier(level, era);
        if (seconds < 0.0F) {
            seconds = 0.0F;
        }
        return (int) Math.floor((double) (seconds * 20.0F));
    }
}
