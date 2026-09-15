package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class MineableHoe {

    public static final String KEY = "mineable/hoe";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
        "acacia_leaves",
        "azalea_leaves",
        "birch_leaves",
        "calibrated_sculk_sensor",
        "cherry_leaves",
        "dark_oak_leaves",
        "dried_kelp_block",
        "flowering_azalea_leaves",
        "hay_block",
        "jungle_leaves",
        "mangrove_leaves",
        "moss_block",
        "moss_carpet",
        "nether_wart_block",
        "oak_leaves",
        "pale_moss_block",
        "pale_moss_carpet",
        "pale_oak_leaves",
        "sculk",
        "sculk_catalyst",
        "sculk_sensor",
        "sculk_shrieker",
        "sculk_vein",
        "shroomlight",
        "sponge",
        "spruce_leaves",
        "target",
        "warped_wart_block",
        "wet_sponge");

    public static final Set<String> LEGACY_TAG = Set.of(
        "acacia_leaves",
        "azalea_leaves",
        "birch_leaves",
        "dark_oak_leaves",
        "dried_kelp_block",
        "flowering_azalea_leaves",
        "hay_block",
        "jungle_leaves",
        "mangrove_leaves",
        "moss_block",
        "moss_carpet",
        "nether_wart_block",
        "oak_leaves",
        "sculk",
        "sculk_catalyst",
        "sculk_sensor",
        "sculk_shrieker",
        "sculk_vein",
        "shroomlight",
        "sponge",
        "spruce_leaves",
        "target",
        "warped_wart_block",
        "wet_sponge");

    private MineableHoe() {
    }

    public static boolean contains(String blockName, ToolEra era) {
        if (blockName == null) {
            return false;
        }
        String name = blockName.toLowerCase(Locale.ROOT);
        if (era == ToolEra.MATERIALS_WITH_TAGS) {
            return TAG.contains(name);
        }
        return LEGACY_TAG.contains(name);
    }
}
