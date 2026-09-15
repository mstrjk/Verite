package teacommontea.veritechasse.vanilla.RideableArmour.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Protocol;

public final class CanEquipHarness {

    public static final String KEY = "can_equip_harness";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final int TAG_PROTOCOL_MAJOR = 1;
    public static final int TAG_PROTOCOL_MINOR = 21;
    public static final int TAG_PROTOCOL_PATCH = 6;

    public static final Set<String> TAG = Set.of("happy_ghast");

    private CanEquipHarness() {
    }

    public static boolean tagExists(Protocol protocol) {
        return protocol.atLeast(TAG_PROTOCOL_MAJOR, TAG_PROTOCOL_MINOR, TAG_PROTOCOL_PATCH);
    }

    public static boolean contains(String entityName, Protocol protocol) {
        if (entityName == null || !tagExists(protocol)) {
            return false;
        }
        return TAG.contains(entityName.toLowerCase(Locale.ROOT));
    }
}
