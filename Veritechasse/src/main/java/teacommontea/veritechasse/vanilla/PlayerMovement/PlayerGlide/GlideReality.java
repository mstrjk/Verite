package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerGlide;

import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY.Gravity;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class GlideReality {

    public static final double TOLERANCE = 1.0E-9D;

    public static final double WALL_DAMAGE_SCALE = 10.0D;
    public static final double WALL_DAMAGE_THRESHOLD = 3.0D;

    private GlideReality() {
    }

    public static double maximumHorizontalAfterRocket(double currentHorizontal) {
        return GlideMotion.horizontalAfterDrag(
            RocketBoost.maximumSpeedAfter(currentHorizontal));
    }

    public static double maximumHorizontal(double currentHorizontal, boolean rocketActive) {
        if (rocketActive) {
            return maximumHorizontalAfterRocket(currentHorizontal);
        }
        return GlideMotion.horizontalAfterDrag(currentHorizontal);
    }

    public static boolean permitsHorizontal(
            double observedHorizontal,
            double currentHorizontal,
            boolean rocketActive) {
        double bound = maximumHorizontal(currentHorizontal, rocketActive);
        return observedHorizontal <= bound + TOLERANCE;
    }

    public static double maximumVertical(
            double currentDeltaY,
            double currentHorizontal,
            float pitchDegrees,
            double lookLength,
            ActiveEffects effects,
            boolean rocketActive,
            Protocol protocol) {
        double gravity = Gravity.effective(currentDeltaY, effects, protocol);
        double lift = GlideMotion.liftForce(pitchDegrees, lookLength, protocol);

        double raised = currentDeltaY + GlideMotion.gravityContribution(gravity, lift);
        raised = raised + GlideMotion.diveConversion(raised, lift);
        raised = raised + GlideMotion.pitchUpVertical(pitchDegrees, currentHorizontal);

        if (rocketActive) {
            raised = RocketBoost.yAfter(1.0D, raised);
        }
        return GlideMotion.verticalAfterDrag(raised);
    }

    public static boolean permitsVertical(
            double observedDeltaY,
            double currentDeltaY,
            double currentHorizontal,
            float pitchDegrees,
            double lookLength,
            ActiveEffects effects,
            boolean rocketActive,
            Protocol protocol) {
        double bound = maximumVertical(
            currentDeltaY, currentHorizontal, pitchDegrees, lookLength,
            effects, rocketActive, protocol);
        return observedDeltaY <= bound + TOLERANCE;
    }

    public static boolean glidingWithoutGlider(
            boolean claimsGliding,
            boolean onGround,
            boolean passenger,
            ActiveEffects effects,
            boolean hasWorkingGlider) {
        if (!claimsGliding) {
            return false;
        }
        return !GlideGate.canGlide(onGround, passenger, effects, hasWorkingGlider);
    }

    public static double wallImpactDamage(double speedBefore, double speedAfter) {
        double difference = speedBefore - speedAfter;
        float damage = (float) (difference * WALL_DAMAGE_SCALE - WALL_DAMAGE_THRESHOLD);
        return damage > 0.0F ? (double) damage : 0.0D;
    }

    public static double wallImpactDamage(
            double speedBefore,
            double speedAfter,
            boolean horizontalCollision) {
        if (!horizontalCollision) {
            return 0.0D;
        }
        return wallImpactDamage(speedBefore, speedAfter);
    }

    public static boolean expectsWallDamage(double speedBefore, double speedAfter) {
        return wallImpactDamage(speedBefore, speedAfter) > 0.0D;
    }

    public static boolean expectsWallDamage(
            double speedBefore,
            double speedAfter,
            boolean horizontalCollision) {
        return wallImpactDamage(speedBefore, speedAfter, horizontalCollision) > 0.0D;
    }

    public static final int FALL_DISTANCE_RULE_MAJOR = 1;
    public static final int FALL_DISTANCE_RULE_MINOR = 21;
    public static final int FALL_DISTANCE_RULE_PATCH = 2;

    public static boolean legacyFallDistanceRule(Protocol protocol) {
        return !protocol.atLeast(
            FALL_DISTANCE_RULE_MAJOR, FALL_DISTANCE_RULE_MINOR, FALL_DISTANCE_RULE_PATCH);
    }

    public static double fallDistanceWhileGliding(double deltaY, double currentFallDistance) {
        if (GlideMotion.fallDistanceHeld(deltaY)) {
            return GlideMotion.FALL_DISTANCE_WHILE_GLIDING;
        }
        return currentFallDistance;
    }

    public static double fallDistanceWhileGliding(
            double deltaY,
            double currentFallDistance,
            Protocol protocol) {
        if (!legacyFallDistanceRule(protocol)) {
            return currentFallDistance;
        }
        return fallDistanceWhileGliding(deltaY, currentFallDistance);
    }

    public static Reality horizontalFrom(double currentHorizontal, boolean rocketActive) {
        return Reality.of(maximumHorizontal(currentHorizontal, rocketActive));
    }

    public static Reality rocketFrom(double currentSpeed) {
        return RocketBoost.speedFrom(currentSpeed);
    }
}
