package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import java.util.Set;

public final class ExclusiveSetArmor {

    public static final String KEY = "exclusive_set/armor";

    public static final Set<String> MEMBERS = Set.of(
        "protection",
        "blast_protection",
        "fire_protection",
        "projectile_protection");

    private ExclusiveSetArmor() {
    }

    public static boolean contains(String enchantKey) {
        return enchantKey != null && MEMBERS.contains(enchantKey);
    }
}
