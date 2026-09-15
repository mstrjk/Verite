package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableArmor;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetArmor;
import teacommontea.veritechasse.vanilla.Era;

public final class FireProtection {

    public static final String KEY = "fire_protection";
    public static final int VANILLA_MAX_LEVEL = 4;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableArmor.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetArmor.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(10.0F, 8.0F),
        new LinearValue(18.0F, 8.0F),
        2);

    private static final LevelValue DAMAGE_PROTECTION = new LinearValue(2.0F, 2.0F);
    private static final LevelValue BURNING_TIME = new LinearValue(-0.15F, -0.15F);

    private static final float LEGACY_MULTIPLIER = 2.0F;
    private static final float LEGACY_BURN_REDUCTION = 0.15F;

    private FireProtection() {
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

    public static float burningTimeMultiplier(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return BURNING_TIME.calculate(level);
        }
        return -((float) level * LEGACY_BURN_REDUCTION);
    }

    public static int burnTicksAfter(int burnTicks, int level, Era era) {
        if (level <= 0) {
            return burnTicks;
        }
        float reduction = Math.abs(burningTimeMultiplier(level, era));
        return burnTicks - (int) Math.floor((double) ((float) burnTicks * reduction));
    }
}
