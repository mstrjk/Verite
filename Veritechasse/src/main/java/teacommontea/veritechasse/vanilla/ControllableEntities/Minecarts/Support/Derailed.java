package teacommontea.veritechasse.vanilla.ControllableEntities.Minecarts.Support;

import teacommontea.veritechasse.vanilla.ControllableEntities.Support.BubbleColumn;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.ExplosionKnockback;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.GroundFriction;
import teacommontea.veritechasse.vanilla.Reality;

public final class Derailed {

    public static final double GROUND_SCALE = 0.5D;
    public static final float AIR_DRAG = 0.95F;

    public static final boolean READS_BLOCK_FRICTION = true;

    private Derailed() {
    }

    public static double clampToMaxSpeed(double component, double maxSpeed) {
        if (component < -maxSpeed) {
            return -maxSpeed;
        }
        return component > maxSpeed ? maxSpeed : component;
    }

    public static double horizontalAfterTick(double horizontal, boolean onGround) {
        if (onGround) {
            return horizontal * GROUND_SCALE;
        }
        return horizontal * AIR_DRAG;
    }

    public static double maxHorizontal(MinecartBehaviour behaviour, boolean inWater, int maxMinecartSpeedRule) {
        return MinecartSpeed.maxSpeed(behaviour, inWater, maxMinecartSpeedRule);
    }

    public static float groundFriction(String blockNameBelow) {
        return GroundFriction.of(blockNameBelow);
    }

    public static boolean onIce(String blockNameBelow) {
        return GroundFriction.isIce(blockNameBelow);
    }

    public static double verticalFromBubbleColumn(double verticalMovement, boolean dragDown, boolean above) {
        if (above) {
            return BubbleColumn.verticalAfterAbove(verticalMovement, dragDown);
        }
        return BubbleColumn.verticalAfterInside(verticalMovement, dragDown);
    }

    public static double maxVerticalFromBubbleColumn(boolean above) {
        return BubbleColumn.maximumUpward(above);
    }

    public static double knockbackFromExplosion(double distanceToCentre, float radius, float exposure) {
        return ExplosionKnockback.vehicleKnockbackPower(distanceToCentre, radius, exposure);
    }

    public static Reality bubbleColumnFrom(boolean above) {
        return BubbleColumn.upwardFrom(above);
    }

    public static Reality explosionKnockbackFrom(double distanceToCentre, float radius, float exposure) {
        return ExplosionKnockback.knockbackFrom(distanceToCentre, radius, exposure);
    }

    public static Reality speedFrom(MinecartBehaviour behaviour, boolean inWater, int maxMinecartSpeedRule) {
        return Reality.of(maxHorizontal(behaviour, inWater, maxMinecartSpeedRule));
    }
}
