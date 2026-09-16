package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import java.util.Set;

public final class ExclusiveSetMining {

    public static final String KEY = "exclusive_set/mining";

    public static final Set<String> MEMBERS = Set.of(
        "fortune",
        "silk_touch");

    private ExclusiveSetMining() {
    }

    public static boolean contains(String enchantKey) {
        return enchantKey != null && MEMBERS.contains(enchantKey);
    }
}
