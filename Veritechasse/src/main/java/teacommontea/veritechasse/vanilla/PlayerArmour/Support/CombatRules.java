package teacommontea.veritechasse.Vanilla.PlayerArmour.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Support.ProtectionModifiers;
import teacommontea.veritechasse.Vanilla.Era;

public final class CombatRules {

    public static final float MAX_ARMOR = 20.0F;
    public static final float ARMOR_PROTECTION_DIVIDER = 25.0F;
    public static final float BASE_ARMOR_TOUGHNESS = 2.0F;
    public static final float MIN_ARMOR_RATIO = 0.2F;
    public static final int NUM_ARMOR_ITEMS = 4;

    private CombatRules() {
    }

    public static float effectiveToughness(float armorToughness) {
        return BASE_ARMOR_TOUGHNESS + armorToughness / 4.0F;
    }

    public static float realArmor(float totalArmor, float damage, float armorToughness) {
        float toughness = effectiveToughness(armorToughness);
        float reduced = totalArmor - damage / toughness;
        float floor = totalArmor * MIN_ARMOR_RATIO;
        if (reduced < floor) {
            reduced = floor;
        }
        return reduced > MAX_ARMOR ? MAX_ARMOR : reduced;
    }

    public static float armorFraction(float totalArmor, float damage, float armorToughness) {
        return realArmor(totalArmor, damage, armorToughness) / ARMOR_PROTECTION_DIVIDER;
    }

    public static float damageAfterAbsorb(float damage, float totalArmor, float armorToughness) {
        return damage * (1.0F - armorFraction(totalArmor, damage, armorToughness));
    }

    public static float damageAfterAbsorb(
            float damage,
            float totalArmor,
            float armorToughness,
            ItemStack weapon,
            Era era) {
        float fraction = armorFraction(totalArmor, damage, armorToughness);
        float modified = ProtectionModifiers.armorEffectiveness(weapon, fraction, era);
        return damage * (1.0F - modified);
    }

    public static float damageAfterMagicAbsorb(float damage, float totalMagicArmor) {
        return ProtectionModifiers.damageAfterProtection(damage, totalMagicArmor);
    }

    public static float damageAfterArmourAndProtection(
            float damage,
            float totalArmor,
            float armorToughness,
            ItemStack weapon,
            ItemStack helmet,
            ItemStack chestplate,
            ItemStack leggings,
            ItemStack boots,
            boolean isFire,
            boolean isExplosion,
            boolean isProjectile,
            boolean isFall,
            Era era) {
        float afterArmour = damageAfterAbsorb(damage, totalArmor, armorToughness, weapon, era);
        float protection = ProtectionModifiers.totalProtection(
            helmet, chestplate, leggings, boots, isFire, isExplosion, isProjectile, isFall, era);
        return damageAfterMagicAbsorb(afterArmour, protection);
    }
}
