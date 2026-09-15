package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableArmor;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableChestArmor;
import teacommontea.veritechasse.vanilla.Era;

public final class Thorns {

    public static final String KEY = "thorns";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 1;

    public static final String SUPPORTED_ITEMS = EnchantableArmor.KEY;
    public static final String PRIMARY_ITEMS = EnchantableChestArmor.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(10.0F, 20.0F),
        new LinearValue(60.0F, 20.0F),
        8);

    public static final float MIN_DAMAGE = 1.0F;
    public static final float MAX_DAMAGE = 5.0F;
    public static final int DURABILITY_COST = 2;

    private static final LevelValue TRIGGER_CHANCE = new LinearValue(0.15F, 0.15F);

    private Thorns() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float triggerChance(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        float chance = TRIGGER_CHANCE.calculate(level);
        return chance > 1.0F ? 1.0F : chance;
    }

    public static float maxReflectedDamage(int level, Era era) {
        return level <= 0 ? 0.0F : MAX_DAMAGE;
    }

    public static int durabilityCost(int level, Era era) {
        return level <= 0 ? 0 : DURABILITY_COST;
    }
}
