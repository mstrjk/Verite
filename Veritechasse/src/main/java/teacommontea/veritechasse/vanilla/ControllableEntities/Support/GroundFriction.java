package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import java.util.Locale;
import java.util.Map;

import teacommontea.veritechasse.vanilla.Reality;

public final class GroundFriction {

    public static final float DEFAULT = 0.6F;

    public static final float ICE = 0.98F;
    public static final float PACKED_ICE = 0.98F;
    public static final float FROSTED_ICE = 0.98F;
    public static final float BLUE_ICE = 0.989F;
    public static final float SLIME_BLOCK = 0.8F;

    public static final float HIGHEST = BLUE_ICE;

    private static final Map<String, Float> BY_BLOCK = Map.of(
        "ice", Float.valueOf(ICE),
        "packed_ice", Float.valueOf(PACKED_ICE),
        "frosted_ice", Float.valueOf(FROSTED_ICE),
        "blue_ice", Float.valueOf(BLUE_ICE),
        "slime_block", Float.valueOf(SLIME_BLOCK));

    private GroundFriction() {
    }

    public static float of(String blockName) {
        if (blockName == null) {
            return DEFAULT;
        }
        Float found = BY_BLOCK.get(blockName.toLowerCase(Locale.ROOT));
        return found == null ? DEFAULT : found.floatValue();
    }

    public static boolean isIce(String blockName) {
        if (blockName == null) {
            return false;
        }
        String name = blockName.toLowerCase(Locale.ROOT);
        return name.equals("ice")
            || name.equals("packed_ice")
            || name.equals("frosted_ice")
            || name.equals("blue_ice");
    }

    public static boolean contributesToAverage(String blockName) {
        if (blockName == null) {
            return false;
        }
        return !blockName.toLowerCase(Locale.ROOT).equals("lily_pad");
    }

    public static float average(float totalFriction, int blockCount) {
        if (blockCount <= 0) {
            return 0.0F;
        }
        return totalFriction / blockCount;
    }

    public static double terminalSpeed(double acceleration, float friction) {
        if (friction >= 1.0F) {
            return Double.POSITIVE_INFINITY;
        }
        return acceleration / (1.0D - friction);
    }

    public static double terminalSpeedOn(String blockName, double acceleration) {
        return terminalSpeed(acceleration, of(blockName));
    }

    public static Reality terminalSpeedFrom(String blockName, double acceleration) {
        return Reality.of(terminalSpeedOn(blockName, acceleration));
    }

    public static Reality highestTerminalSpeed(double acceleration) {
        return Reality.of(terminalSpeed(acceleration, HIGHEST));
    }
}
