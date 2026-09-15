package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import java.util.Locale;
import java.util.Map;

import teacommontea.veritechasse.vanilla.Reality;

public final class ExplosionKnockback {

    public static final float TNT_RADIUS = 4.0F;
    public static final float CREEPER_RADIUS = 3.0F;
    public static final float CHARGED_CREEPER_MULTIPLIER = 2.0F;
    public static final float END_CRYSTAL_RADIUS = 6.0F;
    public static final float BED_RADIUS = 5.0F;
    public static final float RESPAWN_ANCHOR_RADIUS = 5.0F;

    public static final float WIND_CHARGE_RADIUS = 1.2F;
    public static final float WIND_CHARGE_KNOCKBACK_MULTIPLIER = 1.22F;

    public static final float DEFAULT_KNOCKBACK_MULTIPLIER = 1.0F;
    public static final double DEFAULT_KNOCKBACK_RESISTANCE = 0.0D;
    public static final double RADIUS_DOUBLING = 2.0D;

    public static final boolean APPLIES_TO_NON_LIVING = true;

    private static final Map<String, Float> RADIUS_BY_SOURCE = Map.of(
        "tnt", Float.valueOf(TNT_RADIUS),
        "creeper", Float.valueOf(CREEPER_RADIUS),
        "end_crystal", Float.valueOf(END_CRYSTAL_RADIUS),
        "bed", Float.valueOf(BED_RADIUS),
        "respawn_anchor", Float.valueOf(RESPAWN_ANCHOR_RADIUS),
        "wind_charge", Float.valueOf(WIND_CHARGE_RADIUS));

    private static final Map<String, Float> MULTIPLIER_BY_SOURCE = Map.of(
        "wind_charge", Float.valueOf(WIND_CHARGE_KNOCKBACK_MULTIPLIER));

    private ExplosionKnockback() {
    }

    public static float radiusOf(String source) {
        if (source == null) {
            return 0.0F;
        }
        Float found = RADIUS_BY_SOURCE.get(source.toLowerCase(Locale.ROOT));
        return found == null ? 0.0F : found.floatValue();
    }

    public static float knockbackMultiplierOf(String source) {
        if (source == null) {
            return DEFAULT_KNOCKBACK_MULTIPLIER;
        }
        Float found = MULTIPLIER_BY_SOURCE.get(source.toLowerCase(Locale.ROOT));
        return found == null ? DEFAULT_KNOCKBACK_MULTIPLIER : found.floatValue();
    }

    public static double knockbackFor(String source, double distanceToCentre, float exposure) {
        return knockbackPower(
            distanceToCentre,
            radiusOf(source),
            exposure,
            knockbackMultiplierOf(source),
            DEFAULT_KNOCKBACK_RESISTANCE);
    }

    public static float creeperRadius(int explosionRadiusNbt, boolean charged) {
        float multiplier = charged ? CHARGED_CREEPER_MULTIPLIER : 1.0F;
        return explosionRadiusNbt * multiplier;
    }

    public static double effectiveRadius(float radius) {
        return radius * RADIUS_DOUBLING;
    }

    public static double normalisedDistance(double distanceToCentre, float radius) {
        double doubled = effectiveRadius(radius);
        if (doubled <= 0.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return distanceToCentre / doubled;
    }

    public static boolean withinRange(double distanceToCentre, float radius) {
        return normalisedDistance(distanceToCentre, radius) <= 1.0D;
    }

    public static double knockbackPower(
            double distanceToCentre,
            float radius,
            float exposure,
            float knockbackMultiplier,
            double knockbackResistance) {
        double normalised = normalisedDistance(distanceToCentre, radius);
        if (normalised > 1.0D) {
            return 0.0D;
        }
        return (1.0D - normalised) * exposure * knockbackMultiplier * (1.0D - knockbackResistance);
    }

    public static double maxKnockbackPower(float radius) {
        return knockbackPower(0.0D, radius, 1.0F, DEFAULT_KNOCKBACK_MULTIPLIER, DEFAULT_KNOCKBACK_RESISTANCE);
    }

    public static double maxKnockbackPowerFor(String source) {
        return knockbackPower(
            0.0D,
            radiusOf(source),
            1.0F,
            knockbackMultiplierOf(source),
            DEFAULT_KNOCKBACK_RESISTANCE);
    }

    public static double vehicleKnockbackPower(double distanceToCentre, float radius, float exposure) {
        return knockbackPower(
            distanceToCentre,
            radius,
            exposure,
            DEFAULT_KNOCKBACK_MULTIPLIER,
            DEFAULT_KNOCKBACK_RESISTANCE);
    }

    public static Reality knockbackFrom(double distanceToCentre, float radius, float exposure) {
        return Reality.of(vehicleKnockbackPower(distanceToCentre, radius, exposure));
    }

    public static Reality maxKnockbackFrom(String source) {
        return Reality.of(maxKnockbackPower(radiusOf(source)));
    }
}
