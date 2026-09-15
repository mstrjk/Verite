package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerTeleport;

import teacommontea.veritechasse.vanilla.Reality;

public final class TeleportRange {

    public static final double UNBOUNDED = Double.POSITIVE_INFINITY;

    public static final double CHORUS_FRUIT_DIAMETER = 16.0D;
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
        return Math.abs(deltaX) <= CHORUS_FRUIT_HALF_SPREAD
            && Math.abs(deltaY) <= CHORUS_FRUIT_HALF_SPREAD
            && Math.abs(deltaZ) <= CHORUS_FRUIT_HALF_SPREAD;
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
