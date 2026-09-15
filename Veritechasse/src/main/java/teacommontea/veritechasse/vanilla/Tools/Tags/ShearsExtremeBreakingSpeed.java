package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class ShearsExtremeBreakingSpeed {

    public static final String KEY = "shears_extreme_breaking_speed";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final Set<String> TAG = Set.of(
        "acacia_leaves",
        "azalea_leaves",
        "birch_leaves",
        "cherry_leaves",
        "dark_oak_leaves",
        "flowering_azalea_leaves",
        "jungle_leaves",
        "mangrove_leaves",
        "oak_leaves",
        "pale_oak_leaves",
        "spruce_leaves");

    private ShearsExtremeBreakingSpeed() {
    }

    public static boolean contains(String blockName, ToolEra era) {
        if (blockName == null) {
            return false;
        }
        if (era != ToolEra.MATERIALS_WITH_TAGS) {
            throw new IllegalStateException(
                KEY + " does not exist before 1.21.3; use MiningTier levels instead");
        }
        return TAG.contains(blockName.toLowerCase(Locale.ROOT));
    }
}
