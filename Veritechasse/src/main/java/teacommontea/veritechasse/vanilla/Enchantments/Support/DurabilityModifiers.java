package teacommontea.veritechasse.Vanilla.Enchantments.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Enchantments.Mending;
import teacommontea.veritechasse.Vanilla.Enchantments.Unbreaking;

public final class DurabilityModifiers {

    private DurabilityModifiers() {
    }

    public static float skipChance(ItemStack stack, boolean isArmor, Era era) {
        if (stack == null) {
            return 0.0F;
        }
        int level = Unbreaking.levelOn(stack);
        return isArmor ? Unbreaking.armorSkipChance(level, era) : Unbreaking.skipChance(level, era);
    }

    public static double expectedDamagePerUse(ItemStack stack, boolean isArmor, Era era) {
        if (stack == null) {
            return 1.0D;
        }
        int level = Unbreaking.levelOn(stack);
        return isArmor
            ? Unbreaking.expectedArmorDurabilityPerUse(level, era)
            : Unbreaking.expectedDurabilityPerUse(level, era);
    }

    public static double expectedUses(ItemStack stack, int maxDurability, boolean isArmor, Era era) {
        if (stack == null) {
            return (double) maxDurability;
        }
        return Unbreaking.expectedUsesFor(maxDurability, Unbreaking.levelOn(stack), isArmor, era);
    }

    public static double expectedUses(ItemStack stack, int maxDurability, Era era) {
        return expectedUses(stack, maxDurability, false, era);
    }

    public static boolean hasMending(ItemStack stack) {
        return stack != null && Mending.levelOn(stack) > 0;
    }

    public static int repairFromExperience(ItemStack stack, int experience) {
        if (!hasMending(stack)) {
            return 0;
        }
        return Mending.repairFrom(experience);
    }

    public static boolean repairIsPossible(ItemStack stack, int observedRepair, int experienceAbsorbed) {
        if (!hasMending(stack)) {
            return observedRepair <= 0;
        }
        return Mending.repairIsPossible(observedRepair, experienceAbsorbed);
    }
}
