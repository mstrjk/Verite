package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerMovement.Support.GroundState;
import teacommontea.veritechasse.Vanilla.PlayerMovement.Support.MoveInput;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Reality;

public final class HorizontalReality {

    private HorizontalReality() {
    }

    public static double steadyStateSpeed(
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere) {
        return GroundSpeed.terminalSpeed(effects, sprinting, blockBelow, blockHere);
    }

    public static double nextTickSpeed(
            double currentHorizontal,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            double inputLength) {
        return nextTickSpeed(
            currentHorizontal, effects, sprinting, blockBelow, blockHere, inputLength, true, false);
    }

    public static double nextTickSpeedEitherGround(
            double currentHorizontal,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            double inputLength,
            boolean creativeFlying) {
        double grounded = nextTickSpeed(
            currentHorizontal, effects, sprinting, blockBelow, blockHere,
            inputLength, true, creativeFlying);
        double airborne = nextTickSpeed(
            currentHorizontal, effects, sprinting, blockBelow, blockHere,
            inputLength, false, creativeFlying);
        return grounded > airborne ? grounded : airborne;
    }

    public static double nextTickSpeed(
            double currentHorizontal,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            double inputLength,
            boolean onGround,
            boolean creativeFlying) {
        float decay = GroundSpeed.horizontalDecay(
            blockBelow, onGround, GroundSpeed.DEFAULT_FRICTION_MODIFIER, GroundSpeed.DEFAULT_AIR_DRAG_MODIFIER);
        double decayed = currentHorizontal * decay;

        double attribute = GroundSpeed.attributeSpeed(effects, sprinting);
        float friction = GroundSpeed.blockFriction(blockBelow, onGround, GroundSpeed.DEFAULT_FRICTION_MODIFIER);
        double accel = GroundSpeed.frictionInfluencedSpeed(
            attribute, friction, onGround, sprinting, creativeFlying) * inputLength;

        double accelerated = decayed + accel;
        if (!onGround) {
            return ExternalXZ.afterStuckOnly(accelerated, blockHere, effects);
        }
        return ExternalXZ.afterState(accelerated, blockHere, blockBelow, effects);
    }

    public static double nextTickSpeedFromCollision(
            double currentHorizontal,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            double inputLength,
            double attemptedY,
            double resolvedY,
            boolean creativeFlying) {
        boolean onGround = GroundState.onGround(attemptedY, resolvedY);
        return nextTickSpeed(
            currentHorizontal, effects, sprinting, blockBelow, blockHere, inputLength, onGround, creativeFlying);
    }

    public static boolean groundClaimIsFalsified(boolean clientClaim, double attemptedY, double resolvedY) {
        return GroundState.claimIsFalsified(clientClaim, attemptedY, resolvedY);
    }

    public static double maximumNextTickSpeed(
            double currentHorizontal,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            ItemStack trident,
            float pitchDegrees,
            boolean inWaterOrRain,
            boolean passenger,
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean inWater,
            boolean inLava,
            boolean ultraWarmDimension,
            boolean pistonAdjacent,
            boolean insideSolidBlock,
            boolean entityCrowded,
            Era era) {
        double walking = nextTickSpeedEitherGround(
            currentHorizontal, effects, sprinting, blockBelow, blockHere,
            MoveInput.LEGAL_LENGTH_CARDINAL, false);

        double external = ExternalXZ.additiveImpulse(
            explosionSource, explosionDistance, explosionExposure,
            inWater, inLava, ultraWarmDimension, pistonAdjacent, insideSolidBlock, entityCrowded);

        double riptide = 0.0D;
        if (RiptideImpulseXZ.canLaunch(trident, inWaterOrRain, passenger)) {
            riptide = RiptideImpulseXZ.horizontalImpulse(trident, pitchDegrees, era);
        }

        return walking + external + riptide;
    }

    public static boolean permits(
            double observedHorizontal,
            double currentHorizontal,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            ItemStack trident,
            float pitchDegrees,
            boolean inWaterOrRain,
            boolean passenger,
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean inWater,
            boolean inLava,
            boolean ultraWarmDimension,
            boolean pistonAdjacent,
            boolean insideSolidBlock,
            boolean entityCrowded,
            Era era) {
        double bound = maximumNextTickSpeed(
            currentHorizontal, effects, sprinting, blockBelow, blockHere,
            trident, pitchDegrees, inWaterOrRain, passenger,
            explosionSource, explosionDistance, explosionExposure,
            inWater, inLava, ultraWarmDimension, pistonAdjacent, insideSolidBlock, entityCrowded, era);
        return observedHorizontal <= bound;
    }

    public static Reality steadyStateFrom(
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere) {
        return Reality.of(steadyStateSpeed(effects, sprinting, blockBelow, blockHere));
    }

    public static Reality nextTickFrom(
            double currentHorizontal,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            double inputLength) {
        return Reality.of(nextTickSpeed(
            currentHorizontal, effects, sprinting, blockBelow, blockHere, inputLength));
    }
}
