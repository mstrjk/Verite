package teacommontea.veritechasse.vanilla.RideableArmour.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Protocol;

public final class CanEquipSaddle {

    public static final String KEY = "can_equip_saddle";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final int TAG_PROTOCOL_MAJOR = 1;
    public static final int TAG_PROTOCOL_MINOR = 21;
    public static final int TAG_PROTOCOL_PATCH = 5;

    public static final int EXPANDED_PROTOCOL_MAJOR = 1;
    public static final int EXPANDED_PROTOCOL_MINOR = 21;
    public static final int EXPANDED_PROTOCOL_PATCH = 11;

    public static final Set<String> TAG = Set.of(
        "camel",
        "camel_husk",
        "donkey",
        "horse",
        "mule",
        "nautilus",
        "pig",
        "skeleton_horse",
        "strider",
        "zombie_horse",
        "zombie_nautilus");

    public static final Set<String> INTRODUCED_TAG = Set.of(
        "camel",
        "donkey",
        "horse",
        "mule",
        "pig",
        "skeleton_horse",
        "strider",
        "zombie_horse");

    public static final Set<String> LEGACY_SADDLEABLE = Set.of(
        "camel",
        "donkey",
        "horse",
        "mule",
        "pig",
        "skeleton_horse",
        "strider",
        "zombie_horse");

    private CanEquipSaddle() {
    }

    public static boolean tagExists(Protocol protocol) {
        return protocol.atLeast(TAG_PROTOCOL_MAJOR, TAG_PROTOCOL_MINOR, TAG_PROTOCOL_PATCH);
    }

    public static boolean expanded(Protocol protocol) {
        return protocol.atLeast(EXPANDED_PROTOCOL_MAJOR, EXPANDED_PROTOCOL_MINOR, EXPANDED_PROTOCOL_PATCH);
    }

    public static final Set<String> REQUIRES_TAMING = Set.of(
        "camel",
        "donkey",
        "horse",
        "mule",
        "nautilus",
        "zombie_nautilus");

    public static final Set<String> IGNORES_SLOT_RESTRICTIONS = Set.of(
        "skeleton_horse",
        "zombie_horse");

    public static boolean requiresTaming(String entityName) {
        if (entityName == null) {
            return false;
        }
        return REQUIRES_TAMING.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static boolean ignoresSlotRestrictions(String entityName) {
        if (entityName == null) {
            return false;
        }
        return IGNORES_SLOT_RESTRICTIONS.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static boolean individualPermits(String entityName, boolean alive, boolean baby, boolean tamed) {
        if (ignoresSlotRestrictions(entityName)) {
            return true;
        }
        if (!alive || baby) {
            return false;
        }
        return !requiresTaming(entityName) || tamed;
    }

    public static boolean contains(String entityName, Protocol protocol) {
        if (entityName == null) {
            return false;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (expanded(protocol)) {
            return TAG.contains(name);
        }
        if (tagExists(protocol)) {
            return INTRODUCED_TAG.contains(name);
        }
        return LEGACY_SADDLEABLE.contains(name);
    }
}
