package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerAggressor;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerHunger.Exhaustion;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Swords.Support.AttackPipeline;
import teacommontea.veritechasse.Vanilla.Tools.Swords.Support.AttackStrength;

public final class AggressorReality {

    public static final float TOLERANCE = 1.0E-4F;

    public static final double KNOCKBACK_SLOWDOWN = 0.6D;

    private AggressorReality() {
    }

    public static float outgoingDamage(
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
        return AttackResolution.maximumDamage(
            weapon, target, baseAttackDamage, attackStrengthScale, weaponBonus,
            fallDistance, onGround, onClimbable, inWater, effects,
            passenger, targetIsLiving, sprinting, era);
    }

    public static float outgoingKnockback(
            ItemStack weapon,
            float baseKnockback,
            boolean sprinting,
            float attackStrengthScale,
            Era era) {
        return AttackPipeline.resolveKnockback(
            weapon, baseKnockback,
            isKnockbackAttack(sprinting, attackStrengthScale), era);
    }

    public static double knockbackPower(
            int knockbackEnchantLevel,
            boolean sprinting,
            float attackStrengthScale) {
        return AttackResolution.knockbackPower(
            knockbackEnchantLevel, isKnockbackAttack(sprinting, attackStrengthScale));
    }

    public static boolean isKnockbackAttack(boolean sprinting, float attackStrengthScale) {
        return AttackResolution.knockbackAttack(sprinting, attackStrengthScale);
    }

    public static double horizontalAfterKnockbackAttack(double currentHorizontal) {
        return currentHorizontal * KNOCKBACK_SLOWDOWN;
    }

    public static boolean keptSprintingAfterKnockback(
            boolean sprintingBefore,
            boolean sprintingAfter,
            boolean knockbackAttack) {
        if (!knockbackAttack || !sprintingBefore) {
            return false;
        }
        return sprintingAfter;
    }

    public static boolean movedTooFastAfterKnockback(
            double observedHorizontal,
            double horizontalBefore,
            boolean knockbackAttack) {
        if (!knockbackAttack) {
            return false;
        }
        double bound = horizontalAfterKnockbackAttack(horizontalBefore);
        return observedHorizontal > bound + TOLERANCE;
    }

    public static boolean attackedOutOfReach(
            double distanceSquaredToTarget,
            boolean creative,
            Protocol protocol) {
        return AttackReach.exceedsReach(distanceSquaredToTarget, creative, protocol);
    }

    public static boolean attackedTooFast(
            int ticksSinceLastAttack,
            double attackSpeed,
            float partialTick) {
        float scale = AttackStrength.scale(ticksSinceLastAttack, attackSpeed, partialTick);
        return scale <= 0.0F;
    }

    public static float expectedAttackStrength(
            int ticksSinceLastAttack,
            double attackSpeed,
            float partialTick) {
        return AttackStrength.scale(ticksSinceLastAttack, attackSpeed, partialTick);
    }

    public static boolean claimedStrengthIsImpossible(
            float claimedScale,
            int ticksSinceLastAttack,
            double attackSpeed,
            float partialTick) {
        float bound = expectedAttackStrength(ticksSinceLastAttack, attackSpeed, partialTick);
        return claimedScale > bound + TOLERANCE;
    }

    public static float exhaustionPerAttack() {
        return Exhaustion.ATTACK;
    }

    public static boolean damageExceedsBound(float observedDamage, float expectedDamage) {
        return observedDamage > expectedDamage + TOLERANCE;
    }

    public static Reality damageFrom(
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
        return Reality.of(outgoingDamage(
            weapon, target, baseAttackDamage, attackStrengthScale, weaponBonus,
            fallDistance, onGround, onClimbable, inWater, effects,
            passenger, targetIsLiving, sprinting, era));
    }

    public static Reality reachFrom(boolean creative, Protocol protocol) {
        return AttackReach.reachFrom(creative, protocol);
    }
}
