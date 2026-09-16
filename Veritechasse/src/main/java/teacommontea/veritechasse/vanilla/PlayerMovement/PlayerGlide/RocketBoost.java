package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerGlide;

import teacommontea.veritechasse.vanilla.Reality;

public final class RocketBoost {

    public static final double TARGET_POWER = 1.5D;
    public static final double POWER_ADD = 0.1D;
    public static final double APPROACH_RATE = 0.5D;

    public static final boolean REQUIRES_FALL_FLYING = true;

    private RocketBoost() {
    }

    public static double componentAfter(double lookComponent, double currentComponent) {
        return currentComponent
            + lookComponent * POWER_ADD
            + (lookComponent * TARGET_POWER - currentComponent) * APPROACH_RATE;
    }

    public static double xAfter(double lookX, double currentX) {
        return componentAfter(lookX, currentX);
    }

    public static double yAfter(double lookY, double currentY) {
        return componentAfter(lookY, currentY);
    }

    public static double zAfter(double lookZ, double currentZ) {
        return componentAfter(lookZ, currentZ);
    }

    public static final int TICKS_PER_FLIGHT_DURATION = 10;
    public static final int LIFETIME_RANDOM_A = 6;
    public static final int LIFETIME_RANDOM_B = 7;

    public static final int MINIMUM_FLIGHT_DURATION = 1;
    public static final int MAXIMUM_FLIGHT_DURATION = 3;

    public static int minimumLifetime(int flightDuration) {
        return TICKS_PER_FLIGHT_DURATION * flightDuration;
    }

    public static int maximumLifetime(int flightDuration) {
        return TICKS_PER_FLIGHT_DURATION * flightDuration
            + (LIFETIME_RANDOM_A - 1) + (LIFETIME_RANDOM_B - 1);
    }

    public static boolean lifetimeIsPossible(int observedTicks, int flightDuration) {
        return observedTicks >= minimumLifetime(flightDuration)
            && observedTicks <= maximumLifetime(flightDuration);
    }

    public static boolean stillBoosting(int life, int lifetime) {
        return life <= lifetime;
    }

    public static double maximumSpeedAfter(double currentSpeed, int concurrentRockets) {
        if (concurrentRockets <= 0) {
            return currentSpeed;
        }
        double speed = currentSpeed;
        for (int rocket = 0; rocket < concurrentRockets; rocket++) {
            speed = maximumSpeedAfter(speed);
        }
        return speed;
    }

    public static boolean applies(boolean fallFlying) {
        return fallFlying;
    }

    public static double convergedSpeedWithoutDrag() {
        return (TARGET_POWER * APPROACH_RATE + POWER_ADD) / APPROACH_RATE;
    }

    public static double convergedSpeed(double horizontalDrag) {
        double gain = (TARGET_POWER * APPROACH_RATE + POWER_ADD) * horizontalDrag;
        double decay = 1.0D - (1.0D - APPROACH_RATE) * horizontalDrag;
        if (decay <= 0.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return gain / decay;
    }

    public static double convergedSpeed() {
        return convergedSpeed(GlideMotion.HORIZONTAL_DRAG);
    }

    public static double maximumComponentAfter(double currentComponent) {
        return componentAfter(1.0D, currentComponent);
    }

    public static double maximumSpeedAfter(double currentSpeed) {
        return componentAfter(1.0D, currentSpeed);
    }

    public static Reality speedFrom(double currentSpeed) {
        return Reality.of(maximumSpeedAfter(currentSpeed));
    }
}
