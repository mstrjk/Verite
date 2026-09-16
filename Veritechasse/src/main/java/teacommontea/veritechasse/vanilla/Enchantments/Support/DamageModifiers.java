package teacommontea.veritechasse.Vanilla.Enchantments.Support;

import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Enchantments.BaneOfArthropods;
import teacommontea.veritechasse.Vanilla.Enchantments.Impaling;
import teacommontea.veritechasse.Vanilla.Enchantments.Power;
import teacommontea.veritechasse.Vanilla.Enchantments.Sharpness;
import teacommontea.veritechasse.Vanilla.Enchantments.Smite;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.SensitiveToImpaling;

public final class DamageModifiers {

    private DamageModifiers() {
    }

    public static float meleeBonus(ItemStack stack, EntityType target, Era era) {
        if (stack == null) {
            return 0.0F;
        }

        float bonus = 0.0F;
        bonus = bonus + Sharpness.bonusDamage(Sharpness.levelOn(stack), era);
        bonus = bonus + Smite.bonusDamageAgainst(Smite.levelOn(stack), target, era);
        bonus = bonus + BaneOfArthropods.bonusDamageAgainst(BaneOfArthropods.levelOn(stack), target, era);
        bonus = bonus + Impaling.bonusDamageAgainst(
            Impaling.levelOn(stack), SensitiveToImpaling.contains(target, era), era);
        return bonus;
    }

    public static float modifyDamage(ItemStack stack, EntityType target, float baseDamage, Era era) {
        return baseDamage + meleeBonus(stack, target, era);
    }

    public static float projectileBonus(ItemStack stack, Era era) {
        if (stack == null) {
            return 0.0F;
        }
        return Power.bonusDamage(Power.levelOn(stack), era);
    }

    public static float modifyProjectileDamage(ItemStack stack, float baseDamage, Era era) {
        return baseDamage + projectileBonus(stack, era);
    }
}
