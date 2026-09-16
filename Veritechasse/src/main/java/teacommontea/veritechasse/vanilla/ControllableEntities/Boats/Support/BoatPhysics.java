package teacommontea.veritechasse.Vanilla.ControllableEntities.Boats.Support;

import teacommontea.veritechasse.Vanilla.Reality;

public final class BoatPhysics {

    public static final double DEFAULT_GRAVITY = 0.04D;
    public static final double BUOYANCY_GRAVITY_DIVISOR = 0.65D;
    public static final double BUOYANCY_DAMPING = 0.75D;

    public static final double UNDER_WATER_BUOYANCY = 0.01D;
    public static final double UNDER_FLOWING_WATER_VSPEED = -7.0E-4D;

    public static final double SURFACE_SNAP_OFFSET = 0.101D;

    public static final float LAND_FRICTION_PLAYER_DIVISOR = 2.0F;

    public static final double BUBBLE_UPWARD_WITH_PLAYER = 2.7D;
    public static final double BUBBLE_UPWARD_EMPTY = 0.6D;
    public static final double BUBBLE_DOWNWARD = -0.7D;
    public static final int BUBBLE_TIME_TICKS = 60;

    private BoatPhysics() {
    }

    public static double verticalSpeedFor(BoatStatus status, double gravity) {
        if (status == BoatStatus.UNDER_FLOWING_WATER) {
            return UNDER_FLOWING_WATER_VSPEED;
        }
        return -gravity;
    }

    public static double buoyancyFor(BoatStatus status, double waterLevel, double y, double boxHeight) {
        if (status == BoatStatus.UNDER_WATER) {
            return UNDER_WATER_BUOYANCY;
        }
        if (status == BoatStatus.IN_WATER && boxHeight > 0.0D) {
            return (waterLevel - y) / boxHeight;
        }
        return 0.0D;
    }

    public static double applyBuoyancy(double verticalMovement, double buoyancy) {
        if (buoyancy <= 0.0D) {
            return verticalMovement;
        }
        return (verticalMovement + buoyancy * (DEFAULT_GRAVITY / BUOYANCY_GRAVITY_DIVISOR)) * BUOYANCY_DAMPING;
    }

    public static float landFrictionAfterTick(float landFriction, boolean playerControlled) {
        if (!playerControlled) {
            return landFriction;
        }
        return landFriction / LAND_FRICTION_PLAYER_DIVISOR;
    }

    public static double horizontalAfterFriction(double horizontal, float invFriction) {
        return horizontal * invFriction;
    }

    public static double bubbleColumnImpulse(boolean downward, boolean hasPlayer) {
        if (downward) {
            return BUBBLE_DOWNWARD;
        }
        return hasPlayer ? BUBBLE_UPWARD_WITH_PLAYER : BUBBLE_UPWARD_EMPTY;
    }

    public static Reality bubbleImpulseFrom(boolean downward, boolean hasPlayer) {
        return Reality.of(bubbleColumnImpulse(downward, hasPlayer));
    }

    public static Reality gravityFrom() {
        return Reality.of(DEFAULT_GRAVITY);
    }
}
