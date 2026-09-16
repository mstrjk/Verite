package teacommontea.veritechasse.Vanilla.Potions.Support;

import org.bukkit.entity.EntityType;

import teacommontea.veritechasse.Vanilla.Potions.Absorption;
import teacommontea.veritechasse.Vanilla.Potions.Hunger;
import teacommontea.veritechasse.Vanilla.Potions.InstantDamage;
import teacommontea.veritechasse.Vanilla.Potions.InstantHealth;
import teacommontea.veritechasse.Vanilla.Potions.Levitation;
import teacommontea.veritechasse.Vanilla.Potions.Poison;
import teacommontea.veritechasse.Vanilla.Potions.Regeneration;
import teacommontea.veritechasse.Vanilla.Potions.Saturation;
import teacommontea.veritechasse.Vanilla.Potions.Wither;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class HealthResolver {

    private HealthResolver() {
    }

    public static boolean poisonAppliesThisTick(ActiveEffects effects, int tickCount) {
        if (effects == null) {
            return false;
        }
        int amplifier = effects.amplifierOf(Poison.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return false;
        }
        return Poison.appliesThisTick(amplifier, tickCount);
    }

    public static float poisonDamage(ActiveEffects effects, float currentHealth, int tickCount) {
        if (!poisonAppliesThisTick(effects, tickCount)) {
            return 0.0F;
        }
        if (currentHealth <= Poison.MINIMUM_HEALTH) {
            return 0.0F;
        }
        return Poison.DAMAGE_PER_APPLICATION;
    }

    public static boolean witherAppliesThisTick(ActiveEffects effects, int tickCount) {
        if (effects == null) {
            return false;
        }
        int amplifier = effects.amplifierOf(Wither.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return false;
        }
        return Wither.appliesThisTick(amplifier, tickCount);
    }

    public static float witherDamage(ActiveEffects effects, int tickCount) {
        if (!witherAppliesThisTick(effects, tickCount)) {
            return 0.0F;
        }
        return Wither.DAMAGE_PER_APPLICATION;
    }

    public static boolean regenerationAppliesThisTick(ActiveEffects effects, int tickCount) {
        if (effects == null) {
            return false;
        }
        int amplifier = effects.amplifierOf(Regeneration.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return false;
        }
        return Regeneration.appliesThisTick(amplifier, tickCount);
    }

    public static float regenerationHealing(ActiveEffects effects, int tickCount) {
        if (!regenerationAppliesThisTick(effects, tickCount)) {
            return 0.0F;
        }
        return Regeneration.HEAL_PER_APPLICATION;
    }

    public static double absorptionHearts(ActiveEffects effects) {
        if (effects == null) {
            return 0.0D;
        }
        int amplifier = effects.amplifierOf(Absorption.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return 0.0D;
        }
        return Absorption.granted(amplifier);
    }

    public static int instantHealthAmount(ActiveEffects effects, EntityType target, EffectEra era) {
        if (effects == null) {
            return 0;
        }
        int amplifier = effects.amplifierOf(InstantHealth.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return 0;
        }
        boolean inverted = InstantHealth.invertedFor(target, era);
        if (!InstantHealth.healsTarget(inverted)) {
            return 0;
        }
        return InstantHealth.healAmount(amplifier);
    }

    public static int instantDamageAmount(ActiveEffects effects, EntityType target, EffectEra era) {
        if (effects == null) {
            return 0;
        }
        int amplifier = effects.amplifierOf(InstantDamage.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return 0;
        }
        boolean inverted = InstantDamage.invertedFor(target, era);
        if (InstantDamage.healsTarget(inverted)) {
            return 0;
        }
        return InstantDamage.healAmount(amplifier);
    }

    public static float hungerExhaustionPerTick(ActiveEffects effects) {
        if (effects == null) {
            return 0.0F;
        }
        int amplifier = effects.amplifierOf(Hunger.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return 0.0F;
        }
        return Hunger.exhaustionPerTick(amplifier);
    }

    public static int foodAfterSaturation(ActiveEffects effects, int currentFood, Protocol protocol) {
        if (effects == null) {
            return currentFood;
        }
        int amplifier = effects.amplifierOf(Saturation.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return currentFood;
        }
        return Saturation.foodAfter(currentFood, amplifier, protocol);
    }

    public static double levitationVelocity(ActiveEffects effects, double currentY) {
        if (effects == null) {
            return currentY;
        }
        int amplifier = effects.amplifierOf(Levitation.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return currentY;
        }
        return Levitation.nextVelocity(currentY, amplifier);
    }

    public static boolean levitating(ActiveEffects effects) {
        return effects != null && effects.has(Levitation.KEY);
    }

    public static Reality absorptionFrom(ActiveEffects effects) {
        return Reality.of(absorptionHearts(effects));
    }

    public static Reality levitationVelocityFrom(ActiveEffects effects, double currentY) {
        if (!levitating(effects)) {
            return Reality.impossible();
        }
        return Reality.of(levitationVelocity(effects, currentY));
    }
}
