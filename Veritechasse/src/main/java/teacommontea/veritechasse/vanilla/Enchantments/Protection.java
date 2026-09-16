package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableArmor;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.ExclusiveSetArmor;
import teacommontea.veritechasse.Vanilla.Era;

public final class Protection {

    public static final String KEY = "protection";
    public static final int VANILLA_MAX_LEVEL = 4;
    public static final int WEIGHT = 10;

    public static final String SUPPORTED_ITEMS = EnchantableArmor.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetArmor.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(1.0F, 11.0F),
        new LinearValue(12.0F, 11.0F),
        1);

    private static final LevelValue DAMAGE_PROTECTION = new LinearValue(1.0F, 1.0F);

    private static final float LEGACY_MULTIPLIER = 1.0F;

    private Protection() {
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
}
