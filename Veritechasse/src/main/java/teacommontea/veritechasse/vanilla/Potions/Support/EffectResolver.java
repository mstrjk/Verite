package teacommontea.veritechasse.vanilla.Potions.Support;

import java.util.ArrayList;
import java.util.List;

import teacommontea.veritechasse.vanilla.Potions.Haste;
import teacommontea.veritechasse.vanilla.Potions.HealthBoost;
import teacommontea.veritechasse.vanilla.Potions.JumpBoost;
import teacommontea.veritechasse.vanilla.Potions.MiningFatigue;
import teacommontea.veritechasse.vanilla.Potions.Resistance;
import teacommontea.veritechasse.vanilla.Potions.Slowness;
import teacommontea.veritechasse.vanilla.Potions.Speed;
import teacommontea.veritechasse.vanilla.Potions.Strength;
import teacommontea.veritechasse.vanilla.Potions.Weakness;
import teacommontea.veritechasse.vanilla.Protocol;

public final class EffectResolver {

    private EffectResolver() {
    }

    public static final double SPRINT_BONUS = 0.30000001192092896D;

    public static double movementSpeed(double baseSpeed, ActiveEffects effects, boolean sprinting) {
        List<Modifier> modifiers = new ArrayList<>();

        int speed = effects == null ? ActiveEffects.ABSENT : effects.amplifierOf(Speed.KEY);
        if (speed != ActiveEffects.ABSENT) {
            modifiers.add(Speed.modifier(speed));
        }

        int slowness = effects == null ? ActiveEffects.ABSENT : effects.amplifierOf(Slowness.KEY);
        if (slowness != ActiveEffects.ABSENT) {
            modifiers.add(Slowness.modifier(slowness));
        }

        if (sprinting) {
            modifiers.add(new Modifier(SPRINT_BONUS, Operation.ADD_MULTIPLIED_TOTAL));
        }

        if (modifiers.isEmpty()) {
            return baseSpeed;
        }

        Ranged attribute = new Ranged(
            baseSpeed, Attributes.MOVEMENT_SPEED.min(), Attributes.MOVEMENT_SPEED.max());
        return AttributePipeline.resolve(attribute, modifiers);
    }

    public static double movementSpeed(double baseSpeed, ActiveEffects effects) {
        List<Modifier> modifiers = new ArrayList<>();

        int speed = effects.amplifierOf(Speed.KEY);
        if (speed != ActiveEffects.ABSENT) {
            modifiers.add(Speed.modifier(speed));
        }

        int slowness = effects.amplifierOf(Slowness.KEY);
        if (slowness != ActiveEffects.ABSENT) {
            modifiers.add(Slowness.modifier(slowness));
        }

        if (modifiers.isEmpty()) {
            return baseSpeed;
        }

        Ranged attribute = new Ranged(baseSpeed, Attributes.MOVEMENT_SPEED.min(), Attributes.MOVEMENT_SPEED.max());
        return AttributePipeline.resolve(attribute, modifiers);
    }

    public static double attackDamage(double baseDamage, ActiveEffects effects) {
        List<Modifier> modifiers = new ArrayList<>();

        int strength = effects.amplifierOf(Strength.KEY);
        if (strength != ActiveEffects.ABSENT) {
            modifiers.add(Strength.modifier(strength));
        }

        int weakness = effects.amplifierOf(Weakness.KEY);
        if (weakness != ActiveEffects.ABSENT) {
            modifiers.add(Weakness.modifier(weakness));
        }

        if (modifiers.isEmpty()) {
            return baseDamage;
        }

        Ranged attribute = new Ranged(baseDamage, Attributes.ATTACK_DAMAGE.min(), Attributes.ATTACK_DAMAGE.max());
        return AttributePipeline.resolve(attribute, modifiers);
    }

    public static double attackSpeed(double baseAttackSpeed, ActiveEffects effects) {
        List<Modifier> modifiers = new ArrayList<>();

        int haste = effects.amplifierOf(Haste.KEY);
        if (haste != ActiveEffects.ABSENT) {
            modifiers.add(Haste.attackSpeedModifier(haste));
        }

        int fatigue = effects.amplifierOf(MiningFatigue.KEY);
        if (fatigue != ActiveEffects.ABSENT) {
            modifiers.add(MiningFatigue.attackSpeedModifier(fatigue));
        }

        if (modifiers.isEmpty()) {
            return baseAttackSpeed;
        }

        Ranged attribute = new Ranged(baseAttackSpeed, Attributes.ATTACK_SPEED.min(), Attributes.ATTACK_SPEED.max());
        return AttributePipeline.resolve(attribute, modifiers);
    }

    public static double maxHealth(double baseMaxHealth, ActiveEffects effects) {
        int boost = effects.amplifierOf(HealthBoost.KEY);
        if (boost == ActiveEffects.ABSENT) {
            return baseMaxHealth;
        }
        return HealthBoost.maxHealthFrom(baseMaxHealth, boost);
    }

    public static float damageAfterResistance(float damage, ActiveEffects effects, String damageType, EffectEra era) {
        int resistance = effects.amplifierOf(Resistance.KEY);
        if (resistance == ActiveEffects.ABSENT) {
            return damage;
        }
        return Resistance.damageAfter(damage, resistance, damageType, era);
    }

    public static float digSpeed(float baseSpeed, ActiveEffects effects) {
        int haste = effects.amplifierOf(Haste.KEY);
        int conduit = effects.amplifierOf("conduit_power");
        int fatigue = effects.amplifierOf(MiningFatigue.KEY);
        return DigSpeed.apply(baseSpeed, haste, conduit, fatigue);
    }

    public static float jumpPower(float baseJumpStrength, float blockJumpFactor, ActiveEffects effects) {
        int boost = effects.amplifierOf(JumpBoost.KEY);
        if (boost == ActiveEffects.ABSENT) {
            return baseJumpStrength * blockJumpFactor;
        }
        return JumpBoost.jumpPower(baseJumpStrength, blockJumpFactor, boost);
    }

    public static double safeFallDistance(ActiveEffects effects, Protocol protocol) {
        int boost = effects.amplifierOf(JumpBoost.KEY);
        if (boost == ActiveEffects.ABSENT) {
            return Attributes.SAFE_FALL_DISTANCE.base();
        }
        return JumpBoost.safeFallDistance(boost, protocol);
    }
}
