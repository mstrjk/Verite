package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableMeleeWeapon;
import teacommontea.veritechasse.vanilla.Era;

public final class Looting {

    public static final String KEY = "looting";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableMeleeWeapon.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(15.0F, 9.0F),
        new LinearValue(65.0F, 9.0F),
        4);

    private static final LevelValue EQUIPMENT_DROPS = new LinearValue(0.01F, 0.01F);

    private Looting() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float equipmentDropChanceBonus(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return EQUIPMENT_DROPS.calculate(level);
    }

    public static int lootBonusMax(int level) {
        if (level <= 0) {
            return 0;
        }
        return level;
    }

    public static boolean requiresPlayerAttacker() {
        return true;
    }
}
