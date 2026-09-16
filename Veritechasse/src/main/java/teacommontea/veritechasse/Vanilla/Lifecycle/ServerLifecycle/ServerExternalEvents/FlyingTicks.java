package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class FlyingTicks {

    public static final String KEY = "flying_ticks";

    public static final int GRAVITY_SCALED_MAJOR = 1;
    public static final int GRAVITY_SCALED_MINOR = 20;
    public static final int GRAVITY_SCALED_PATCH = 5;

    public static final int BASE_LIMIT = 80;

    public static final double BASE_GRAVITY = 0.08D;

    public static final float NEGLIGIBLE_GRAVITY_SOURCE = 1.0E-5F;

    public static final double NEGLIGIBLE_GRAVITY = NEGLIGIBLE_GRAVITY_SOURCE;

    private FlyingTicks() {
    }

    public static boolean limitScalesWithGravity(Protocol protocol) {
        return protocol.atLeast(
            GRAVITY_SCALED_MAJOR, GRAVITY_SCALED_MINOR, GRAVITY_SCALED_PATCH);
    }

    public static boolean gravityIsNegligible(double gravity) {
        return gravity < NEGLIGIBLE_GRAVITY;
    }

    public static double gravityModifier(double gravity) {
        return BASE_GRAVITY / gravity;
    }

    public static int maximumFlyingTicks(double gravity, Protocol protocol) {
        if (!limitScalesWithGravity(protocol)) {
            return BASE_LIMIT;
        }
        if (gravityIsNegligible(gravity)) {
            return Integer.MAX_VALUE;
        }
        double modifier = Math.max(gravityModifier(gravity), 1.0D);
        return (int) Math.ceil((double) BASE_LIMIT * modifier);
    }

    public static boolean limitIsUnbounded(double gravity, Protocol protocol) {
        return limitScalesWithGravity(protocol) && gravityIsNegligible(gravity);
    }

    public static boolean exceedsLimit(int aboveGroundTickCount, double gravity, Protocol protocol) {
        if (limitIsUnbounded(gravity, protocol)) {
            return false;
        }
        return aboveGroundTickCount + 1 > maximumFlyingTicks(gravity, protocol);
    }

    public static boolean counterRuns(
            boolean clientIsFloating,
            boolean sleeping,
            boolean passenger,
            boolean deadOrDying) {
        return clientIsFloating && !sleeping && !passenger && !deadOrDying;
    }

    public static int afterTick(
            int aboveGroundTickCount,
            boolean clientIsFloating,
            boolean sleeping,
            boolean passenger,
            boolean deadOrDying) {
        if (!counterRuns(clientIsFloating, sleeping, passenger, deadOrDying)) {
            return 0;
        }
        return aboveGroundTickCount + 1;
    }

    public static boolean vehicleCounterRuns(
            boolean clientVehicleIsFloating,
            boolean playerControlsVehicle) {
        return clientVehicleIsFloating && playerControlsVehicle;
    }

    public static int vehicleAfterTick(
            int aboveGroundVehicleTickCount,
            boolean clientVehicleIsFloating,
            boolean playerControlsVehicle) {
        if (!vehicleCounterRuns(clientVehicleIsFloating, playerControlsVehicle)) {
            return 0;
        }
        return aboveGroundVehicleTickCount + 1;
    }

    public static int afterReset() {
        return 0;
    }

    public static Reality limitFrom(double gravity, Protocol protocol) {
        return Reality.of(maximumFlyingTicks(gravity, protocol));
    }
}
