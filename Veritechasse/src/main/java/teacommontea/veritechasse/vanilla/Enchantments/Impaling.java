package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableTrident;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.ExclusiveSetDamage;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Era;

public final class Impaling {

    private static final LevelValue DAMAGE = new LinearValue(2.5F, 2.5F);

    public static final String KEY = "impaling";
    public static final int VANILLA_MAX_LEVEL = 5;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableTrident.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetDamage.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(1.0F, 8.0F),
        new LinearValue(21.0F, 8.0F),
        4);

    private Impaling() {
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
        return DAMAGE.calculate(level);
    }

    public static float bonusDamageAgainst(int level, boolean sensitiveToImpaling, Era era) {
        if (!sensitiveToImpaling) {
            return 0.0F;
        }
        return bonusDamage(level, era);
    }
}
