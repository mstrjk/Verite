package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableMeleeWeapon;
import teacommontea.veritechasse.vanilla.Era;

public final class Knockback {

    public static final String KEY = "knockback";
    public static final int VANILLA_MAX_LEVEL = 2;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableMeleeWeapon.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(5.0F, 20.0F),
        new LinearValue(55.0F, 20.0F),
        2);

    public static final double SPRINT_BONUS = 0.5D;
    public static final double VERTICAL_CAP = 0.4D;

    private static final LevelValue KNOCKBACK = new LinearValue(1.0F, 1.0F);

    private Knockback() {
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
        return KNOCKBACK.calculate(level);
    }

    public static double power(int level, boolean sprinting, Era era) {
        double power = (double) knockback(level, era);
        if (sprinting) {
            power += SPRINT_BONUS;
        }
        return power;
    }

    public static double horizontalImpulse(int level, boolean sprinting, double knockbackResistance, Era era) {
        double power = power(level, sprinting, era) * (1.0D - knockbackResistance);
        return power <= 0.0D ? 0.0D : power;
    }

    public static double verticalImpulse(double currentY, int level, boolean sprinting, boolean onGround, double knockbackResistance, Era era) {
        if (!onGround) {
            return currentY;
        }
        double power = power(level, sprinting, era) * (1.0D - knockbackResistance);
        if (power <= 0.0D) {
            return currentY;
        }
        return Math.min(VERTICAL_CAP, currentY / 2.0D + power);
    }
}
