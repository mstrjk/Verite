package teacommontea.veritechasse.Vanilla.PlayerArmour.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class PiglinSafeArmor {

    public static final String KEY = "piglin_safe_armor";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final int TAG_PROTOCOL_MAJOR = 1;
    public static final int TAG_PROTOCOL_MINOR = 21;
    public static final int TAG_PROTOCOL_PATCH = 3;

    public static final Set<String> TAG = Set.of(
        "golden_boots",
        "golden_chestplate",
        "golden_helmet",
        "golden_leggings");

    public static final Set<String> LEGACY_MATERIAL_KEYS = Set.of("gold");

    private PiglinSafeArmor() {
    }

    public static boolean tagExists(Protocol protocol) {
        return protocol.atLeast(TAG_PROTOCOL_MAJOR, TAG_PROTOCOL_MINOR, TAG_PROTOCOL_PATCH);
    }

    public static boolean contains(String itemName, Protocol protocol) {
        if (itemName == null) {
            return false;
        }
        String name = itemName.toLowerCase(Locale.ROOT);
        if (tagExists(protocol)) {
            return TAG.contains(name);
        }
        return name.startsWith("golden_")
            && (name.endsWith("_helmet")
                || name.endsWith("_chestplate")
                || name.endsWith("_leggings")
                || name.endsWith("_boots"));
    }
}
