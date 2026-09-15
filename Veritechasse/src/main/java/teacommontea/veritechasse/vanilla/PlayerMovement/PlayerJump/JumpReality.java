package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerJump;

import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ.SprintJump;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class JumpReality {

    public static final double TOLERANCE = 1.0E-9D;

    private JumpReality() {
    }

    public static double maximumVertical(
            double currentDeltaY,
            double jumpStrengthAttribute,
            String blockHere,
            String blockBelow,
            ActiveEffects effects,
            Protocol protocol) {
        float power = JumpPower.of(
            jumpStrengthAttribute, blockHere, blockBelow, effects, protocol);
        return JumpPower.appliedVertical(currentDeltaY, power, protocol);
    }

    public static boolean permitsVertical(
            double observedDeltaY,
            double currentDeltaY,
            double jumpStrengthAttribute,
            String blockHere,
            String blockBelow,
            ActiveEffects effects,
            Protocol protocol) {
        double bound = maximumVertical(
            currentDeltaY, jumpStrengthAttribute, blockHere, blockBelow, effects, protocol);
        return observedDeltaY <= bound + TOLERANCE;
    }

    public static double horizontalImpulse(boolean sprinting) {
        return sprinting ? SprintJump.HORIZONTAL_IMPULSE : 0.0D;
    }

    public static double maximumHorizontalAfterJump(double currentHorizontal, boolean sprinting) {
        return currentHorizontal + horizontalImpulse(sprinting);
    }

    public static double maximumTakeoffHorizontal(
            double carriedHorizontal,
            double groundAcceleration,
            float airDrag,
            boolean sprinting) {
        return carriedHorizontal * (double) airDrag
            + groundAcceleration
            + horizontalImpulse(sprinting);
    }

    public static boolean jumpedWithoutGround(
            boolean claimsJumped,
            double attemptedY,
            double resolvedY,
            boolean inWater,
            double fluidHeight,
            double jumpThreshold) {
        if (!claimsJumped) {
            return false;
        }
        if (JumpGate.groundIsDerived(attemptedY, resolvedY)) {
            return false;
        }
        return !(JumpGate.inWaterWithDepth(inWater, fluidHeight) && fluidHeight <= jumpThreshold);
    }

    public static boolean jumpedDuringCooldown(boolean claimsJumped, int noJumpDelay) {
        return claimsJumped && JumpGate.cooldownActive(noJumpDelay);
    }

    public static boolean jumpsPerSecondIsPossible(int jumpsObserved, int ticksElapsed) {
        if (jumpsObserved <= 1) {
            return true;
        }
        int minimumTicks = (jumpsObserved - 1) * JumpGate.COOLDOWN_TICKS;
        return ticksElapsed >= minimumTicks;
    }

    public static boolean powerExceedsAttribute(
            float observedPower,
            double jumpStrengthAttribute,
            String blockHere,
            String blockBelow,
            ActiveEffects effects,
            Protocol protocol) {
        float bound = JumpPower.of(
            jumpStrengthAttribute, blockHere, blockBelow, effects, protocol);
        return observedPower > bound + TOLERANCE;
    }

    public static Reality verticalFrom(
            double currentDeltaY,
            double jumpStrengthAttribute,
            String blockHere,
            String blockBelow,
            ActiveEffects effects,
            Protocol protocol) {
        return Reality.of(maximumVertical(
            currentDeltaY, jumpStrengthAttribute, blockHere, blockBelow, effects, protocol));
    }

    public static Reality horizontalFrom(double currentHorizontal, boolean sprinting) {
        return Reality.of(maximumHorizontalAfterJump(currentHorizontal, sprinting));
    }
}
