package teacommontea.veritechasse.vanilla.Enchantments.Support;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public final class Registry {

    private static final Map<String, Enchantment> RESOLVED = new ConcurrentHashMap<>();
    private static final Set<String> UNRESOLVED = ConcurrentHashMap.newKeySet();

    private Registry() {
    }

    public static Enchantment byKey(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        Enchantment cached = RESOLVED.get(key);
        if (cached != null) {
            return cached;
        }

        Enchantment found = Enchantment.getByKey(NamespacedKey.minecraft(key));
        if (found == null) {
            UNRESOLVED.add(key);
            return null;
        }

        RESOLVED.put(key, found);
        return found;
    }

    public static boolean isResolvable(String key) {
        return byKey(key) != null;
    }

    public static Set<String> unresolved() {
        return Set.copyOf(UNRESOLVED);
    }

    public static int levelOn(ItemStack stack, String key) {
        if (stack == null) {
            return 0;
        }

        Enchantment enchantment = byKey(key);
        if (enchantment == null) {
            return 0;
        }
        return stack.getEnchantmentLevel(enchantment);
    }
}
