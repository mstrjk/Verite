package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableDurability;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.ExclusiveSetBow;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;

public final class Mending {

    public static final float DURABILITY_PER_XP = 2.0F;

    public static final String KEY = "mending";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableDurability.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetBow.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(25.0F, 25.0F),
        new LinearValue(75.0F, 25.0F),
        4);

    private Mending() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static int repairFrom(int experience) {
        if (experience <= 0) {
            return 0;
        }
        return (int) ((float) experience * DURABILITY_PER_XP);
    }

    public static boolean repairIsPossible(int observedRepair, int experienceAbsorbed) {
        return observedRepair <= repairFrom(experienceAbsorbed);
    }
}
