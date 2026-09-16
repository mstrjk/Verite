package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerTeleport;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class TeleportRange {

    public static final double UNBOUNDED = Double.POSITIVE_INFINITY;

    public static final double OVERWORLD_COORDINATE_SCALE = 1.0D;
    public static final double NETHER_COORDINATE_SCALE = 8.0D;
    public static final double END_COORDINATE_SCALE = 1.0D;

    public static final int NETHER_PORTAL_SEARCH_RADIUS = 16;
    public static final int OVERWORLD_PORTAL_SEARCH_RADIUS = 128;

    public static final int LEGACY_CHORUS_Y_RANGE = 16;
    public static final int LEGACY_CHORUS_Y_OFFSET = 8;

    public static final int CONTINUOUS_CHORUS_MAJOR = 1;
    public static final int CONTINUOUS_CHORUS_MINOR = 21;
    public static final int CONTINUOUS_CHORUS_PATCH = 2;

    public static final double CHORUS_FRUIT_DIAMETER = 16.0D;

    public static double coordinateScaleOf(String dimensionName) {
        if (dimensionName == null) {
            return OVERWORLD_COORDINATE_SCALE;
        }
        String key = dimensionName.toLowerCase(java.util.Locale.ROOT);
        if (key.contains("nether")) {
            return NETHER_COORDINATE_SCALE;
        }
        if (key.contains("end")) {
            return END_COORDINATE_SCALE;
        }
        return OVERWORLD_COORDINATE_SCALE;
    }

    public static double teleportationScale(String fromDimension, String toDimension) {
        double to = coordinateScaleOf(toDimension);
        if (to <= 0.0D) {
            return 1.0D;
        }
        return coordinateScaleOf(fromDimension) / to;
    }

    public static double scaledCoordinate(
            double coordinate,
            String fromDimension,
            String toDimension) {
        return coordinate * teleportationScale(fromDimension, toDimension);
    }

    public static int portalSearchRadius(String toDimension) {
        String key = toDimension == null
            ? ""
            : toDimension.toLowerCase(java.util.Locale.ROOT);
        return key.contains("nether")
            ? NETHER_PORTAL_SEARCH_RADIUS
            : OVERWORLD_PORTAL_SEARCH_RADIUS;
    }

    public static boolean portalDestinationIsPossible(
            double observedX,
            double observedZ,
            double sourceX,
            double sourceZ,
            String fromDimension,
            String toDimension) {
        double expectedX = scaledCoordinate(sourceX, fromDimension, toDimension);
        double expectedZ = scaledCoordinate(sourceZ, fromDimension, toDimension);
        double radius = (double) portalSearchRadius(toDimension);
        return Math.abs(observedX - expectedX) <= radius
            && Math.abs(observedZ - expectedZ) <= radius;
    }

    public static boolean chorusIsContinuous(Protocol protocol) {
        return protocol.atLeast(
            CONTINUOUS_CHORUS_MAJOR, CONTINUOUS_CHORUS_MINOR, CONTINUOUS_CHORUS_PATCH);
    }

    public static double maximumChorusVerticalRise(Protocol protocol) {
        if (chorusIsContinuous(protocol)) {
            return CHORUS_FRUIT_DIAMETER * 0.5D;
        }
        return (double) (LEGACY_CHORUS_Y_RANGE - 1 - LEGACY_CHORUS_Y_OFFSET);
    }

    public static boolean chorusVerticalIsPossible(double observedRise, Protocol protocol) {
        return observedRise <= maximumChorusVerticalRise(protocol);
    }
    public static final double CHORUS_FRUIT_HALF_SPREAD = CHORUS_FRUIT_DIAMETER / 2.0D;
    public static final int CHORUS_FRUIT_ATTEMPTS = 16;

    public static final int EXIT_BED_MAX_AXIS_OFFSET = 2;
    public static final int EXIT_BED_MAX_VERTICAL_OFFSET = 1;

    public static final double EXIT_BED_MAX_OFFSET = Math.sqrt(
        (double) (EXIT_BED_MAX_AXIS_OFFSET * EXIT_BED_MAX_AXIS_OFFSET)
        + (double) (EXIT_BED_MAX_AXIS_OFFSET * EXIT_BED_MAX_AXIS_OFFSET)
        + (double) (EXIT_BED_MAX_VERTICAL_OFFSET * EXIT_BED_MAX_VERTICAL_OFFSET));

    private TeleportRange() {
    }

    public static double maximumFor(String cause) {
        String key = TeleportCauses.normalise(cause);
        if (TeleportCauses.CHORUS_FRUIT.equals(key) || TeleportCauses.CONSUMABLE_EFFECT.equals(key)) {
            return chorusFruitMaximumDistance();
        }
        if (TeleportCauses.EXIT_BED.equals(key)) {
            return EXIT_BED_MAX_OFFSET;
        }
        return UNBOUNDED;
    }

    public static double dismountMaximum(double vehicleWidth, double vehicleHeight) {
        double reach = vehicleWidth < 0.0D ? 0.0D : vehicleWidth;
        double lift = vehicleHeight < 0.0D ? 0.0D : vehicleHeight;
        return Math.sqrt(reach * reach + lift * lift + reach * reach);
    }

    public static double chorusFruitMaximumDistance() {
        double horizontal = CHORUS_FRUIT_HALF_SPREAD * Math.sqrt(2.0D);
        double vertical = CHORUS_FRUIT_HALF_SPREAD;
        return Math.sqrt(horizontal * horizontal + vertical * vertical);
    }

    public static boolean withinChorusSpread(double deltaX, double deltaY, double deltaZ) {
        return withinChorusSpread(deltaX, deltaY, deltaZ, Double.NEGATIVE_INFINITY);
    }

    public static boolean withinChorusSpread(
            double deltaX,
            double deltaY,
            double deltaZ,
            double minimumWorldY) {
        if (Math.abs(deltaX) > CHORUS_FRUIT_HALF_SPREAD
                || Math.abs(deltaZ) > CHORUS_FRUIT_HALF_SPREAD) {
            return false;
        }
        if (deltaY > CHORUS_FRUIT_HALF_SPREAD) {
            return false;
        }
        return deltaY >= minimumWorldY;
    }

    public static boolean descentIsUnbounded() {
        return true;
    }

    public static boolean isBounded(String cause) {
        return maximumFor(cause) != UNBOUNDED;
    }

    public static boolean permits(String cause, double distance) {
        double maximum = maximumFor(cause);
        if (maximum == UNBOUNDED) {
            return true;
        }
        return distance <= maximum;
    }

    public static double distance(
            double fromX,
            double fromY,
            double fromZ,
            double toX,
            double toY,
            double toZ) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double dz = toZ - fromZ;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static boolean crossesDimension(String fromWorld, String toWorld) {
        if (fromWorld == null || toWorld == null) {
            return false;
        }
        return !fromWorld.equals(toWorld);
    }

    public static Reality maximumFrom(String cause) {
        return Reality.of(maximumFor(cause));
    }
}
