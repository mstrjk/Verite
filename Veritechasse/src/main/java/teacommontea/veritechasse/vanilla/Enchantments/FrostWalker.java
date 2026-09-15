package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.ClampedValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.BurnFromStepping;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableFootArmor;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetBoots;
import teacommontea.veritechasse.vanilla.Era;

public final class FrostWalker {

    public static final String KEY = "frost_walker";
    public static final int VANILLA_MAX_LEVEL = 2;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableFootArmor.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetBoots.KEY;
    public static final String IMMUNE_DAMAGE_TAG = BurnFromStepping.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(10.0F, 10.0F),
        new LinearValue(25.0F, 10.0F),
        4);

    public static final String PLACED_BLOCK = "frosted_ice";

    private static final LevelValue RADIUS = new ClampedValue(
        new LinearValue(3.0F, 1.0F), 0.0F, 16.0F);

    private FrostWalker() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float freezeRadius(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return RADIUS.calculate(level);
    }

    public static boolean freezes(int level, boolean onGround, boolean riding) {
        return level > 0 && onGround && !riding;
    }

    public static boolean immuneToSteppingBurn(int level, String damageType) {
        return level > 0 && BurnFromStepping.contains(damageType);
    }
}
