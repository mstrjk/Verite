package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ;

import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.BlockEjection;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.BlockMovementFactors;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.CollisionPush;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.ExplosionKnockback;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.FluidCurrent;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.Knockback;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.PistonPush;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.StuckInBlock;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Reality;

public final class ExternalXZ {

    private ExternalXZ() {
    }

    public static double waterCurrent(double normalisedFlowComponent, boolean ridingBoat) {
        if (!FluidCurrent.pushedByWater(ridingBoat)) {
            return 0.0D;
        }
        return FluidCurrent.waterImpulse(normalisedFlowComponent);
    }

    public static double lavaCurrent(double normalisedFlowComponent, boolean ultraWarmDimension) {
        return FluidCurrent.lavaImpulse(normalisedFlowComponent, ultraWarmDimension);
    }

    public static double collisionPush(double axisDistance, double absMaxAxisDistance) {
        return CollisionPush.impulse(axisDistance, absMaxAxisDistance);
    }

    public static double explosionKnockback(String source, double distanceToCentre, float exposure) {
        return ExplosionKnockback.knockbackFor(source, distanceToCentre, exposure);
    }

    public static double pistonDisplacement() {
        return PistonPush.maximumDisplacementPerTick();
    }

    public static double blockEjection() {
        return BlockEjection.maximumHorizontalImpulse();
    }

    public static double afterMeleeKnockback(
            double currentHorizontal,
            double attackKnockbackAttribute,
            double knockbackResistance) {
        return Knockback.horizontalAfterMelee(currentHorizontal, attackKnockbackAttribute, knockbackResistance);
    }

    public static double afterKnockback(double currentHorizontal, double power, double knockbackResistance) {
        return Knockback.horizontalAfter(currentHorizontal, power, knockbackResistance);
    }

    public static double arrowKnockback(double knockbackValue, double knockbackResistance) {
        return Knockback.arrowKnockback(knockbackValue, knockbackResistance);
    }

    public static double fishingRodPull(double distanceToOwner) {
        return Knockback.fishingRodPullMagnitude(distanceToOwner);
    }

    public static double sprintJumpImpulse() {
        return SprintJump.HORIZONTAL_IMPULSE;
    }

    public static double afterSlimeStep(double horizontal, String blockSteppedOn, double deltaY, boolean sneaking) {
        return BlockMovementFactors.afterSlimeStep(horizontal, blockSteppedOn, deltaY, sneaking);
    }

    public static double additiveImpulse(
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean inWater,
            boolean inLava,
            boolean ultraWarmDimension,
            boolean pistonAdjacent,
            boolean insideSolidBlock,
            boolean entityCrowded,
            double attackKnockbackAttribute,
            double knockbackResistance,
            boolean sprintJumping) {
        double total = additiveImpulse(
            explosionSource, explosionDistance, explosionExposure,
            inWater, inLava, ultraWarmDimension, pistonAdjacent, insideSolidBlock, entityCrowded);
        if (attackKnockbackAttribute >= 0.0D) {
            total = total + Knockback.afterResistance(
                Knockback.meleeTotalPower(attackKnockbackAttribute), knockbackResistance);
        }
        if (sprintJumping) {
            total = total + SprintJump.HORIZONTAL_IMPULSE;
        }
        return total;
    }

    public static double additiveImpulse(
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean inWater,
            boolean inLava,
            boolean ultraWarmDimension,
            boolean pistonAdjacent,
            boolean insideSolidBlock,
            boolean entityCrowded) {
        double total = 0.0D;
        if (explosionSource != null) {
            total = total + explosionKnockback(explosionSource, explosionDistance, explosionExposure);
        }
        if (pistonAdjacent) {
            total = total + pistonDisplacement();
        }
        if (insideSolidBlock) {
            total = total + blockEjection();
        }
        if (inWater) {
            total = total + FluidCurrent.waterImpulse(1.0D);
        }
        if (inLava) {
            total = total + FluidCurrent.lavaImpulse(1.0D, ultraWarmDimension);
        }
        if (entityCrowded) {
            total = total + CollisionPush.maximumImpulse();
        }
        return total;
    }

    public static double stateMultiplier(String blockHere, String blockBelow, ActiveEffects effects) {
        return stateMultiplier(blockHere, blockBelow, effects,
            BlockMovementFactors.isWaterOrBubbleColumn(blockHere));
    }

    public static double stateMultiplier(
            String blockHere,
            String blockBelow,
            ActiveEffects effects,
            boolean inWaterOrBubble) {
        double stuck = StuckInBlock.horizontalMultiplier(blockHere, effects);
        double speedFactor = BlockMovementFactors.resolveSpeedFactor(
            blockHere, blockBelow, inWaterOrBubble);
        return stuck * speedFactor;
    }

    public static double afterState(double horizontal, String blockHere, String blockBelow, ActiveEffects effects) {
        return horizontal * stateMultiplier(blockHere, blockBelow, effects);
    }

    public static double afterStuckOnly(double horizontal, String blockHere, ActiveEffects effects) {
        return StuckInBlock.horizontalAfter(horizontal, blockHere, effects);
    }

    public static final double STUCK_ACTIVE_SQUARED_THRESHOLD = 1.0E-7D;

    public static final int PISTON_EXEMPT_MAJOR = 26;
    public static final int PISTON_EXEMPT_MINOR = 2;
    public static final int PISTON_EXEMPT_PATCH = 0;

    public static boolean pistonSkipsStuckMultiplier(Protocol protocol) {
        return protocol.atLeast(
            PISTON_EXEMPT_MAJOR, PISTON_EXEMPT_MINOR, PISTON_EXEMPT_PATCH);
    }

    public static boolean stuckMultiplierApplies(String blockHere, ActiveEffects effects) {
        double multiplier = StuckInBlock.horizontalMultiplier(blockHere, effects);
        return multiplier * multiplier > STUCK_ACTIVE_SQUARED_THRESHOLD
            && multiplier < 1.0D;
    }

    public static double carriedAfterStuck(
            double horizontal,
            String blockHere,
            ActiveEffects effects) {
        if (stuckMultiplierApplies(blockHere, effects)) {
            return 0.0D;
        }
        return horizontal;
    }

    public static boolean stuckZeroesCarriedVelocity() {
        return true;
    }

    public static boolean isSlowingBlock(String blockName) {
        return StuckInBlock.isStickyBlock(blockName) || BlockMovementFactors.slowsMovement(blockName);
    }

    public static Reality additiveFrom(
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean inWater,
            boolean inLava,
            boolean ultraWarmDimension,
            boolean pistonAdjacent,
            boolean insideSolidBlock,
            boolean entityCrowded) {
        return Reality.of(additiveImpulse(
            explosionSource, explosionDistance, explosionExposure,
            inWater, inLava, ultraWarmDimension, pistonAdjacent, insideSolidBlock, entityCrowded));
    }

    public static Reality multiplierFrom(String blockHere, String blockBelow, ActiveEffects effects) {
        return Reality.of(stateMultiplier(blockHere, blockBelow, effects));
    }
}
