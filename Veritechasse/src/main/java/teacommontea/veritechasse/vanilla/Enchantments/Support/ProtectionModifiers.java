package teacommontea.veritechasse.Vanilla.Enchantments.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Enchantments.BlastProtection;
import teacommontea.veritechasse.Vanilla.Enchantments.Breach;
import teacommontea.veritechasse.Vanilla.Enchantments.FeatherFalling;
import teacommontea.veritechasse.Vanilla.Enchantments.FireProtection;
import teacommontea.veritechasse.Vanilla.Enchantments.ProjectileProtection;
import teacommontea.veritechasse.Vanilla.Enchantments.Protection;

public final class ProtectionModifiers {

    public static final float MAX_PROTECTION = 20.0F;
    public static final float PROTECTION_DIVISOR = 25.0F;

    private ProtectionModifiers() {
    }

    public static float protectionOn(ItemStack stack, boolean isFire, boolean isExplosion, boolean isProjectile, boolean isFall, Era era) {
        if (stack == null) {
            return 0.0F;
        }

        float total = 0.0F;
        total = total + Protection.damageProtection(Protection.levelOn(stack), era);

        if (isFire) {
            total = total + FireProtection.damageProtection(FireProtection.levelOn(stack), era);
        }
        if (isExplosion) {
            total = total + BlastProtection.damageProtection(BlastProtection.levelOn(stack), era);
        }
        if (isProjectile) {
            total = total + ProjectileProtection.damageProtection(ProjectileProtection.levelOn(stack), era);
        }
        if (isFall) {
            total = total + FeatherFalling.damageProtection(FeatherFalling.levelOn(stack), era);
        }

        return total;
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
        Era era
    ) {
        float total = 0.0F;
        total = total + protectionOn(helmet, isFire, isExplosion, isProjectile, isFall, era);
        total = total + protectionOn(chestplate, isFire, isExplosion, isProjectile, isFall, era);
        total = total + protectionOn(leggings, isFire, isExplosion, isProjectile, isFall, era);
        total = total + protectionOn(boots, isFire, isExplosion, isProjectile, isFall, era);
        return total;
    }

    public static float clampProtection(float totalProtection) {
        if (totalProtection < 0.0F) {
            return 0.0F;
        }
        return totalProtection > MAX_PROTECTION ? MAX_PROTECTION : totalProtection;
    }

    public static float damageAfterProtection(float damage, float totalProtection) {
        float clamped = clampProtection(totalProtection);
        return damage * (1.0F - clamped / PROTECTION_DIVISOR);
    }

    public static float armorEffectiveness(ItemStack weapon, float armorFraction, Era era) {
        if (weapon == null) {
            return armorFraction;
        }
        float modified = Breach.armorEffectiveness(armorFraction, Breach.levelOn(weapon), era);
        if (modified < 0.0F) {
            return 0.0F;
        }
        return modified > 1.0F ? 1.0F : modified;
    }
}
