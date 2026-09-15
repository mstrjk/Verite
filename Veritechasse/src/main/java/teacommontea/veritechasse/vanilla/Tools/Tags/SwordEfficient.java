package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class SwordEfficient {

    public static final String KEY = "sword_efficient";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final Set<String> TAG = Set.of(
        "acacia_leaves",
        "azalea_leaves",
        "big_dripleaf",
        "big_dripleaf_stem",
        "birch_leaves",
        "carved_pumpkin",
        "cherry_leaves",
        "chorus_flower",
        "chorus_plant",
        "cocoa",
        "dark_oak_leaves",
        "flowering_azalea_leaves",
        "glow_lichen",
        "jack_o_lantern",
        "jungle_leaves",
        "mangrove_leaves",
        "melon",
        "oak_leaves",
        "pale_oak_leaves",
        "pumpkin",
        "spruce_leaves",
        "vine");

    private SwordEfficient() {
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
