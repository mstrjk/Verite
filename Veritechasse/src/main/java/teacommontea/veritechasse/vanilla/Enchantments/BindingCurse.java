package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableEquippable;

public final class BindingCurse {

    public static final String KEY = "binding_curse";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 1;

    public static final String SUPPORTED_ITEMS = EnchantableEquippable.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(25.0F, 0.0F),
        new LinearValue(50.0F, 0.0F),
        8);

    private BindingCurse() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static boolean preventsArmorChange(int level) {
        return level > 0;
    }

    public static boolean canBeRemoved(int level, boolean creative, boolean itemBroke, boolean died) {
        if (level <= 0) {
            return true;
        }
        return creative || itemBroke || died;
    }
}
