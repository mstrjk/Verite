package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerAggressor;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Potions.Blindness;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Support.CriticalAttack;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Tools.Swords.Support.AttackPipeline;
import teacommontea.veritechasse.vanilla.Tools.Swords.Support.AttackStrength;
import teacommontea.veritechasse.vanilla.Tools.Swords.Support.SweepAttack;

public final class AttackResolution {

    public static final String KEY = "attack_resolution";

    public static final float KNOCKBACK_PER_LEVEL = 0.5F;

    private AttackResolution() {
    }

    public static boolean mobilityRestricted(ActiveEffects effects) {
        return effects != null && effects.has(Blindness.KEY);
    }

    public static float strengthScale(
            int ticksSinceLastAttack,
            double attackSpeed,
            float partialTick) {
        return AttackStrength.scale(ticksSinceLastAttack, attackSpeed, partialTick);
    }

    public static boolean fullStrength(float attackStrengthScale) {
        return AttackStrength.fullStrength(attackStrengthScale);
    }

    public static boolean fullStrength(
            int ticksSinceLastAttack,
            double attackSpeed,
            float partialTick) {
        return fullStrength(strengthScale(ticksSinceLastAttack, attackSpeed, partialTick));
    }

    public static boolean knockbackAttack(boolean sprinting, float attackStrengthScale) {
        return AttackStrength.isKnockbackAttack(sprinting, fullStrength(attackStrengthScale));
    }

    public static boolean criticalAttack(
            float attackStrengthScale,
            double fallDistance,
            boolean onGround,
            boolean onClimbable,
            boolean inWater,
            ActiveEffects effects,
            boolean passenger,
            boolean targetIsLiving,
            boolean sprinting) {
        if (!fullStrength(attackStrengthScale)) {
            return false;
        }
        return CriticalAttack.permitted(
            fallDistance, onGround, onClimbable, inWater,
            mobilityRestricted(effects), passenger, targetIsLiving, sprinting);
    }

    public static boolean sweepAttack(
            float attackStrengthScale,
            boolean criticalAttack,
            boolean knockbackAttack,
            boolean onGround,
            boolean holdingSword,
            double horizontalDistanceSqr,
            double walkedLastTick,
            double speed,
            Protocol protocol) {
        return SweepAttack.sweeps(
            fullStrength(attackStrengthScale), criticalAttack, knockbackAttack,
            onGround, holdingSword, horizontalDistanceSqr, walkedLastTick,
            speed, protocol);
    }

    public static int knockbackLevels(int knockbackEnchantLevel, boolean knockbackAttack) {
        int levels = knockbackEnchantLevel < 0 ? 0 : knockbackEnchantLevel;
        return knockbackAttack ? levels + 1 : levels;
    }

    public static double knockbackPower(int knockbackEnchantLevel, boolean knockbackAttack) {
        return (double) knockbackLevels(knockbackEnchantLevel, knockbackAttack)
            * (double) KNOCKBACK_PER_LEVEL;
    }

    public static boolean dealsKnockback(int knockbackEnchantLevel, boolean knockbackAttack) {
        return knockbackLevels(knockbackEnchantLevel, knockbackAttack) > 0;
    }

    public static float damage(
            ItemStack weapon,
            EntityType target,
            float baseAttackDamage,
            float attackStrengthScale,
            float weaponBonus,
            boolean criticalAttack,
            Era era) {
        return AttackPipeline.resolve(
            weapon, target, baseAttackDamage, attackStrengthScale,
            weaponBonus, criticalAttack, era);
    }

    public static float maximumDamage(
            ItemStack weapon,
            EntityType target,
            float baseAttackDamage,
            float attackStrengthScale,
            float weaponBonus,
            double fallDistance,
            boolean onGround,
            boolean onClimbable,
            boolean inWater,
            ActiveEffects effects,
            boolean passenger,
            boolean targetIsLiving,
            boolean sprinting,
            Era era) {
        boolean critical = criticalAttack(
            attackStrengthScale, fallDistance, onGround, onClimbable,
            inWater, effects, passenger, targetIsLiving, sprinting);
        return damage(weapon, target, baseAttackDamage, attackStrengthScale,
            weaponBonus, critical, era);
    }

    public static float maximumDamageFromState(
            ItemStack weapon,
            EntityType target,
            float baseAttackDamage,
            float weaponBonus,
            int ticksSinceLastAttack,
            double attackSpeed,
            float partialTick,
            double fallDistance,
            boolean onGround,
            boolean onClimbable,
            boolean inWater,
            ActiveEffects effects,
            boolean passenger,
            boolean targetIsLiving,
            boolean sprinting,
            Era era) {
        float scale = strengthScale(ticksSinceLastAttack, attackSpeed, partialTick);
        return maximumDamage(
            weapon, target, baseAttackDamage, scale, weaponBonus,
            fallDistance, onGround, onClimbable, inWater, effects,
            passenger, targetIsLiving, sprinting, era);
    }

    public static boolean claimedCriticalIsImpossible(
            boolean claimedCritical,
            float attackStrengthScale,
            double fallDistance,
            boolean onGround,
            boolean onClimbable,
            boolean inWater,
            ActiveEffects effects,
            boolean passenger,
            boolean targetIsLiving,
            boolean sprinting) {
        if (!claimedCritical) {
            return false;
        }
        return !criticalAttack(
            attackStrengthScale, fallDistance, onGround, onClimbable,
            inWater, effects, passenger, targetIsLiving, sprinting);
    }

    public static boolean claimedSweepIsImpossible(
            boolean claimedSweep,
            float attackStrengthScale,
            boolean criticalAttack,
            boolean knockbackAttack,
            boolean onGround,
            boolean holdingSword,
            double horizontalDistanceSqr,
            double walkedLastTick,
            double speed,
            Protocol protocol) {
        if (!claimedSweep) {
            return false;
        }
        return !sweepAttack(
            attackStrengthScale, criticalAttack, knockbackAttack, onGround,
            holdingSword, horizontalDistanceSqr, walkedLastTick, speed, protocol);
    }
}
