package teacommontea.veritechasse.vanilla.Enchantments.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Enchantments.Knockback;
import teacommontea.veritechasse.vanilla.Enchantments.Punch;

public final class KnockbackModifiers {

    private KnockbackModifiers() {
    }

    public static float meleeBonus(ItemStack stack, Era era) {
        if (stack == null) {
            return 0.0F;
        }
        return Knockback.knockback(Knockback.levelOn(stack), era);
    }

    public static float projectileBonus(ItemStack stack, Era era) {
        if (stack == null) {
            return 0.0F;
        }
        return Punch.knockback(Punch.levelOn(stack), era);
    }

    public static float modifyKnockback(ItemStack stack, float base, Era era) {
        return base + meleeBonus(stack, era);
    }

    public static float modifyProjectileKnockback(ItemStack stack, float base, Era era) {
        return base + projectileBonus(stack, era);
    }
}
