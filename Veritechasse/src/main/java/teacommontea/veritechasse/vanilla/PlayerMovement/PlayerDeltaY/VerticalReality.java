package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.ControllableEntities.Support.BlockEjection;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.BubbleColumn;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.ExplosionKnockback;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.Knockback;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.PistonPush;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerMovement.Support.GroundState;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class VerticalReality {

    private VerticalReality() {
    }

    public static double nextTickDeltaY(
            double currentDeltaY,
            ActiveEffects effects,
            Protocol protocol) {
        return VerticalTick.next(currentDeltaY, effects, protocol);
    }

    public static double maximumRise(
            double currentDeltaY,
            ActiveEffects effects,
            boolean inBubbleColumn,
            boolean aboveBubbleColumn,
            boolean bubbleColumnDrags,
            double knockbackPower,
            double knockbackResistance,
            boolean onGround,
            Protocol protocol) {
        double natural = nextTickDeltaY(currentDeltaY, effects, protocol);

        double best = natural;
        if (inBubbleColumn) {
            double column = aboveBubbleColumn
                ? BubbleColumn.verticalAfterAbove(currentDeltaY, bubbleColumnDrags)
                : BubbleColumn.verticalAfterInside(currentDeltaY, bubbleColumnDrags);
            if (column > best) {
                best = column;
            }
        }
        if (knockbackPower > 0.0D) {
            double knocked = Knockback.verticalAfter(currentDeltaY, knockbackPower, onGround);
            if (knocked > best) {
                best = knocked;
            }
        }
        return best;
    }

    public static boolean permitsRise(
            double observedDeltaY,
            double currentDeltaY,
            ActiveEffects effects,
            boolean inBubbleColumn,
            boolean aboveBubbleColumn,
            boolean bubbleColumnDrags,
            double knockbackPower,
            double knockbackResistance,
            boolean onGround,
            Protocol protocol) {
        double bound = maximumRise(
            currentDeltaY, effects, inBubbleColumn, aboveBubbleColumn, bubbleColumnDrags,
            knockbackPower, knockbackResistance, onGround, protocol);
        return observedDeltaY <= bound + VerticalTick.CONVERGENCE_TOLERANCE;
    }

    public static double explosionImpulse(
            String explosionSource,
            double distanceToCentre,
            float exposure) {
        if (explosionSource == null) {
            return 0.0D;
        }
        return ExplosionKnockback.knockbackFor(explosionSource, distanceToCentre, exposure);
    }

    public static double externalUpwardImpulse(
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean pistonAdjacent,
            boolean insideSolidBlock,
            ItemStack trident,
            float yawDegrees,
            float pitchDegrees,
            boolean onGround,
            boolean inWaterOrRain,
            boolean passenger,
            Era era) {
        double total = explosionImpulse(explosionSource, explosionDistance, explosionExposure);
        if (pistonAdjacent) {
            total = total + PistonPush.maximumDisplacementPerTick();
        }
        if (insideSolidBlock) {
            total = total + BlockEjection.maximumHorizontalImpulse();
        }
        if (trident != null && inWaterOrRain && !passenger) {
            total = total
                + Math.abs(RiptideImpulseY.verticalImpulse(trident, yawDegrees, pitchDegrees, era))
                + RiptideImpulseY.groundLaunchDisplacement(onGround);
        }
        return total;
    }

    public static double maximumRiseWithExternal(
            double currentDeltaY,
            ActiveEffects effects,
            boolean inBubbleColumn,
            boolean aboveBubbleColumn,
            boolean bubbleColumnDrags,
            double knockbackPower,
            double knockbackResistance,
            boolean onGround,
            String blockLandedOn,
            boolean sneaking,
            boolean living,
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean pistonAdjacent,
            boolean insideSolidBlock,
            ItemStack trident,
            float yawDegrees,
            float pitchDegrees,
            boolean inWaterOrRain,
            boolean passenger,
            Era era,
            Protocol protocol) {
        double natural = maximumRise(
            currentDeltaY, effects, inBubbleColumn, aboveBubbleColumn, bubbleColumnDrags,
            knockbackPower, knockbackResistance, onGround, protocol);

        double bounce = Bounce.maximumRebound(currentDeltaY, blockLandedOn, living, protocol);
        if (!Bounce.suppressedBySneaking(sneaking) && bounce > natural) {
            natural = bounce;
        }

        return natural + externalUpwardImpulse(
            explosionSource, explosionDistance, explosionExposure,
            pistonAdjacent, insideSolidBlock,
            trident, yawDegrees, pitchDegrees, onGround, inWaterOrRain, passenger, era);
    }

    public static boolean hangsInAir(
            double observedDeltaY,
            double previousDeltaY,
            ActiveEffects effects,
            Protocol protocol) {
        return VerticalTick.risesWithoutCause(observedDeltaY, previousDeltaY, effects, protocol);
    }

    public static boolean groundClaimIsFalsified(
            boolean clientClaim,
            double attemptedY,
            double resolvedY) {
        return GroundState.claimIsFalsified(clientClaim, attemptedY, resolvedY);
    }

    public static double landingFallDistance(
            double currentFallDistance,
            double deltaY,
            boolean inWater,
            Protocol protocol) {
        return FallDistance.landingDistance(currentFallDistance, deltaY, inWater, protocol);
    }

    public static boolean landingContradictsDamage(
            String landedOn,
            double fallDistance,
            boolean damageObserved,
            boolean mayFly,
            ActiveEffects effects,
            Protocol protocol) {
        boolean expected = FallDamage.expectsDamageOn(
            landedOn, fallDistance, mayFly, effects, protocol);
        return expected != damageObserved;
    }

    public static Reality nextTickFrom(
            double currentDeltaY,
            ActiveEffects effects,
            Protocol protocol) {
        return Reality.of(nextTickDeltaY(currentDeltaY, effects, protocol));
    }

    public static Reality riseFrom(
            double currentDeltaY,
            ActiveEffects effects,
            boolean inBubbleColumn,
            boolean aboveBubbleColumn,
            boolean bubbleColumnDrags,
            double knockbackPower,
            double knockbackResistance,
            boolean onGround,
            Protocol protocol) {
        return Reality.of(maximumRise(
            currentDeltaY, effects, inBubbleColumn, aboveBubbleColumn, bubbleColumnDrags,
            knockbackPower, knockbackResistance, onGround, protocol));
    }
}
