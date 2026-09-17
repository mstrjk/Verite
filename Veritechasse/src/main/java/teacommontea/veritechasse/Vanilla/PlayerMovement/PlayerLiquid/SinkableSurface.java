package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerMisc.PowderSnowSupport;

public final class SinkableSurface {

    public static final String WATER = "water";
    public static final String LAVA = "lava";
    public static final String BUBBLE_COLUMN = "bubble_column";
    public static final String POWDER_SNOW = "powder_snow";

    public static final double SUSPENSION_TOLERANCE = 1.0E-3D;

    private SinkableSurface() {
    }

    public static boolean isFluidName(String blockName) {
        return WATER.equals(blockName)
            || LAVA.equals(blockName)
            || BUBBLE_COLUMN.equals(blockName);
    }

    public static boolean isSinkableName(String blockName, ItemStack boots) {
        if (isFluidName(blockName)) {
            return true;
        }
        if (PowderSnowSupport.isPowderSnow(blockName)) {
            return !PowderSnowSupport.walksOnPowderSnow(boots);
        }
        return false;
    }

    public static String surfaceOf(String blockHere, String blockBelow, ItemStack boots) {
        String here = resolve(blockHere, boots);
        if (here != null) {
            return here;
        }
        return resolve(blockBelow, boots);
    }

    private static String resolve(String blockName, ItemStack boots) {
        if (PowderSnowSupport.isPowderSnow(blockName)) {
            return PowderSnowSupport.walksOnPowderSnow(boots) ? null : POWDER_SNOW;
        }
        if (LAVA.equals(blockName)) {
            return LAVA;
        }
        if (WATER.equals(blockName) || BUBBLE_COLUMN.equals(blockName)) {
            return WATER;
        }
        return null;
    }

    public static boolean over(String blockHere, String blockBelow, ItemStack boots) {
        return surfaceOf(blockHere, blockBelow, boots) != null;
    }

    public static boolean inside(String blockHere, ItemStack boots) {
        return isSinkableName(blockHere, boots);
    }

    public static boolean unsupported(
            String blockHere, String blockBelow, ItemStack boots, boolean supported) {
        return over(blockHere, blockBelow, boots) && !supported;
    }

    public static final double FLAT_BAND = 0.02D;

    public static boolean heldFlat(double deltaY) {
        return Math.abs(deltaY) < FLAT_BAND;
    }

    public static boolean descendedLessThanVanilla(
            double observedDeltaY, double expectedDeltaY) {
        return observedDeltaY - expectedDeltaY > SUSPENSION_TOLERANCE;
    }

    public static boolean suspensionIsImpossible(
            boolean overSinkable,
            boolean supported,
            boolean submerged,
            boolean swimming,
            double observedDeltaY) {
        if (!overSinkable || supported || submerged || swimming) {
            return false;
        }
        return heldFlat(observedDeltaY);
    }

    public static boolean groundClaimIsImpossible(
            boolean groundClaim,
            boolean overSinkable,
            boolean supported,
            boolean submerged,
            boolean swimming) {
        if (!groundClaim || !overSinkable) {
            return false;
        }
        return !supported && !submerged && !swimming;
    }

    public static boolean risingFromSurfaceIsImpossible(
            boolean wasOverSinkableUnsupported,
            boolean wasRising,
            boolean rising,
            boolean touchingFluid) {
        if (touchingFluid) {
            return false;
        }
        return rising && !wasRising && wasOverSinkableUnsupported;
    }
}
