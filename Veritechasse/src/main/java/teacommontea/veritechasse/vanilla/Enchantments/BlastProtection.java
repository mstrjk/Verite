package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableArmor;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetArmor;
import teacommontea.veritechasse.vanilla.Era;

public final class BlastProtection {

    public static final String KEY = "blast_protection";
    public static final int VANILLA_MAX_LEVEL = 4;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableArmor.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetArmor.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(5.0F, 8.0F),
        new LinearValue(13.0F, 8.0F),
        4);

    private static final LevelValue DAMAGE_PROTECTION = new LinearValue(2.0F, 2.0F);
    private static final LevelValue KNOCKBACK_RESISTANCE = new LinearValue(0.15F, 0.15F);

    private static final float LEGACY_MULTIPLIER = 2.0F;
    private static final float LEGACY_KNOCKBACK_REDUCTION = 0.15F;

    private BlastProtection() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float damageProtection(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return DAMAGE_PROTECTION.calculate(level);
        }
        return (float) level * LEGACY_MULTIPLIER;
    }

    public static float explosionKnockbackResistance(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return KNOCKBACK_RESISTANCE.calculate(level);
        }
        return (float) level * LEGACY_KNOCKBACK_REDUCTION;
    }

    public static double explosionKnockbackAfter(double knockback, int level, Era era) {
        if (level <= 0) {
            return knockback;
        }
        if (era == Era.ENCHANTS_AS_CLASSES) {
            return knockback - Math.floor(knockback * (double) explosionKnockbackResistance(level, era));
        }
        double resisted = knockback * (1.0D - (double) explosionKnockbackResistance(level, era));
        return resisted < 0.0D ? 0.0D : resisted;
    }
}
