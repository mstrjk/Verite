package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerWorldChange;

import teacommontea.veritechasse.Vanilla.Reality;

public final class DimensionChange {

    public static final String KEY = "dimension_change";

    public static final int PLAYER_PORTAL_DELAY = 10;

    public static final int ENTITY_PORTAL_DELAY = 300;

    public static final int END_GATEWAY_DELAY = 40;

    public static final double OVERWORLD_SCALE = 1.0D;

    public static final double NETHER_SCALE = 8.0D;

    public static final float MINIMUM_SCALE_SOURCE = 1.0E-5F;

    public static final double MINIMUM_SCALE = MINIMUM_SCALE_SOURCE;

    public static final double MAXIMUM_SCALE = 3.0E7D;

    private DimensionChange() {
    }

    public static double teleportationScale(double fromScale, double toScale) {
        if (toScale <= 0.0D) {
            return 1.0D;
        }
        return fromScale / toScale;
    }

    public static double scaledCoordinate(double coordinate, double fromScale, double toScale) {
        return coordinate * teleportationScale(fromScale, toScale);
    }

    public static int portalDelay(boolean isPlayer) {
        return isPlayer ? PLAYER_PORTAL_DELAY : ENTITY_PORTAL_DELAY;
    }

    public static int portalDelayForVehicle(boolean carriesPlayer) {
        return carriesPlayer ? PLAYER_PORTAL_DELAY : ENTITY_PORTAL_DELAY;
    }

    public static boolean scaleIsLegal(double scale) {
        return scale >= MINIMUM_SCALE && scale <= MAXIMUM_SCALE;
    }

    public static boolean baselineIsInvalidated() {
        return true;
    }

    public static boolean movedWronglyIsExemptWhileChanging(boolean changingDimension) {
        return changingDimension;
    }

    public static boolean momentumSurvivesDimensionChange() {
        return true;
    }

    public static Reality playerPortalCooldown() {
        return Reality.of(PLAYER_PORTAL_DELAY);
    }

    public static Reality endGatewayCooldown() {
        return Reality.of(END_GATEWAY_DELAY);
    }
}
