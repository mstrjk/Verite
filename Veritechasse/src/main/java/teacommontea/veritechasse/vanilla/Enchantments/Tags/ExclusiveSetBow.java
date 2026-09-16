package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import java.util.Set;

public final class ExclusiveSetBow {

    public static final String KEY = "exclusive_set/bow";

    public static final Set<String> MEMBERS = Set.of(
        "infinity",
        "mending");

    private ExclusiveSetBow() {
    }

    public static boolean contains(String enchantKey) {
        return enchantKey != null && MEMBERS.contains(enchantKey);
    }
}
