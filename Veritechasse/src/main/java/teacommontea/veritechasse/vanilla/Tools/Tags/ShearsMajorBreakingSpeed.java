package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class ShearsMajorBreakingSpeed {

    public static final String KEY = "shears_major_breaking_speed";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final Set<String> TAG = Set.of(
        "black_wool",
        "blue_wool",
        "brown_wool",
        "cyan_wool",
        "gray_wool",
        "green_wool",
        "light_blue_wool",
        "light_gray_wool",
        "lime_wool",
        "magenta_wool",
        "orange_wool",
        "pink_wool",
        "purple_wool",
        "red_wool",
        "white_wool",
        "yellow_wool");

    private ShearsMajorBreakingSpeed() {
    }

    public static boolean contains(String blockName, ToolEra era) {
        if (blockName == null) {
            return false;
        }
        if (era != ToolEra.MATERIALS_WITH_TAGS) {
            throw new IllegalStateException(
                KEY + " does not exist before 1.20.5; use MiningTier levels instead");
        }
        return TAG.contains(blockName.toLowerCase(Locale.ROOT));
    }
}
