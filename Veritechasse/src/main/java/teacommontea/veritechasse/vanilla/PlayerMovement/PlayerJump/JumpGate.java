package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerJump;

import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerLiquid.FluidState;
import teacommontea.veritechasse.vanilla.PlayerMovement.Support.GroundState;

public final class JumpGate {

    public static final int COOLDOWN_TICKS = 10;

    public static final float LIQUID_JUMP_IMPULSE_SOURCE = 0.04F;
    public static final double LIQUID_JUMP_IMPULSE = LIQUID_JUMP_IMPULSE_SOURCE;
    public static final float SINK_IMPULSE_SOURCE = -0.04F;
    public static final double SINK_IMPULSE = SINK_IMPULSE_SOURCE;

    public static final boolean COOLDOWN_RESETS_WHEN_NOT_ATTEMPTING_FLUID_JUMP = true;

    private JumpGate() {
    }

    public static boolean cooldownActive(int noJumpDelay) {
        return noJumpDelay > 0;
    }

    public static int cooldownAfterJump() {
        return COOLDOWN_TICKS;
    }

    public static int decrementCooldown(int noJumpDelay) {
        return noJumpDelay > 0 ? noJumpDelay - 1 : 0;
    }

    public static boolean cooldownIsReset(boolean jumpHeld, boolean affectedByFluids) {
        return !(jumpHeld && affectedByFluids);
    }

    public static int cooldownAfterTick(
            int noJumpDelay,
            boolean jumpHeld,
            boolean affectedByFluids,
            boolean groundJumpFired) {
        int decremented = decrementCooldown(noJumpDelay);
        if (cooldownIsReset(jumpHeld, affectedByFluids)) {
            return 0;
        }
        return groundJumpFired ? COOLDOWN_TICKS : decremented;
    }

    public static int cooldownAfterTick(int noJumpDelay, boolean jumpHeld) {
        return cooldownAfterTick(noJumpDelay, jumpHeld, true, false);
    }

    public static boolean liquidJumpHasCooldown() {
        return false;
    }

    public static boolean inWaterWithDepth(boolean inWater, double fluidHeight) {
        return inWater && fluidHeight > 0.0D;
    }

    public static boolean groundJumpAllowed(
            boolean onGround,
            boolean inWater,
            double fluidHeight,
            double jumpThreshold,
            int noJumpDelay) {
        if (cooldownActive(noJumpDelay)) {
            return false;
        }
        if (onGround) {
            return true;
        }
        return inWaterWithDepth(inWater, fluidHeight) && fluidHeight <= jumpThreshold;
    }

    public static boolean swimsUpInstead(
            boolean inWater,
            double fluidHeight,
            double jumpThreshold,
            boolean onGround) {
        if (!inWaterWithDepth(inWater, fluidHeight)) {
            return false;
        }
        if (onGround && fluidHeight <= jumpThreshold) {
            return false;
        }
        return true;
    }

    public static boolean swimsUpInLava(boolean inLava, boolean onGround, boolean shallowLava) {
        if (!inLava) {
            return false;
        }
        return !(onGround && shallowLava);
    }

    public static boolean jumpIsPossible(
            boolean jumpHeld,
            boolean affectedByFluids,
            boolean onGround,
            boolean inWater,
            boolean inLava,
            double fluidHeight,
            double jumpThreshold,
            boolean shallowLava,
            int noJumpDelay) {
        if (!jumpHeld || !affectedByFluids) {
            return false;
        }
        if (swimsUpInstead(inWater, fluidHeight, jumpThreshold, onGround)) {
            return false;
        }
        if (swimsUpInLava(inLava, onGround, shallowLava)) {
            return false;
        }
        return groundJumpAllowed(onGround, inWater, fluidHeight, jumpThreshold, noJumpDelay);
    }

    public static boolean groundIsDerived(double attemptedY, double resolvedY) {
        return GroundState.onGround(attemptedY, resolvedY);
    }

    public static double jumpThresholdFor(double eyeHeight) {
        return FluidState.jumpThreshold(eyeHeight);
    }
}
