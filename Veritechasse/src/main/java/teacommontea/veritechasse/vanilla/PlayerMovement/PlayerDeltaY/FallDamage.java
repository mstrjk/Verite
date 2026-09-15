package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.vanilla.Potions.JumpBoost;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class FallDamage {

    public static final double LEGACY_SAFE_FALL_DISTANCE = 3.0D;
    public static final double DEFAULT_SAFE_FALL_DISTANCE = 3.0D;
    public static final double DEFAULT_FALL_DAMAGE_MULTIPLIER = 1.0D;

    public static final double FALL_POWER_EPSILON = 1.0E-6D;

    public static final int ATTRIBUTE_PROTOCOL_MAJOR = 1;
    public static final int ATTRIBUTE_PROTOCOL_MINOR = 20;
    public static final int ATTRIBUTE_PROTOCOL_PATCH = 5;

    public static final int FLOOR_PROTOCOL_MAJOR = 1;
    public static final int FLOOR_PROTOCOL_MINOR = 21;
    public static final int FLOOR_PROTOCOL_PATCH = 5;

    private FallDamage() {
    }

    public static boolean attributesApply(Protocol protocol) {
        return protocol.atLeast(
            ATTRIBUTE_PROTOCOL_MAJOR,
            ATTRIBUTE_PROTOCOL_MINOR,
            ATTRIBUTE_PROTOCOL_PATCH);
    }

    public static boolean usesFloor(Protocol protocol) {
        return protocol.atLeast(
            FLOOR_PROTOCOL_MAJOR,
            FLOOR_PROTOCOL_MINOR,
            FLOOR_PROTOCOL_PATCH);
    }

    public static double safeFallDistance(ActiveEffects effects, Protocol protocol) {
        if (!attributesApply(protocol)) {
            return LEGACY_SAFE_FALL_DISTANCE;
        }
        int amplifier = effects == null ? ActiveEffects.ABSENT : effects.amplifierOf(JumpBoost.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return Attributes.SAFE_FALL_DISTANCE.base();
        }
        return JumpBoost.safeFallDistance(amplifier, protocol);
    }

    public static double legacyJumpBoostReduction(ActiveEffects effects) {
        if (effects == null) {
            return 0.0D;
        }
        int amplifier = effects.amplifierOf(JumpBoost.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return 0.0D;
        }
        return (double) (amplifier + 1);
    }

    public static double fallPower(double fallDistance, ActiveEffects effects, Protocol protocol) {
        if (!usesFloor(protocol)) {
            return fallDistance - safeFallDistance(effects, protocol);
        }
        return fallDistance + FALL_POWER_EPSILON - safeFallDistance(effects, protocol);
    }

    public static int damage(
            double fallDistance,
            float damageModifier,
            ActiveEffects effects,
            double fallDamageMultiplier,
            Protocol protocol) {
        if (!attributesApply(protocol)) {
            double reduced = fallDistance - LEGACY_SAFE_FALL_DISTANCE - legacyJumpBoostReduction(effects);
            return ceil(reduced * (double) damageModifier);
        }
        double multiplier = Attributes.FALL_DAMAGE_MULTIPLIER.sanitize(fallDamageMultiplier);
        double power = fallPower(fallDistance, effects, protocol);
        double raw = power * (double) damageModifier * multiplier;
        return usesFloor(protocol) ? floor(raw) : ceil(raw);
    }

    public static int damage(double fallDistance, ActiveEffects effects, Protocol protocol) {
        return damage(
            fallDistance,
            1.0F,
            effects,
            DEFAULT_FALL_DAMAGE_MULTIPLIER,
            protocol);
    }

    public static float damageModifierOf(String landedOn) {
        return LandingBlocks.damageModifierOf(landedOn);
    }

    public static double effectiveDistanceOn(String landedOn, double fallDistance, Protocol protocol) {
        return LandingBlocks.effectiveDistance(landedOn, fallDistance, protocol);
    }

    public static boolean immune(boolean mayFly) {
        return mayFly;
    }

    public static boolean expectsDamage(double fallDistance, ActiveEffects effects, Protocol protocol) {
        return damage(fallDistance, effects, protocol) > 0;
    }

    public static boolean expectsDamageOn(
            String landedOn,
            double fallDistance,
            boolean mayFly,
            ActiveEffects effects,
            Protocol protocol) {
        if (immune(mayFly)) {
            return false;
        }
        double distance = effectiveDistanceOn(landedOn, fallDistance, protocol);
        double multiplier = Attributes.FALL_DAMAGE_MULTIPLIER.base();
        return damage(distance, damageModifierOf(landedOn), effects, multiplier, protocol) > 0;
    }

    public static double minimumDamagingDistance(ActiveEffects effects, Protocol protocol) {
        double safe = attributesApply(protocol)
            ? safeFallDistance(effects, protocol)
            : LEGACY_SAFE_FALL_DISTANCE + legacyJumpBoostReduction(effects);
        if (usesFloor(protocol)) {
            return safe + 1.0D - FALL_POWER_EPSILON;
        }
        return safe;
    }

    public static boolean landingContradictsDamage(
            double fallDistance,
            boolean damageObserved,
            ActiveEffects effects,
            Protocol protocol) {
        return expectsDamage(fallDistance, effects, protocol) != damageObserved;
    }

    public static int ceil(double value) {
        int truncated = (int) value;
        return value > (double) truncated ? truncated + 1 : truncated;
    }

    public static int floor(double value) {
        int truncated = (int) value;
        return value < (double) truncated ? truncated - 1 : truncated;
    }

    public static Reality damageFrom(double fallDistance, ActiveEffects effects, Protocol protocol) {
        return Reality.of(damage(fallDistance, effects, protocol));
    }
}
