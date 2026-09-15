package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableMace;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetDamage;
import teacommontea.veritechasse.vanilla.Era;

public final class Breach {

    public static final String KEY = "breach";
    public static final int VANILLA_MAX_LEVEL = 4;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableMace.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetDamage.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(15.0F, 9.0F),
        new LinearValue(65.0F, 9.0F),
        4);

    private static final LevelValue ARMOR_EFFECTIVENESS = new LinearValue(-0.15F, -0.15F);

    private Breach() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float armorEffectivenessModifier(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return ARMOR_EFFECTIVENESS.calculate(level);
    }

    public static float armorEffectiveness(float baseFraction, int level, Era era) {
        float modified = baseFraction + armorEffectivenessModifier(level, era);
        if (modified < 0.0F) {
            return 0.0F;
        }
        return modified > 1.0F ? 1.0F : modified;
    }
}
