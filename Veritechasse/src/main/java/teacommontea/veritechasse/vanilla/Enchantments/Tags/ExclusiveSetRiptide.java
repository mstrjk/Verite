package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import java.util.Set;

public final class ExclusiveSetRiptide {

    public static final String KEY = "exclusive_set/riptide";

    public static final Set<String> MEMBERS = Set.of(
        "loyalty",
        "channeling");

    public static final Set<String> GROUP = Set.of(
        "riptide",
        "loyalty",
        "channeling");

    private ExclusiveSetRiptide() {
    }

    public static boolean contains(String enchantKey) {
        return enchantKey != null && GROUP.contains(enchantKey);
    }
}
