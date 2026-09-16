package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import java.util.Set;

public final class ExclusiveSetDamage {

    public static final String KEY = "exclusive_set/damage";

    public static final Set<String> MEMBERS = Set.of(
        "sharpness",
        "smite",
        "bane_of_arthropods",
        "impaling",
        "density",
        "breach");

    private ExclusiveSetDamage() {
    }

    public static boolean contains(String enchantKey) {
        return enchantKey != null && MEMBERS.contains(enchantKey);
    }
}
