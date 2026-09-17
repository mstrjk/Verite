package teacommontea.veritechasse.Vanilla.ControllableEntities.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class DismountsUnderwater {

    public static final String KEY = "dismounts_underwater";

    public static final int TAG_MAJOR = 1;
    public static final int TAG_MINOR = 19;
    public static final int TAG_PATCH = 4;

    public static final int HAPPY_GHAST_MAJOR = 1;
    public static final int HAPPY_GHAST_MINOR = 21;
    public static final int HAPPY_GHAST_PATCH = 6;

    public static final String SKELETON_HORSE = "skeleton_horse";

    public static final String HAPPY_GHAST = "happy_ghast";

    public static final Set<String> TAG = Set.of(
        "camel",
        "chicken",
        "donkey",
        "happy_ghast",
        "horse",
        "llama",
        "mule",
        "pig",
        "ravager",
        "spider",
        "strider",
        "trader_llama",
        "zombie_horse");

    public static final Set<String> TAG_BEFORE_HAPPY_GHAST = Set.of(
        "camel",
        "chicken",
        "donkey",
        "horse",
        "llama",
        "mule",
        "pig",
        "ravager",
        "spider",
        "strider",
        "trader_llama",
        "zombie_horse");

    private DismountsUnderwater() {
    }

    public static boolean tagExists(Protocol protocol) {
        return protocol.atLeast(TAG_MAJOR, TAG_MINOR, TAG_PATCH);
    }

    public static boolean happyGhastIsMember(Protocol protocol) {
        return protocol.atLeast(HAPPY_GHAST_MAJOR, HAPPY_GHAST_MINOR, HAPPY_GHAST_PATCH);
    }

    public static Set<String> members(Protocol protocol) {
        return happyGhastIsMember(protocol) ? TAG : TAG_BEFORE_HAPPY_GHAST;
    }

    public static boolean contains(String entityName, Protocol protocol) {
        if (entityName == null) {
            return false;
        }
        return members(protocol).contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static boolean rideableUnderWater(String entityName, boolean living) {
        if (entityName != null
                && SKELETON_HORSE.equals(entityName.toLowerCase(Locale.ROOT))) {
            return true;
        }
        return !living;
    }

    public static boolean dismounts(String entityName, boolean living, Protocol protocol) {
        if (tagExists(protocol)) {
            return contains(entityName, protocol);
        }
        return !rideableUnderWater(entityName, living);
    }
}
