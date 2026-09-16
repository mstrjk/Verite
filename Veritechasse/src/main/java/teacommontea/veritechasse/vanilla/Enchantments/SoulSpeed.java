package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableFootArmor;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.SoulSpeedBlocks;
import teacommontea.veritechasse.Vanilla.Era;

public final class SoulSpeed {

    public static final String KEY = "soul_speed";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 1;

    public static final String SUPPORTED_ITEMS = EnchantableFootArmor.KEY;
    public static final String REQUIRED_BLOCK_TAG = SoulSpeedBlocks.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(10.0F, 10.0F),
        new LinearValue(25.0F, 10.0F),
        8);

    public static final float MOVEMENT_EFFICIENCY = 1.0F;
    public static final float DURABILITY_LOSS_CHANCE = 0.04F;
    public static final int DURABILITY_COST = 1;

    private static final LevelValue MOVEMENT_SPEED = new LinearValue(0.0405F, 0.0105F);

    private SoulSpeed() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static float movementSpeedBonus(int level, Era era) {
        if (level <= 0) {
            return 0.0F;
        }
        return MOVEMENT_SPEED.calculate(level);
    }

    public static boolean applies(int level, boolean onSoulBlock, boolean onGround, boolean flying, boolean riding) {
        if (level <= 0 || flying || riding) {
            return false;
        }
        return onSoulBlock || !onGround;
    }

    public static double expectedDurabilityPerTick(int level, boolean onSoulBlock, boolean onGround) {
        if (level <= 0 || !onSoulBlock || !onGround) {
            return 0.0D;
        }
        return (double) DURABILITY_LOSS_CHANCE * (double) DURABILITY_COST;
    }
}
