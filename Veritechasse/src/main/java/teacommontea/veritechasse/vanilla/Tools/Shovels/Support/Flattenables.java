package teacommontea.veritechasse.Vanilla.Tools.Shovels.Support;

import java.util.Locale;
import java.util.Set;

public final class Flattenables {

    public static final String RESULT = "dirt_path";

    public static final Set<String> BLOCKS = Set.of(
        "grass_block",
        "dirt",
        "podzol",
        "coarse_dirt",
        "mycelium",
        "rooted_dirt");

    private Flattenables() {
    }

    public static boolean flattenable(String blockName) {
        if (blockName == null) {
            return false;
        }
        return BLOCKS.contains(blockName.toLowerCase(Locale.ROOT));
    }

    public static String flattenedForm(String blockName) {
        return flattenable(blockName) ? RESULT : null;
    }

    public static boolean flattens(String blockName, boolean airAbove, boolean clickedBottomFace) {
        if (clickedBottomFace) {
            return false;
        }
        if (!airAbove) {
            return false;
        }
        return flattenable(blockName);
    }
}
