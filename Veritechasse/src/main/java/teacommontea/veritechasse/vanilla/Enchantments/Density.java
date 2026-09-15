package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableMace;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetDamage;
import teacommontea.veritechasse.vanilla.Era;

public final class Density {

    public static final String KEY = "density";
    public static final int VANILLA_MAX_LEVEL = 5;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableMace.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetDamage.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(5.0F, 8.0F),
        new LinearValue(25.0F, 8.0F),
        2);

    private static final LevelValue SMASH_DAMAGE_PER_BLOCK = new LinearValue(0.5F, 0.5F);

    private Density() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float smashDamagePerFallenBlock(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return SMASH_DAMAGE_PER_BLOCK.calculate(level);
    }

    public static float bonusSmashDamage(int level, double blocksFallen, Era era) {
        if (level <= 0 || blocksFallen <= 0.0D) {
            return 0.0F;
        }
        return (float) blocksFallen * smashDamagePerFallenBlock(level, era);
    }
}
