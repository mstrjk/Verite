package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.FractionValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableSweeping;
import teacommontea.veritechasse.Vanilla.Era;

public final class SweepingEdge {

    public static final String KEY = "sweeping_edge";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableSweeping.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(5.0F, 9.0F),
        new LinearValue(20.0F, 9.0F),
        4);

    private static final LevelValue SWEEPING_DAMAGE_RATIO = new FractionValue(
        new LinearValue(1.0F, 1.0F),
        new LinearValue(2.0F, 1.0F));

    private SweepingEdge() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float sweepingDamageRatio(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return SWEEPING_DAMAGE_RATIO.calculate(level);
    }

    public static float sweepDamage(float attackDamage, int level, Era era) {
        return 1.0F + attackDamage * sweepingDamageRatio(level, era);
    }
}
