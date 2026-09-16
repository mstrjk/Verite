package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.BlockMovementFactors;
import teacommontea.veritechasse.Vanilla.Enchantments.SoulSpeed;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.ArmourModifiers;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.SoulSpeedBlocks;
import teacommontea.veritechasse.Vanilla.Reality;

public final class MovementEfficiency {

    public static final double DEFAULT = 0.0D;
    public static final double MINIMUM = 0.0D;
    public static final double MAXIMUM = 1.0D;

    public static final double SOUL_SPEED_EFFICIENCY = 1.0D;

    public static final double POWDER_SNOW_SLOWDOWN_PER_PERCENT = -0.05D;

    private MovementEfficiency() {
    }

    public static double fromSoulSpeed(ItemStack boots, boolean onSoulBlock) {
        if (boots == null || !onSoulBlock) {
            return DEFAULT;
        }
        return SoulSpeed.levelOn(boots) > 0 ? SOUL_SPEED_EFFICIENCY : DEFAULT;
    }

    public static double soulSpeedBonus(ItemStack boots, boolean onSoulBlock, Era era) {
        if (boots == null || !onSoulBlock) {
            return 0.0D;
        }
        return ArmourModifiers.soulSpeedBonus(boots, era);
    }

    public static boolean isSoulBlock(String blockName) {
        return SoulSpeedBlocks.contains(blockName);
    }

    public static float effectiveSpeedFactor(String blockHere, String blockBelow, double movementEfficiency) {
        float raw = BlockMovementFactors.resolveSpeedFactor(blockHere, blockBelow, false);
        double clamped = clamp(movementEfficiency);
        return (float) (raw + (1.0D - raw) * clamped);
    }

    public static float effectiveSpeedFactor(
            String blockHere,
            String blockBelow,
            ItemStack boots) {
        double efficiency = fromSoulSpeed(boots, isSoulBlock(blockBelow));
        return effectiveSpeedFactor(blockHere, blockBelow, efficiency);
    }

    public static double powderSnowSlowdown(float percentFrozen) {
        return POWDER_SNOW_SLOWDOWN_PER_PERCENT * percentFrozen;
    }

    public static double clamp(double movementEfficiency) {
        if (movementEfficiency < MINIMUM) {
            return MINIMUM;
        }
        return movementEfficiency > MAXIMUM ? MAXIMUM : movementEfficiency;
    }

    public static Reality speedFactorFrom(String blockHere, String blockBelow, ItemStack boots) {
        return Reality.of(effectiveSpeedFactor(blockHere, blockBelow, boots));
    }
}
