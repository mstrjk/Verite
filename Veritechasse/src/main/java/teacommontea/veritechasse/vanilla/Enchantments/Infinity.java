package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableBow;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetBow;

public final class Infinity {

    public static final String KEY = "infinity";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 1;

    public static final String SUPPORTED_ITEMS = EnchantableBow.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetBow.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(20.0F, 0.0F),
        new LinearValue(50.0F, 0.0F),
        8);

    public static final String FREE_AMMO = "arrow";

    private Infinity() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static int ammoUse(int level, String ammoItem) {
        if (level <= 0) {
            return 1;
        }
        return FREE_AMMO.equals(ammoItem) ? 0 : 1;
    }

    public static boolean consumesAmmo(int level, String ammoItem) {
        return ammoUse(level, ammoItem) > 0;
    }
}
