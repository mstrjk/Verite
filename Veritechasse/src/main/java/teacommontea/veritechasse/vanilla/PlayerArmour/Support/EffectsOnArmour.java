package teacommontea.veritechasse.Vanilla.PlayerArmour.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Helmets.TurtleHelmet;
import teacommontea.veritechasse.Vanilla.Potions.ConduitPower;
import teacommontea.veritechasse.Vanilla.Potions.FireResistance;
import teacommontea.veritechasse.Vanilla.Potions.Resistance;
import teacommontea.veritechasse.Vanilla.Potions.SlowFalling;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.Vanilla.Potions.WaterBreathing;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class EffectsOnArmour {

    private EffectsOnArmour() {
    }

    public static float damageAfterResistance(float damage, ActiveEffects effects) {
        if (effects == null) {
            return damage;
        }
        int amplifier = effects.amplifierOf(Resistance.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return damage;
        }
        return Resistance.damageAfter(damage, amplifier);
    }

    public static float damageAfterResistance(
            float damage,
            ActiveEffects effects,
            String damageType,
            EffectEra era) {
        if (effects == null) {
            return damage;
        }
        int amplifier = effects.amplifierOf(Resistance.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return damage;
        }
        return Resistance.damageAfter(damage, amplifier, damageType, era);
    }

    public static float damageAfterFireResistance(
            float damage,
            ActiveEffects effects,
            String damageType,
            EffectEra era) {
        if (effects == null || !effects.has(FireResistance.KEY)) {
            return damage;
        }
        return FireResistance.damageAfter(damage, damageType, era);
    }

    public static boolean breathesUnderwater(ActiveEffects effects) {
        if (effects == null) {
            return false;
        }
        if (effects.has(WaterBreathing.KEY)) {
            return WaterBreathing.preventsDrowning();
        }
        return effects.has(ConduitPower.KEY) && ConduitPower.grantsWaterBreathing();
    }

    public static boolean breathesUnderwater(ActiveEffects effects, ItemStack helmet, boolean eyeInWater) {
        if (breathesUnderwater(effects)) {
            return true;
        }
        return TurtleHelmet.grantsWaterBreathing(helmet, eyeInWater);
    }

    public static double effectiveGravity(double gravity, double deltaY, ActiveEffects effects, Protocol protocol) {
        if (effects == null || !effects.has(SlowFalling.KEY)) {
            return gravity;
        }
        return SlowFalling.effectiveGravity(gravity, deltaY, protocol);
    }

    public static boolean negatesSubmergedMiningPenalty(ActiveEffects effects) {
        if (effects == null) {
            return false;
        }
        return effects.has(ConduitPower.KEY) && ConduitPower.negatesSubmergedMiningPenalty();
    }

    public static float damageAfterArmourEffectsAndResistance(
            float damage,
            float totalArmour,
            float totalToughness,
            ItemStack weapon,
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            String damageType,
            ActiveEffects effects,
            Era era,
            EffectEra effectEra) {
        float afterFireResistance = damageAfterFireResistance(damage, effects, damageType, effectEra);
        if (afterFireResistance <= 0.0F) {
            return 0.0F;
        }
        float afterArmour = CombatRules.damageAfterArmourAndProtection(
            afterFireResistance,
            totalArmour,
            totalToughness,
            weapon,
            helmet,
            chestplate,
            leggings,
            boots,
            isFire,
            isExplosion,
            isProjectile,
            isFall,
            era);
        return damageAfterResistance(afterArmour, effects, damageType, effectEra);
    }
}
