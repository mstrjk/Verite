package teacommontea.veritechasse.vanilla.Tools.Heavy.Support;

import teacommontea.veritechasse.vanilla.Reality;

public final class SmashAttack {

    public static final float FALL_THRESHOLD = 1.5F;
    public static final float HEAVY_THRESHOLD = 5.0F;

    public static final double TIER_ONE_LIMIT = 3.0D;
    public static final double TIER_TWO_LIMIT = 8.0D;

    public static final double TIER_ONE_RATE = 4.0D;
    public static final double TIER_TWO_BASE = 12.0D;
    public static final double TIER_TWO_RATE = 2.0D;
    public static final double TIER_THREE_BASE = 22.0D;
    public static final double TIER_THREE_RATE = 1.0D;

    public static final float KNOCKBACK_RADIUS = 3.5F;
    public static final float KNOCKBACK_POWER = 0.7F;
    public static final float KNOCKBACK_VERTICAL = 0.7F;
    public static final int HEAVY_KNOCKBACK_MULTIPLIER = 2;

    public static final float LANDING_Y_MOTION = 0.01F;

    private SmashAttack() {
    }

    public static boolean canSmash(double fallDistance, boolean fallFlying) {
        return fallDistance > (double) FALL_THRESHOLD && !fallFlying;
    }

    public static boolean isHeavy(double fallDistance) {
        return fallDistance > (double) HEAVY_THRESHOLD;
    }

    public static double bonusDamage(double fallDistance) {
        if (fallDistance <= TIER_ONE_LIMIT) {
            return TIER_ONE_RATE * fallDistance;
        }
        if (fallDistance <= TIER_TWO_LIMIT) {
            return TIER_TWO_BASE + TIER_TWO_RATE * (fallDistance - TIER_ONE_LIMIT);
        }
        return TIER_THREE_BASE + TIER_THREE_RATE * (fallDistance - TIER_TWO_LIMIT);
    }

    public static double bonusDamage(double fallDistance, boolean fallFlying) {
        if (!canSmash(fallDistance, fallFlying)) {
            return 0.0D;
        }
        return bonusDamage(fallDistance);
    }

    public static double bonusDamageWithDensity(double fallDistance, float densityBonusPerBlock) {
        return bonusDamage(fallDistance) + (double) densityBonusPerBlock * fallDistance;
    }

    public static double knockbackPower(double distanceToTarget, double fallDistance, double knockbackResistance) {
        double multiplier = isHeavy(fallDistance) ? (double) HEAVY_KNOCKBACK_MULTIPLIER : 1.0D;
        return (KNOCKBACK_RADIUS - distanceToTarget) * (double) KNOCKBACK_POWER * multiplier * (1.0D - knockbackResistance);
    }

    public static boolean withinKnockbackRadius(double distanceSqr) {
        return distanceSqr <= (double) (KNOCKBACK_RADIUS * KNOCKBACK_RADIUS);
    }

    public static boolean negatesFallDamage() {
        return true;
    }

    public static boolean resetsFallDistance() {
        return true;
    }

    public static Reality damageFrom(double fallDistance, boolean fallFlying) {
        return Reality.of(bonusDamage(fallDistance, fallFlying));
    }
}
