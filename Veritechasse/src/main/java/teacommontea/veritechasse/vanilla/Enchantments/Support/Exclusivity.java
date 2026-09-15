package teacommontea.veritechasse.vanilla.Enchantments.Support;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetArmor;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetBoots;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetBow;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetCrossbow;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetDamage;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetMining;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.ExclusiveSetRiptide;

public final class Exclusivity {

    private static final List<Set<String>> GROUPS = List.of(
        ExclusiveSetArmor.MEMBERS,
        ExclusiveSetBoots.MEMBERS,
        ExclusiveSetBow.MEMBERS,
        ExclusiveSetCrossbow.MEMBERS,
        ExclusiveSetDamage.MEMBERS,
        ExclusiveSetMining.MEMBERS,
        ExclusiveSetRiptide.GROUP);

    private Exclusivity() {
    }

    public static String keyOf(Enchantment enchantment) {
        if (enchantment == null) {
            return "";
        }
        return enchantment.getKey().getKey().toLowerCase(Locale.ROOT);
    }

    public static boolean conflict(String first, String second) {
        if (first == null || second == null || first.equals(second)) {
            return false;
        }
        for (Set<String> group : GROUPS) {
            if (group.contains(first) && group.contains(second)) {
                return true;
            }
        }
        return false;
    }

    public static boolean conflict(Enchantment first, Enchantment second) {
        return conflict(keyOf(first), keyOf(second));
    }

    public static boolean combinationIsLegal(ItemStack stack) {
        if (stack == null) {
            return true;
        }

        List<Enchantment> present = List.copyOf(stack.getEnchantments().keySet());
        for (int i = 0; i < present.size(); i++) {
            for (int j = i + 1; j < present.size(); j++) {
                if (conflict(present.get(i), present.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }
}
