package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents;

import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.ImpulseExemption;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class MovedWrongly {

    public static final String KEY = "moved_wrongly";

    public static final double THRESHOLD = 0.0625D;

    public static final double VERTICAL_DISCARD_LOWER = -0.5D;
    public static final double VERTICAL_DISCARD_UPPER = 0.5D;

    private MovedWrongly() {
    }

    public static boolean verticalResidualIsDiscarded(double yDist) {
        return yDist > VERTICAL_DISCARD_LOWER || yDist < VERTICAL_DISCARD_UPPER;
    }

    public static double effectiveVerticalResidual(double yDist) {
        return verticalResidualIsDiscarded(yDist) ? 0.0D : yDist;
    }

    public static double residualDistanceSquared(double xDist, double yDist, double zDist) {
        double effectiveY = effectiveVerticalResidual(yDist);
        return xDist * xDist + effectiveY * effectiveY + zDist * zDist;
    }

    public static boolean exceedsThreshold(double residualDistanceSquared) {
        return residualDistanceSquared > THRESHOLD;
    }

    public static boolean exempt(
            boolean changingDimension,
            boolean sleeping,
            boolean creative,
            boolean spectator,
            int impulseGraceRemaining,
            Protocol protocol) {
        return changingDimension
            || sleeping
            || creative
            || spectator
            || ImpulseExemption.movementGateForgiven(impulseGraceRemaining, protocol);
    }

    public static boolean fails(
            double residualDistanceSquared,
            boolean changingDimension,
            boolean sleeping,
            boolean creative,
            boolean spectator,
            int impulseGraceRemaining,
            Protocol protocol) {
        if (exempt(changingDimension, sleeping, creative, spectator,
                impulseGraceRemaining, protocol)) {
            return false;
        }
        return exceedsThreshold(residualDistanceSquared);
    }

    public static boolean fails(
            double xDist,
            double yDist,
            double zDist,
            boolean changingDimension,
            boolean sleeping,
            boolean creative,
            boolean spectator,
            int impulseGraceRemaining,
            Protocol protocol) {
        return fails(
            residualDistanceSquared(xDist, yDist, zDist),
            changingDimension, sleeping, creative, spectator,
            impulseGraceRemaining, protocol);
    }

    public static Reality permittedResidualSquared() {
        return Reality.of(THRESHOLD);
    }
}
