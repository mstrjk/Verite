package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableSharpWeapon;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetDamage;
import teacommontea.veritechasse.vanilla.Era;

public final class Sharpness {

    public static final String KEY = "sharpness";
    public static final int VANILLA_MAX_LEVEL = 5;
    public static final int WEIGHT = 10;

    public static final String SUPPORTED_ITEMS = EnchantableSharpWeapon.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetDamage.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(1.0F, 11.0F),
        new LinearValue(21.0F, 11.0F),
        1);

    private static final LevelValue DAMAGE = new LinearValue(1.0F, 0.5F);

    private Sharpness() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float bonusDamage(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        if (era == Era.ENCHANTS_AS_DATA) {
            return DAMAGE.calculate(level);
        }
        return 1.0F + (float) Math.max(0, level - 1) * 0.5F;
    }
}
