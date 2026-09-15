package teacommontea.veritechasse.vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.EnchantableHeadArmor;
import teacommontea.veritechasse.vanilla.Era;

public final class AquaAffinity {

    public static final String KEY = "aqua_affinity";
    public static final int VANILLA_MAX_LEVEL = 1;
    public static final int WEIGHT = 2;

    public static final String SUPPORTED_ITEMS = EnchantableHeadArmor.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(1.0F, 0.0F),
        new LinearValue(41.0F, 0.0F),
        4);

    public static final float UNDERWATER_MINING_PENALTY = 0.2F;
    public static final float ATTRIBUTE_MAX = 20.0F;

    private static final LevelValue SUBMERGED_MINING_SPEED = new LinearValue(4.0F, 4.0F);

    private AquaAffinity() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float submergedMiningSpeed(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return SUBMERGED_MINING_SPEED.calculate(level);
    }

    public static float submergedMiningMultiplier(int level, Era era) {
        if (level <= 0) {
            return UNDERWATER_MINING_PENALTY;
        }

        if (era == Era.ENCHANTS_AS_CLASSES) {
            return 1.0F;
        }

        float multiplied = UNDERWATER_MINING_PENALTY * (1.0F + submergedMiningSpeed(level, era));
        return multiplied > ATTRIBUTE_MAX ? ATTRIBUTE_MAX : multiplied;
    }
}
