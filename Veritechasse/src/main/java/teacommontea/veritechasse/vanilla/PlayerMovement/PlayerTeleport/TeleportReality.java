package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerTeleport;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class TeleportReality {

    public static final boolean UNANNOUNCED_MOVEMENT_IS_REAL = false;

    private TeleportReality() {
    }

    public static boolean invalidatesPreviousPosition(String cause, Protocol protocol) {
        return TeleportCauses.exists(cause, protocol) && !TeleportCauses.isUnknown(cause);
    }

    public static boolean permitsDistance(String cause, double distance, Protocol protocol) {
        if (!TeleportCauses.exists(cause, protocol)) {
            return false;
        }
        return TeleportRange.permits(cause, distance);
    }

    public static boolean permits(
            String cause,
            double fromX,
            double fromY,
            double fromZ,
            double toX,
            double toY,
            double toZ,
            String fromWorld,
            String toWorld,
            Protocol protocol) {
        if (!TeleportCauses.exists(cause, protocol)) {
            return false;
        }
        if (TeleportRange.crossesDimension(fromWorld, toWorld)) {
            return TeleportEffects.crossesDimensionLegally(cause, protocol);
        }
        double distance = TeleportRange.distance(fromX, fromY, fromZ, toX, toY, toZ);
        return TeleportRange.permits(cause, distance);
    }

    public static boolean chorusFruitPlacementIsPossible(
            double fromX,
            double fromY,
            double fromZ,
            double toX,
            double toY,
            double toZ) {
        return TeleportRange.withinChorusSpread(toX - fromX, toY - fromY, toZ - fromZ);
    }

    public static double horizontalBaselineAfter(String cause, double currentHorizontal) {
        return TeleportEffects.horizontalAfter(cause, currentHorizontal);
    }

    public static double verticalBaselineAfter(String cause, double currentVertical) {
        return TeleportEffects.verticalAfter(cause, currentVertical);
    }

    public static double fallDistanceAfter(String cause, double currentFallDistance) {
        return TeleportEffects.resetsFallDistance(cause) ? 0.0D : currentFallDistance;
    }

    public static boolean movementIsUnexplained(
            String cause,
            double distance,
            Protocol protocol) {
        if (cause == null || TeleportCauses.isUnknown(cause)) {
            return true;
        }
        return !permitsDistance(cause, distance, protocol);
    }

    public static boolean repeatedTooQuickly(
            String cause,
            int ticksSincePrevious,
            boolean isPlayer) {
        return TeleportEffects.withinCooldown(cause, ticksSincePrevious, isPlayer);
    }

    public static Reality distanceFrom(String cause) {
        return TeleportRange.maximumFrom(cause);
    }
}
