package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientToServerForgiveness;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class FloatingCriterion {

    public static final String KEY = "floating_criterion";

    public static final double RISE_THRESHOLD = -0.03125D;

    public static final int ALLOW_FLIGHT_EXEMPT_MAJOR = 1;
    public static final int ALLOW_FLIGHT_EXEMPT_MINOR = 21;
    public static final int ALLOW_FLIGHT_EXEMPT_PATCH = 9;

    private FloatingCriterion() {
    }

    public static boolean serverPropertyExempts(Protocol protocol) {
        return protocol.atLeast(
            ALLOW_FLIGHT_EXEMPT_MAJOR, ALLOW_FLIGHT_EXEMPT_MINOR, ALLOW_FLIGHT_EXEMPT_PATCH);
    }

    public static boolean risesEnoughToCount(double yDist) {
        return yDist >= RISE_THRESHOLD;
    }

    public static boolean isFloating(
            double yDist,
            boolean standsOnSomething,
            boolean spectator,
            boolean allowFlightProperty,
            boolean mayFly,
            boolean levitating,
            boolean fallFlying,
            boolean autoSpinAttack,
            boolean noBlocksAround,
            Protocol protocol) {
        if (!risesEnoughToCount(yDist)) {
            return false;
        }
        if (standsOnSomething || spectator || mayFly
                || levitating || fallFlying || autoSpinAttack) {
            return false;
        }
        if (serverPropertyExempts(protocol) && allowFlightProperty) {
            return false;
        }
        return noBlocksAround;
    }

    public static boolean vehicleIsFloating(
            double yDist,
            boolean vehicleRestsOnSomething,
            boolean allowFlightProperty,
            boolean flyingVehicle,
            boolean noGravity,
            boolean noBlocksAround) {
        if (!risesEnoughToCount(yDist)) {
            return false;
        }
        if (vehicleRestsOnSomething || allowFlightProperty
                || flyingVehicle || noGravity) {
            return false;
        }
        return noBlocksAround;
    }

    public static boolean vehicleCriterionHasSevenExemptions() {
        return false;
    }

    public static int playerExemptionCount(Protocol protocol) {
        return serverPropertyExempts(protocol) ? 7 : 6;
    }
}
