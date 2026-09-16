package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerVictim;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.ControllableEntities.Support.Knockback;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.ProtectionModifiers;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerArmour.Support.CombatRules;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.Vanilla.Potions.Support.EffectResolver;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class VictimReality {

    public static final float TOLERANCE = 1.0E-4F;

    public static final double KNOCKBACK_TOLERANCE = 1.0E-6D;

    private VictimReality() {
    }

    public static float expectedDamage(
            float incomingDamage,
            float totalArmour,
            float armourToughness,
            float totalProtection,
            ActiveEffects effects,
            String damageType,
            EffectEra era,
            boolean damagesHelmet,
            boolean wearingHelmet,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown,
            Protocol protocol) {
        float afterWindow = HurtWindow.appliedDamage(
            incomingDamage, lastHurt, invulnerableTime, bypassesCooldown, protocol);
        if (afterWindow <= 0.0F) {
            return 0.0F;
        }
        float afterHelmet = HurtWindow.afterHelmetReduction(
            afterWindow, damagesHelmet, wearingHelmet);
        float afterArmour = CombatRules.damageAfterAbsorb(
            afterHelmet, totalArmour, armourToughness);
        float afterProtection = ProtectionModifiers.damageAfterProtection(
            afterArmour, totalProtection);
        return afterEffects(afterProtection, effects, damageType, era);
    }

    public static float afterEffects(
            float damage,
            ActiveEffects effects,
            String damageType,
            EffectEra era) {
        if (effects == null) {
            return damage;
        }
        return EffectResolver.damageAfterResistance(damage, effects, damageType, era);
    }

    public static boolean tookTooLittleDamage(
            float observedHealthLost,
            float expectedDamage) {
        return observedHealthLost < expectedDamage - TOLERANCE;
    }

    public static boolean tookTooMuchDamage(
            float observedHealthLost,
            float expectedDamage) {
        return observedHealthLost > expectedDamage + TOLERANCE;
    }

    public static boolean damageDisagrees(
            float observedHealthLost,
            float expectedDamage) {
        return tookTooLittleDamage(observedHealthLost, expectedDamage)
            || tookTooMuchDamage(observedHealthLost, expectedDamage);
    }

    public static boolean hitDuringInvulnerability(
            float incomingDamage,
            float lastHurt,
            int invulnerableTime,
            boolean bypassesCooldown,
            boolean observedHealthChanged,
            Protocol protocol) {
        boolean absorbed = HurtWindow.absorbedEntirely(
            incomingDamage, lastHurt, invulnerableTime, bypassesCooldown, protocol);
        return absorbed && observedHealthChanged;
    }

    public static double expectedKnockback(
            double currentHorizontal,
            double power,
            double knockbackResistance) {
        return Knockback.maximumHorizontal(currentHorizontal, power, knockbackResistance);
    }

    public static boolean resistedTooMuchKnockback(
            double observedHorizontal,
            double expectedHorizontal) {
        return observedHorizontal < expectedHorizontal - KNOCKBACK_TOLERANCE;
    }

    public static boolean tookTooMuchKnockback(
            double observedHorizontal,
            double expectedHorizontal) {
        return observedHorizontal > expectedHorizontal + KNOCKBACK_TOLERANCE;
    }

    public static float totalProtection(
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            Era era) {
        return ProtectionModifiers.totalProtection(
            helmet, chestplate, leggings, boots,
            isFire, isExplosion, isProjectile, isFall, era);
    }

    public static Reality damageFrom(float expectedDamage) {
        return Reality.of(expectedDamage);
    }

    public static Reality knockbackFrom(
            double currentHorizontal,
            double power,
            double knockbackResistance) {
        return Knockback.horizontalFrom(currentHorizontal, power, knockbackResistance);
    }
}
