package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.FluidCurrent;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.Gravity;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.GroundSpeed;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

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

    public static double waterHorizontalAfterTick(
            double carriedHorizontal,
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        float efficiency = WaterMotion.efficiencyFrom(boots, era);
        float slowDown = WaterMotion.slowDown(sprinting, efficiency, onGround, effects);
        double attribute = GroundSpeed.attributeSpeed(effects, sprinting);
        float acceleration = WaterMotion.acceleration(attribute, efficiency, onGround);
        return WaterMotion.horizontalAfter(
            carriedHorizontal + (double) acceleration, slowDown);
    }

    public static double maximumWaterSpeed(
            double carriedHorizontal,
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        double decayed = waterHorizontalAfterTick(
            carriedHorizontal, effects, boots, sprinting, onGround, era);
        double steady = maximumWaterSpeed(effects, boots, sprinting, onGround, era);
        return decayed > steady ? decayed : steady;
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

    public static double maximumWaterSpeedWithCurrent(
            double carriedHorizontal,
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        return maximumWaterSpeed(
                carriedHorizontal, effects, boots, sprinting, onGround, era)
            + FluidCurrent.waterImpulse(1.0D);
    }

    public static double maximumLavaSpeed(boolean shallow, boolean ultraWarmDimension) {
        return LavaMotion.terminalSpeed(LavaMotion.SPEED, shallow)
            + FluidCurrent.lavaImpulse(1.0D, ultraWarmDimension);
    }

    public static double lavaHorizontalAfterTick(
            double carriedHorizontal, boolean shallow) {
        return LavaMotion.horizontalAfter(
            carriedHorizontal + (double) LavaMotion.SPEED, shallow);
    }

    public static double maximumLavaSpeed(
            double carriedHorizontal, boolean shallow, boolean ultraWarmDimension) {
        double decayed = lavaHorizontalAfterTick(carriedHorizontal, shallow);
        double steady = maximumLavaSpeed(shallow, ultraWarmDimension);
        return decayed > steady ? decayed : steady;
    }

    public static boolean permitsLavaSpeed(
            double observedHorizontal,
            boolean shallow,
            boolean ultraWarmDimension) {
        return observedHorizontal <= maximumLavaSpeed(shallow, ultraWarmDimension) + TOLERANCE;
    }

    public static final double MAXIMUM_CARRIED_ENTRY = 1.0D;

    public static final int SETTLE_TICK_LIMIT = 200;

    public static int ticksToSettleInWater(
            double carriedHorizontal,
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        double steady = maximumWaterSpeedWithCurrent(
            effects, boots, sprinting, onGround, era);
        double carried = carriedHorizontal;
        int ticks = 0;
        while (carried > steady && ticks < SETTLE_TICK_LIMIT) {
            carried = waterHorizontalAfterTick(
                carried, effects, boots, sprinting, onGround, era);
            ticks = ticks + 1;
        }
        return ticks;
    }

    public static int ticksToSettleInWater(
            ActiveEffects effects,
            ItemStack boots,
            boolean sprinting,
            boolean onGround,
            Era era) {
        return ticksToSettleInWater(
            MAXIMUM_CARRIED_ENTRY, effects, boots, sprinting, onGround, era);
    }

    public static int ticksToSettleInLava(
            double carriedHorizontal, boolean shallow, boolean ultraWarmDimension) {
        double steady = maximumLavaSpeed(shallow, ultraWarmDimension);
        double carried = carriedHorizontal;
        int ticks = 0;
        while (carried > steady && ticks < SETTLE_TICK_LIMIT) {
            carried = lavaHorizontalAfterTick(carried, shallow);
            ticks = ticks + 1;
        }
        return ticks;
    }

    public static int ticksToSettleInLava(boolean shallow, boolean ultraWarmDimension) {
        return ticksToSettleInLava(
            MAXIMUM_CARRIED_ENTRY, shallow, ultraWarmDimension);
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
