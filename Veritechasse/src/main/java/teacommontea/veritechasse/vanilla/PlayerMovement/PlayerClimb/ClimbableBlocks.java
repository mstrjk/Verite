package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerClimb;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Protocol;

public final class ClimbableBlocks {

    public static final String TAG = "minecraft:climbable";
    public static final String LEGACY_TAG = "minecraft:climbable";
    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final String LADDER = "ladder";
    public static final String VINE = "vine";
    public static final String SCAFFOLDING = "scaffolding";
    public static final String WEEPING_VINES = "weeping_vines";
    public static final String WEEPING_VINES_PLANT = "weeping_vines_plant";
    public static final String TWISTING_VINES = "twisting_vines";
    public static final String TWISTING_VINES_PLANT = "twisting_vines_plant";
    public static final String CAVE_VINES = "cave_vines";
    public static final String CAVE_VINES_PLANT = "cave_vines_plant";

    public static final String TRAPDOOR_SUFFIX = "_trapdoor";

    public static final int TAG_DIRECTORY_RENAME_PROTOCOL_MAJOR = 1;
    public static final int TAG_DIRECTORY_RENAME_PROTOCOL_MINOR = 21;
    public static final int TAG_DIRECTORY_RENAME_PROTOCOL_PATCH = 0;

    private static final Set<String> MEMBERS = Set.of(
        LADDER,
        VINE,
        SCAFFOLDING,
        WEEPING_VINES,
        WEEPING_VINES_PLANT,
        TWISTING_VINES,
        TWISTING_VINES_PLANT,
        CAVE_VINES,
        CAVE_VINES_PLANT);

    public static final String CAN_GLIDE_THROUGH_TAG = "can_glide_through";

    public static final int GLIDE_THROUGH_MAJOR = 1;
    public static final int GLIDE_THROUGH_MINOR = 21;
    public static final int GLIDE_THROUGH_PATCH = 11;

    public static final Set<String> CAN_GLIDE_THROUGH = Set.of(
        "vine",
        "twisting_vines",
        "twisting_vines_plant",
        "weeping_vines",
        "weeping_vines_plant",
        "cave_vines",
        "cave_vines_plant");

    private ClimbableBlocks() {
    }

    public static boolean glideThroughTagExists(Protocol protocol) {
        return protocol.atLeast(
            GLIDE_THROUGH_MAJOR, GLIDE_THROUGH_MINOR, GLIDE_THROUGH_PATCH);
    }

    public static boolean allowsGlideThrough(String blockName, Protocol protocol) {
        if (blockName == null || !glideThroughTagExists(protocol)) {
            return false;
        }
        return CAN_GLIDE_THROUGH.contains(blockName.toLowerCase(Locale.ROOT));
    }

    public static String normalise(String blockName) {
        if (blockName == null) {
            return null;
        }
        String lower = blockName.toLowerCase(Locale.ROOT);
        int colon = lower.indexOf(':');
        return colon < 0 ? lower : lower.substring(colon + 1);
    }

    public static boolean contains(String blockName) {
        String key = normalise(blockName);
        return key != null && MEMBERS.contains(key);
    }

    public static boolean isTrapdoor(String blockName) {
        String key = normalise(blockName);
        return key != null && key.endsWith(TRAPDOOR_SUFFIX);
    }

    public static boolean isScaffolding(String blockName) {
        return SCAFFOLDING.equals(normalise(blockName));
    }

    public static boolean isLadder(String blockName) {
        return LADDER.equals(normalise(blockName));
    }

    public static int size() {
        return MEMBERS.size();
    }

    public static Set<String> members() {
        return MEMBERS;
    }

    public static boolean tagDirectoryIsSingular(Protocol protocol) {
        return protocol.atLeast(
            TAG_DIRECTORY_RENAME_PROTOCOL_MAJOR,
            TAG_DIRECTORY_RENAME_PROTOCOL_MINOR,
            TAG_DIRECTORY_RENAME_PROTOCOL_PATCH);
    }
}
