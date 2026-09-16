package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerLiquid;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.ControllableEntities.Support.FluidCurrent;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY.Gravity;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ.GroundSpeed;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class LiquidReality {

    public static final double TOLERANCE = 1.0E-9D;

    private LiquidReality() {
    }

    public static double maximumWaterSpeed(
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        float efficiency = WaterMotion.efficiencyFrom(boots, era);
        float slowDown = WaterMotion.slowDown(sprinting, efficiency, onGround, effects);
        double attribute = GroundSpeed.attributeSpeed(effects, sprinting);
        float acceleration = WaterMotion.acceleration(attribute, efficiency, onGround);
        return WaterMotion.terminalSpeed(acceleration, slowDown);
    }

    public static boolean permitsWaterSpeed(
            double observedHorizontal,
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        double bound = maximumWaterSpeed(effects, boots, sprinting, onGround, era);
        return observedHorizontal <= bound + TOLERANCE;
    }

    public static double maximumWaterSpeedWithCurrent(
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        return maximumWaterSpeed(effects, boots, sprinting, onGround, era)
            + FluidCurrent.waterImpulse(1.0D);
    }

    public static double maximumLavaSpeed(boolean shallow, boolean ultraWarmDimension) {
        return LavaMotion.terminalSpeed(LavaMotion.SPEED, shallow)
            + FluidCurrent.lavaImpulse(1.0D, ultraWarmDimension);
    }

    public static boolean permitsLavaSpeed(
            double observedHorizontal,
            boolean shallow,
            boolean ultraWarmDimension) {
        return observedHorizontal <= maximumLavaSpeed(shallow, ultraWarmDimension) + TOLERANCE;
    }

    public static double maximumAscent(boolean inWater, boolean jumping) {
        if (!inWater || !jumping) {
            return 0.0D;
        }
        return WaterMotion.SWIM_JUMP;
    }

    public static final double FLUID_FALLING_GRAVITY_DIVISOR = 16.0D;
    public static final double FLUID_FALLING_HOVER_TARGET = -0.003D;
    public static final double FLUID_FALLING_HOVER_EPSILON = 0.003D;
    public static final double FLUID_FALLING_HOVER_REFERENCE = 0.005D;

    public static double fluidFallingAdjusted(
            double componentY,
            double baseGravity,
            boolean falling,
            boolean sprinting) {
        if (baseGravity == 0.0D || sprinting) {
            return componentY;
        }
        double pulled = componentY - baseGravity / FLUID_FALLING_GRAVITY_DIVISOR;
        if (falling
                && Math.abs(componentY - FLUID_FALLING_HOVER_REFERENCE)
                    >= FLUID_FALLING_HOVER_EPSILON
                && Math.abs(pulled) < FLUID_FALLING_HOVER_EPSILON) {
            return FLUID_FALLING_HOVER_TARGET;
        }
        return pulled;
    }

    public static double nextWaterVertical(
            double deltaY,
            double baseGravity,
            boolean sprinting) {
        return nextWaterVertical(deltaY, baseGravity, sprinting, deltaY <= 0.0D);
    }

    public static double nextWaterVertical(
            double deltaY,
            double baseGravity,
            boolean sprinting,
            boolean falling) {
        double dragged = WaterMotion.verticalAfter(deltaY);
        return fluidFallingAdjusted(dragged, baseGravity, falling, sprinting);
    }

    public static double nextLavaVertical(
            double deltaY,
            ActiveEffects effects,
            boolean shallow,
            Protocol protocol) {
        return nextLavaVertical(deltaY, effects, shallow, false, deltaY <= 0.0D, protocol);
    }

    public static double nextLavaVertical(
            double deltaY,
            ActiveEffects effects,
            boolean shallow,
            boolean sprinting,
            boolean falling,
            Protocol protocol) {
        double gravity = Gravity.effective(deltaY, effects, protocol);
        double dragged = LavaMotion.verticalAfter(deltaY, shallow);
        if (shallow) {
            dragged = fluidFallingAdjusted(dragged, gravity, falling, sprinting);
        }
        return dragged + LavaMotion.gravityPull(gravity);
    }

    public static boolean swimsWithoutSprinting(
            boolean claimsSwimming,
            boolean sprinting,
            boolean underWater,
            boolean passenger) {
        if (!claimsSwimming) {
            return false;
        }
        return !FluidState.canSwim(sprinting, underWater, passenger);
    }

    public static boolean lavaIsAsFastAsWater(double observedHorizontal, boolean shallow) {
        return observedHorizontal > LavaMotion.terminalSpeed(LavaMotion.SPEED, shallow) + TOLERANCE;
    }

    public static Reality waterFrom(
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        return Reality.of(maximumWaterSpeed(effects, boots, sprinting, onGround, era));
    }

    public static Reality lavaFrom(boolean shallow, boolean ultraWarmDimension) {
        return Reality.of(maximumLavaSpeed(shallow, ultraWarmDimension));
    }
}
