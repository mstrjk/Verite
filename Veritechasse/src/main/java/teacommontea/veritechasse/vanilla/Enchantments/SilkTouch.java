package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableMiningLoot;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.ExclusiveSetMining;

public final class SilkTouch {

    public static final String KEY = "silk_touch";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 1;

    public static final String SUPPORTED_ITEMS = EnchantableMiningLoot.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetMining.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(15.0F, 0.0F),
        new LinearValue(65.0F, 0.0F),
        8);

    public static final float BLOCK_EXPERIENCE = 0.0F;

    private SilkTouch() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static boolean dropsExperience(int level) {
        return level <= 0;
    }
}
