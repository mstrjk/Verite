package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import java.util.Set;

public final class ExclusiveSetBoots {

    public static final String KEY = "exclusive_set/boots";

    public static final Set<String> MEMBERS = Set.of(
        "frost_walker",
        "depth_strider");

    private ExclusiveSetBoots() {
    }

    public static boolean contains(String enchantKey) {
        return enchantKey != null && MEMBERS.contains(enchantKey);
    }
}
