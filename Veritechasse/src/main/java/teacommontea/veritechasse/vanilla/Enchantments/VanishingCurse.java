package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableVanishing;

public final class VanishingCurse {

    public static final String KEY = "vanishing_curse";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 1;

    public static final String SUPPORTED_ITEMS = EnchantableVanishing.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(25.0F, 0.0F),
        new LinearValue(50.0F, 0.0F),
        8);

    private VanishingCurse() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static boolean survivesDeath(int level) {
        return level <= 0;
    }

    public static boolean dropsOnDeath(int level) {
        return level <= 0;
    }
}
