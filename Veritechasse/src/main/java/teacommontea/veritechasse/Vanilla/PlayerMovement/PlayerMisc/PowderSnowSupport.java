package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerMisc;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.StuckInBlock;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Boots.LeatherBoots;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.Gravity;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.VerticalDrag;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class PowderSnowSupport {

    public static final String KEY = "powder_snow";

    public static final double FALL_DISTANCE_PUNCH_THROUGH = 2.5D;

    public static final double FALLING_COLLISION_HEIGHT = 0.9F;

    public static final double CLIMB_VELOCITY = 0.2D;

    public static final double IS_ABOVE_EPSILON = 1.0E-5F;

    public static final boolean RESETS_FALL_DISTANCE = true;

    public static final boolean CLEARS_DELTA_ON_STUCK = true;

    public static final int FALL_THROUGH_TICK_LIMIT = 200;

    public static final double FULL_BLOCK = 1.0D;

    private PowderSnowSupport() {
    }

    public static boolean isPowderSnow(String blockName) {
        return KEY.equals(blockName);
    }

    public static boolean walksOnPowderSnow(String entityName, ItemStack feet) {
        return LeatherBoots.entityWalksOnPowderSnow(entityName, feet);
    }

    public static boolean walksOnPowderSnow(ItemStack feet) {
        return LeatherBoots.walksOnPowderSnow(feet);
    }

    public static boolean isAbove(double entityMinY, int blockY) {
        return entityMinY > (double) blockY + 1.0D - IS_ABOVE_EPSILON;
    }

    public static boolean fallDistancePunchesThrough(double fallDistance) {
        return fallDistance > FALL_DISTANCE_PUNCH_THROUGH;
    }

    public static boolean collisionIsSolid(
            boolean walksOnSnow,
            double fallDistance,
            double entityMinY,
            int blockY,
            boolean shiftKeyDown) {
        if (fallDistancePunchesThrough(fallDistance)) {
            return false;
        }
        return walksOnSnow && isAbove(entityMinY, blockY) && !shiftKeyDown;
    }

    public static boolean collisionIsSolid(
            ItemStack feet,
            String entityName,
            double fallDistance,
            double entityMinY,
            int blockY,
            boolean shiftKeyDown) {
        return collisionIsSolid(
            walksOnPowderSnow(entityName, feet),
            fallDistance,
            entityMinY,
            blockY,
            shiftKeyDown);
    }

    public static boolean supportsStanding(
            boolean walksOnSnow,
            double fallDistance,
            double entityMinY,
            int blockY,
            boolean shiftKeyDown) {
        return collisionIsSolid(
            walksOnSnow, fallDistance, entityMinY, blockY, shiftKeyDown);
    }

    public static boolean supportsStanding(
            ItemStack feet,
            String entityName,
            double fallDistance,
            double entityMinY,
            int blockY,
            boolean shiftKeyDown) {
        return collisionIsSolid(
            feet, entityName, fallDistance, entityMinY, blockY, shiftKeyDown);
    }

    public static boolean sinks(
            boolean walksOnSnow,
            double fallDistance,
            double entityMinY,
            int blockY,
            boolean shiftKeyDown) {
        return !collisionIsSolid(
            walksOnSnow, fallDistance, entityMinY, blockY, shiftKeyDown);
    }

    public static boolean sinks(
            ItemStack feet,
            String entityName,
            double fallDistance,
            double entityMinY,
            int blockY,
            boolean shiftKeyDown) {
        return !collisionIsSolid(
            feet, entityName, fallDistance, entityMinY, blockY, shiftKeyDown);
    }

    public static boolean climbApplies(
            boolean horizontalCollision,
            boolean jumping,
            boolean wasInPowderSnow,
            boolean walksOnSnow) {
        if (!horizontalCollision && !jumping) {
            return false;
        }
        return wasInPowderSnow && walksOnSnow;
    }

    public static boolean climbApplies(
            boolean horizontalCollision,
            boolean jumping,
            boolean wasInPowderSnow,
            ItemStack feet,
            String entityName) {
        return climbApplies(
            horizontalCollision,
            jumping,
            wasInPowderSnow,
            walksOnPowderSnow(entityName, feet));
    }

    public static double climbVelocity() {
        return CLIMB_VELOCITY;
    }

    public static double horizontalMultiplier() {
        return StuckInBlock.POWDER_SNOW_HORIZONTAL;
    }

    public static double verticalMultiplier() {
        return StuckInBlock.POWDER_SNOW_VERTICAL;
    }

    public static double enteringDelta(double carriedDeltaY) {
        return carriedDeltaY * StuckInBlock.POWDER_SNOW_VERTICAL;
    }

    public static double nextCarriedDeltaY(
            double displacedY, ActiveEffects effects, Protocol protocol) {
        double gravity = Gravity.effective(displacedY, effects, protocol);
        return (displacedY - gravity) * (double) VerticalDrag.BASE_VERTICAL_AIR_DRAG;
    }

    public static int ticksToFallThrough(
            double blockHeight, ActiveEffects effects, Protocol protocol) {
        double travelled = 0.0D;
        double carried = 0.0D;
        int ticks = 0;
        while (travelled < blockHeight && ticks < FALL_THROUGH_TICK_LIMIT) {
            ticks = ticks + 1;
            double displaced = enteringDelta(carried);
            travelled = travelled - displaced;
            carried = nextCarriedDeltaY(displaced, effects, protocol);
        }
        return ticks;
    }

    public static int ticksToFallThrough(ActiveEffects effects, Protocol protocol) {
        return ticksToFallThrough(FULL_BLOCK, effects, protocol);
    }

    public static int ticksBeforeDescentIsVisible(
            double epsilon, ActiveEffects effects, Protocol protocol) {
        double travelled = 0.0D;
        double carried = 0.0D;
        int ticks = 0;
        while (travelled < epsilon && ticks < FALL_THROUGH_TICK_LIMIT) {
            ticks = ticks + 1;
            double displaced = enteringDelta(carried);
            travelled = travelled - displaced;
            carried = nextCarriedDeltaY(displaced, effects, protocol);
        }
        return ticks;
    }

    public static Reality sinkFrom(
            boolean walksOnSnow,
            double fallDistance,
            double entityMinY,
            int blockY,
            boolean shiftKeyDown) {
        boolean sinking = sinks(
            walksOnSnow, fallDistance, entityMinY, blockY, shiftKeyDown);
        return Reality.of(sinking ? 1.0D : 0.0D);
    }
}
