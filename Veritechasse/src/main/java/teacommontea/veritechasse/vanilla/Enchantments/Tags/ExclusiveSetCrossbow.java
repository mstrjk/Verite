package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import java.util.Set;

public final class ExclusiveSetCrossbow {

    public static final String KEY = "exclusive_set/crossbow";

    public static final Set<String> MEMBERS = Set.of(
        "multishot",
        "piercing");

    private ExclusiveSetCrossbow() {
    }

    public static boolean contains(String enchantKey) {
        return enchantKey != null && MEMBERS.contains(enchantKey);
    }
}
