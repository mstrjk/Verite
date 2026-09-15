package teacommontea.veritechasse.vanilla.ControllableEntities.Minecarts.Support;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Reality;

public final class Rails {

    public static final String RAIL = "rail";
    public static final String POWERED_RAIL = "powered_rail";
    public static final String DETECTOR_RAIL = "detector_rail";
    public static final String ACTIVATOR_RAIL = "activator_rail";

    public static final Set<String> ALL = Set.of(
        RAIL,
        POWERED_RAIL,
        DETECTOR_RAIL,
        ACTIVATOR_RAIL);

    public static final double POWERED_PUSH_PER_TICK = 0.06D;
    public static final double POWERED_PUSH_MINIMUM_SPEED = 0.01D;

    public static final double SLOPE_SLIDE_SPEED = 0.0078125D;
    public static final double SLOPE_SLIDE_WATER_MULTIPLIER = 0.2D;

    public static final double HALT_TRACK_MULTIPLIER = 0.5D;
    public static final double HALT_TRACK_DEAD_STOP_SPEED = 0.03D;

    private Rails() {
    }

    public static boolean isRail(String blockName) {
        if (blockName == null) {
            return false;
        }
        return ALL.contains(blockName.toLowerCase(Locale.ROOT));
    }

    public static boolean isPoweredRail(String blockName) {
        if (blockName == null) {
            return false;
        }
        return POWERED_RAIL.equals(blockName.toLowerCase(Locale.ROOT));
    }

    public static boolean accelerates(String blockName, boolean powered, double horizontalSpeed) {
        if (!isPoweredRail(blockName) || !powered) {
            return false;
        }
        return horizontalSpeed > POWERED_PUSH_MINIMUM_SPEED;
    }

    public static boolean brakes(String blockName, boolean powered) {
        return isPoweredRail(blockName) && !powered;
    }

    public static double slopeSlideSpeed(boolean inWater) {
        return inWater ? SLOPE_SLIDE_SPEED * SLOPE_SLIDE_WATER_MULTIPLIER : SLOPE_SLIDE_SPEED;
    }

    public static double speedAfterPoweredRail(double horizontalSpeed) {
        if (horizontalSpeed <= POWERED_PUSH_MINIMUM_SPEED) {
            return horizontalSpeed;
        }
        return horizontalSpeed + POWERED_PUSH_PER_TICK;
    }

    public static double speedAfterBraking(double horizontalSpeed) {
        if (horizontalSpeed < HALT_TRACK_DEAD_STOP_SPEED) {
            return 0.0D;
        }
        return horizontalSpeed * HALT_TRACK_MULTIPLIER;
    }

    public static boolean requiresRail(double observedSpeed) {
        return observedSpeed > 0.0D;
    }

    public static Reality pushFrom(double horizontalSpeed) {
        return Reality.of(speedAfterPoweredRail(horizontalSpeed));
    }
}
