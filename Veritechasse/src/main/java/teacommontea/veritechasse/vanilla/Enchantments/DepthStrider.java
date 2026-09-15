package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableFootArmor;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetBoots;
import teacommontea.veritechasse.vanilla.Era;

public final class DepthStrider {

    public static final String KEY = "depth_strider";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableFootArmor.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetBoots.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(10.0F, 10.0F),
        new LinearValue(25.0F, 10.0F),
        4);

    public static final float WATER_SPEED_TARGET = 0.54600006F;
    public static final float AIRBORNE_SCALE = 0.5F;

    private static final LevelValue WATER_MOVEMENT_EFFICIENCY = new LinearValue(0.33333334F, 0.33333334F);

    private static final float LEGACY_CLAMP = 3.0F;

    private DepthStrider() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float waterMovementEfficiency(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return WATER_MOVEMENT_EFFICIENCY.calculate(level);
        }

        float effective = (float) level;
        if (effective > LEGACY_CLAMP) {
            effective = LEGACY_CLAMP;
        }
        return effective / LEGACY_CLAMP;
    }

    public static float waterSlowDown(float baseSlowDown, int level, boolean onGround, Era era) {
        float efficiency = waterMovementEfficiency(level, era);
        if (!onGround) {
            efficiency *= AIRBORNE_SCALE;
        }
        if (efficiency <= 0.0F) {
            return baseSlowDown;
        }
        return baseSlowDown + (WATER_SPEED_TARGET - baseSlowDown) * efficiency;
    }
}
