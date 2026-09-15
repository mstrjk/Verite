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
