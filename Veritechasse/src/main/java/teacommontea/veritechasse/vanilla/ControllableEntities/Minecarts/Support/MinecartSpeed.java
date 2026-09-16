package teacommontea.veritechasse.Vanilla.ControllableEntities.Minecarts.Support;

import teacommontea.veritechasse.Vanilla.Reality;

public final class MinecartSpeed {

    public static final double OLD_MAX_SPEED = 0.4D;
    public static final double OLD_MAX_SPEED_IN_WATER = 0.2D;

    public static final int NEW_SPEED_RULE_DEFAULT = 8;
    public static final int NEW_SPEED_RULE_MINIMUM = 1;
    public static final int NEW_SPEED_RULE_MAXIMUM = 1000;
    public static final double NEW_SPEED_RULE_DIVISOR = 20.0D;
    public static final double NEW_WATER_MULTIPLIER = 0.5D;

    public static final double OLD_SLOWDOWN_EMPTY = 0.96D;
    public static final double OLD_SLOWDOWN_OCCUPIED = 0.997D;
    public static final double NEW_SLOWDOWN_EMPTY = 0.975D;
    public static final double NEW_SLOWDOWN_OCCUPIED = 0.997D;

    private MinecartSpeed() {
    }

    public static double maxSpeed(MinecartBehaviour behaviour, boolean inWater, int maxMinecartSpeedRule) {
        if (behaviour == MinecartBehaviour.NEW) {
            double base = maxMinecartSpeedRule / NEW_SPEED_RULE_DIVISOR;
            return inWater ? base * NEW_WATER_MULTIPLIER : base;
        }
        return inWater ? OLD_MAX_SPEED_IN_WATER : OLD_MAX_SPEED;
    }

    public static double maxSpeed(MinecartBehaviour behaviour, boolean inWater) {
        return maxSpeed(behaviour, inWater, NEW_SPEED_RULE_DEFAULT);
    }

    public static double slowdownFactor(MinecartBehaviour behaviour, boolean occupied) {
        if (behaviour == MinecartBehaviour.NEW) {
            return occupied ? NEW_SLOWDOWN_OCCUPIED : NEW_SLOWDOWN_EMPTY;
        }
        return occupied ? OLD_SLOWDOWN_OCCUPIED : OLD_SLOWDOWN_EMPTY;
    }

    public static boolean ruleIsLegal(int maxMinecartSpeedRule) {
        return maxMinecartSpeedRule >= NEW_SPEED_RULE_MINIMUM
            && maxMinecartSpeedRule <= NEW_SPEED_RULE_MAXIMUM;
    }

    public static double speedAfterSlowdown(double speed, MinecartBehaviour behaviour, boolean occupied) {
        return speed * slowdownFactor(behaviour, occupied);
    }

    public static Reality speedFrom(MinecartBehaviour behaviour, boolean inWater, int maxMinecartSpeedRule) {
        return Reality.of(maxSpeed(behaviour, inWater, maxMinecartSpeedRule));
    }

    public static Reality speedFrom(MinecartBehaviour behaviour, boolean inWater) {
        return Reality.of(maxSpeed(behaviour, inWater));
    }
}
