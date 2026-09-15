package teacommontea.veritechasse.vanilla.RideableArmour.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Protocol;

public final class CanWearNautilusArmour {

    public static final String KEY = "can_wear_nautilus_armor";

    public static final int PROTOCOL_MAJOR = 1;
    public static final int PROTOCOL_MINOR = 21;
    public static final int PROTOCOL_PATCH = 11;

    public static final Set<String> TAG = Set.of(
        "nautilus",
        "zombie_nautilus");

    private CanWearNautilusArmour() {
    }

    public static boolean tagExists(Protocol protocol) {
        return protocol.atLeast(PROTOCOL_MAJOR, PROTOCOL_MINOR, PROTOCOL_PATCH);
    }

    public static boolean contains(String entityName, Protocol protocol) {
        if (entityName == null || !tagExists(protocol)) {
            return false;
        }
        return TAG.contains(entityName.toLowerCase(Locale.ROOT));
    }
}
