package teacommontea.veritechasse.Vanilla.Tools.Hoes.Support;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class Tillables {

    public static final String FARMLAND = "farmland";
    public static final String DIRT = "dirt";
    public static final String HANGING_ROOTS = "hanging_roots";

    public static final Set<String> REQUIRES_AIR_ABOVE = Set.of(
        "grass_block",
        "dirt_path",
        "dirt",
        "coarse_dirt");

    public static final Set<String> IGNORES_AIR_ABOVE = Set.of(
        "rooted_dirt");

    public static final Map<String, String> RESULT = Map.of(
        "grass_block", FARMLAND,
        "dirt_path", FARMLAND,
        "dirt", FARMLAND,
        "coarse_dirt", DIRT,
        "rooted_dirt", DIRT);

    public static final Map<String, String> DROPS = Map.of(
        "rooted_dirt", HANGING_ROOTS);

    private Tillables() {
    }

    public static boolean tillable(String blockName) {
        if (blockName == null) {
            return false;
        }
        return RESULT.containsKey(blockName.toLowerCase(Locale.ROOT));
    }

    public static boolean requiresAirAbove(String blockName) {
        if (blockName == null) {
            return false;
        }
        return REQUIRES_AIR_ABOVE.contains(blockName.toLowerCase(Locale.ROOT));
    }

    public static boolean tills(String blockName, boolean airAbove, boolean clickedBottomFace) {
        if (!tillable(blockName)) {
            return false;
        }
        if (!requiresAirAbove(blockName)) {
            return true;
        }
        return !clickedBottomFace && airAbove;
    }

    public static String tilledForm(String blockName) {
        if (blockName == null) {
            return null;
        }
        return RESULT.get(blockName.toLowerCase(Locale.ROOT));
    }

    public static String droppedItem(String blockName) {
        if (blockName == null) {
            return null;
        }
        return DROPS.get(blockName.toLowerCase(Locale.ROOT));
    }
}
