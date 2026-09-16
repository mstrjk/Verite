package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerMisc;

import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.GroundSpeed;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

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

    public static final int TRAVEL_FLYING_MAJOR = 1;
    public static final int TRAVEL_FLYING_MINOR = 21;
    public static final int TRAVEL_FLYING_PATCH = 6;

    public static final boolean PLAYER_USES_TRAVEL_FLYING = false;

    public static final float FLYING_WATER_ACCELERATION = 0.02F;
    public static final float FLYING_LAVA_ACCELERATION = 0.02F;
    public static final float FLYING_WATER_DRAG = 0.8F;
    public static final float FLYING_LAVA_DRAG = 0.5F;

    public static boolean travelFlyingExists(Protocol protocol) {
        return protocol.atLeast(
            TRAVEL_FLYING_MAJOR, TRAVEL_FLYING_MINOR, TRAVEL_FLYING_PATCH);
    }

    public static boolean playerUsesTravelFlying() {
        return PLAYER_USES_TRAVEL_FLYING;
    }

    public static float flyingDrag(boolean inWater, boolean inLava) {
        if (inWater) {
            return FLYING_WATER_DRAG;
        }
        return inLava ? FLYING_LAVA_DRAG : GroundSpeed.AIR_DRAG;
    }

    public static float flyingAcceleration(
            float flyingSpeed,
            boolean sprinting,
            boolean inWater,
            boolean inLava) {
        if (inWater) {
            return FLYING_WATER_ACCELERATION;
        }
        if (inLava) {
            return FLYING_LAVA_ACCELERATION;
        }
        return horizontalSpeed(flyingSpeed, sprinting);
    }

    public static double mobTravelFlyingTerminal(
            float flyingSpeed,
            boolean sprinting,
            boolean inWater,
            boolean inLava) {
        if (!inWater && !inLava) {
            return horizontalTerminal(flyingSpeed, sprinting);
        }
        float drag = flyingDrag(inWater, inLava);
        if (drag >= 1.0F) {
            return Double.POSITIVE_INFINITY;
        }
        double acceleration =
            (double) flyingAcceleration(flyingSpeed, sprinting, inWater, inLava);
        return acceleration * (double) drag / (1.0D - (double) drag);
    }

    public static double horizontalTerminal(
            float flyingSpeed,
            boolean sprinting,
            boolean inWater,
            boolean inLava) {
        return horizontalTerminal(flyingSpeed, sprinting);
    }

    public static double mobTravelFlyingAfterTick(
            double currentHorizontal,
            float flyingSpeed,
            boolean sprinting,
            boolean inWater,
            boolean inLava) {
        if (!inWater && !inLava) {
            return horizontalAfterTick(currentHorizontal, flyingSpeed, sprinting);
        }
        double accelerated = currentHorizontal
            + (double) flyingAcceleration(flyingSpeed, sprinting, inWater, inLava);
        return accelerated * (double) flyingDrag(inWater, inLava);
    }

    public static double horizontalAfterTick(
            double currentHorizontal,
            float flyingSpeed,
            boolean sprinting,
            boolean inWater,
            boolean inLava) {
        return horizontalAfterTick(currentHorizontal, flyingSpeed, sprinting);
    }

    public static double horizontalTerminal(float flyingSpeed, boolean sprinting) {
        float drag = GroundSpeed.AIR_DRAG;
        if (drag >= 1.0F) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) horizontalSpeed(flyingSpeed, sprinting)
            / (1.0D - (double) drag);
    }

    public static double horizontalTerminal(boolean sprinting) {
        return horizontalTerminal(DEFAULT_FLYING_SPEED, sprinting);
    }

    public static double horizontalAfterTick(double currentHorizontal, float flyingSpeed, boolean sprinting) {
        return currentHorizontal * (double) GroundSpeed.AIR_DRAG
            + (double) horizontalSpeed(flyingSpeed, sprinting);
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
