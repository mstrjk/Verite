package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerMisc;

import teacommontea.veritechasse.vanilla.Reality;

public final class CreativeFlight {

    public static final float DEFAULT_FLYING_SPEED = 0.05F;
    public static final float DEFAULT_WALKING_SPEED = 0.1F;

    public static final float SPRINT_MULTIPLIER = 2.0F;

    public static final double VERTICAL_DAMPING = 0.6D;

    public static final float VERTICAL_INPUT_MULTIPLIER = 3.0F;

    public static final boolean SUPPRESSES_CROUCH = true;
    public static final boolean SUPPRESSES_SWIMMING = true;
    public static final boolean SUPPRESSES_FALL_DAMAGE = true;
    public static final boolean SUPPRESSES_LEDGE_PROTECTION = true;

    private CreativeFlight() {
    }

    public static float horizontalSpeed(float flyingSpeed, boolean sprinting) {
        return sprinting ? flyingSpeed * SPRINT_MULTIPLIER : flyingSpeed;
    }

    public static float horizontalSpeed(boolean sprinting) {
        return horizontalSpeed(DEFAULT_FLYING_SPEED, sprinting);
    }

    public static double verticalAfterTravel(double deltaYBeforeTravel) {
        return deltaYBeforeTravel * VERTICAL_DAMPING;
    }

    public static double verticalTerminal(double impulsePerTick) {
        double drag = VERTICAL_DAMPING;
        if (drag >= 1.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return impulsePerTick * drag / (1.0D - drag);
    }

    public static double verticalImpulse(float flyingSpeed, float verticalInput) {
        return (double) (verticalInput * flyingSpeed * VERTICAL_INPUT_MULTIPLIER);
    }

    public static double maximumAscent(float flyingSpeed) {
        return verticalTerminal(verticalImpulse(flyingSpeed, 1.0F));
    }

    public static double maximumAscent() {
        return maximumAscent(DEFAULT_FLYING_SPEED);
    }

    public static boolean speedExceedsVanilla(float flyingSpeed) {
        return flyingSpeed > DEFAULT_FLYING_SPEED;
    }

    public static boolean fallDamageApplies(boolean mayFly) {
        return !mayFly;
    }

    public static Reality horizontalFrom(float flyingSpeed, boolean sprinting) {
        return Reality.of(horizontalSpeed(flyingSpeed, sprinting));
    }
}
