package teacommontea.veritechasse.Vanilla.Tools.Swords.Support;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.DamageModifiers;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.KnockbackModifiers;
import teacommontea.veritechasse.Vanilla.Potions.Support.CriticalAttack;
import teacommontea.veritechasse.Vanilla.Reality;

public final class AttackPipeline {

    private AttackPipeline() {
    }

    public static float enchantedDamage(ItemStack weapon, EntityType target, float baseDamage, Era era) {
        return DamageModifiers.modifyDamage(weapon, target, baseDamage, era);
    }

    public static float resolve(
        ItemStack weapon,
        EntityType target,
        float baseAttackDamage,
        float attackStrengthScale,
        float weaponBonus,
        boolean criticalAttack,
        Era era
    ) {
        float enchanted = enchantedDamage(weapon, target, baseAttackDamage, era);
        return totalDamage(baseAttackDamage, enchanted, attackStrengthScale, weaponBonus, criticalAttack);
    }

    public static float resolveKnockback(ItemStack weapon, float baseKnockback, boolean knockbackAttack, Era era) {
        float withEnchants = KnockbackModifiers.modifyKnockback(weapon, baseKnockback, era);
        return knockbackStrength(withEnchants, knockbackAttack);
    }

    public static float magicBoost(float attackStrengthScale, float enchantedDamage, float baseDamage) {
        return attackStrengthScale * (enchantedDamage - baseDamage);
    }

    public static float chargedBase(float baseDamage, float attackStrengthScale) {
        return baseDamage * AttackStrength.damageScaleFactor(attackStrengthScale);
    }

    public static boolean permitsCritical(
        double fallDistance,
        boolean onGround,
        boolean onClimbable,
        boolean inWater,
        boolean blindness,
        boolean passenger,
        boolean targetIsLiving,
        boolean sprinting,
        boolean fullStrength
    ) {
        if (!fullStrength) {
            return false;
        }
        return CriticalAttack.permitted(fallDistance, onGround, onClimbable, inWater, blindness, passenger, targetIsLiving, sprinting);
    }

    public static float totalDamage(
        float baseAttackDamage,
        float enchantedDamage,
        float attackStrengthScale,
        float weaponBonus,
        boolean criticalAttack
    ) {
        float magic = magicBoost(attackStrengthScale, enchantedDamage, baseAttackDamage);
        float damage = chargedBase(baseAttackDamage, attackStrengthScale);
        damage = damage + weaponBonus;
        if (criticalAttack) {
            damage = damage * AttackStrength.CRIT_MULTIPLIER;
        }
        return damage + magic;
    }

    public static float knockbackStrength(float baseKnockback, boolean knockbackAttack) {
        return knockbackAttack ? baseKnockback + AttackStrength.KNOCKBACK_BONUS : baseKnockback;
    }

    public static Reality damageFrom(
        float baseAttackDamage,
        float enchantedDamage,
        float attackStrengthScale,
        float weaponBonus,
        boolean criticalAttack
    ) {
        return Reality.of(totalDamage(baseAttackDamage, enchantedDamage, attackStrengthScale, weaponBonus, criticalAttack));
    }
}
