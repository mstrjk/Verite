package teacommontea.veritechasse.vanilla.Tools.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Enchantments.Support.DurabilityModifiers;
import teacommontea.veritechasse.vanilla.Reality;

public final class Durability {

    public static final int COST_PER_BLOCK_MINED = 1;
    public static final int COST_PER_ATTACK = 2;
    public static final int COST_PER_INSTANT_BREAK_BLOCK = 0;

    private Durability() {
    }

    public static int maxDamage(Material material) {
        return material.durability();
    }

    public static int remaining(Material material, int damage) {
        int left = material.durability() - damage;
        return left < 0 ? 0 : left;
    }

    public static boolean broken(Material material, int damage) {
        return damage >= material.durability();
    }

    public static int afterUse(int damage, int cost) {
        return damage + cost;
    }

    public static double expectedUses(Material material, double skipChance) {
        if (skipChance >= 1.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) material.durability() / (1.0D - skipChance);
    }

    public static double expectedUsesFor(ItemStack stack, Material material, Era era) {
        return DurabilityModifiers.expectedUses(stack, material.durability(), era);
    }

    public static double expectedDamagePerUse(ItemStack stack, boolean isArmor, Era era) {
        return DurabilityModifiers.expectedDamagePerUse(stack, isArmor, era);
    }

    public static float skipChance(ItemStack stack, boolean isArmor, Era era) {
        return DurabilityModifiers.skipChance(stack, isArmor, era);
    }

    public static boolean repairIsPossible(ItemStack stack, int observedRepair, int experienceAbsorbed) {
        return DurabilityModifiers.repairIsPossible(stack, observedRepair, experienceAbsorbed);
    }

    public static Reality remainingFrom(Material material, int damage) {
        return Reality.of(remaining(material, damage));
    }
}
