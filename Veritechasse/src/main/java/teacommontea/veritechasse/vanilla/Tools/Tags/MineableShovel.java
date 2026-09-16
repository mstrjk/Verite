package teacommontea.veritechasse.Vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class MineableShovel {

    public static final String KEY = "mineable/shovel";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
        "black_concrete_powder",
        "blue_concrete_powder",
        "brown_concrete_powder",
        "clay",
        "coarse_dirt",
        "cyan_concrete_powder",
        "dirt",
        "dirt_path",
        "farmland",
        "grass_block",
        "gravel",
        "gray_concrete_powder",
        "green_concrete_powder",
        "light_blue_concrete_powder",
        "light_gray_concrete_powder",
        "lime_concrete_powder",
        "magenta_concrete_powder",
        "mud",
        "muddy_mangrove_roots",
        "mycelium",
        "orange_concrete_powder",
        "pink_concrete_powder",
        "podzol",
        "purple_concrete_powder",
        "red_concrete_powder",
        "red_sand",
        "rooted_dirt",
        "sand",
        "snow",
        "snow_block",
        "soul_sand",
        "soul_soil",
        "suspicious_gravel",
        "suspicious_sand",
        "white_concrete_powder",
        "yellow_concrete_powder");

    public static final Set<String> LEGACY_TAG = Set.of(
        "black_concrete_powder",
        "blue_concrete_powder",
        "brown_concrete_powder",
        "clay",
        "coarse_dirt",
        "cyan_concrete_powder",
        "dirt",
        "dirt_path",
        "farmland",
        "grass_block",
        "gravel",
        "gray_concrete_powder",
        "green_concrete_powder",
        "light_blue_concrete_powder",
        "light_gray_concrete_powder",
        "lime_concrete_powder",
        "magenta_concrete_powder",
        "mud",
        "muddy_mangrove_roots",
        "mycelium",
        "orange_concrete_powder",
        "pink_concrete_powder",
        "podzol",
        "purple_concrete_powder",
        "red_concrete_powder",
        "red_sand",
        "rooted_dirt",
        "sand",
        "snow",
        "snow_block",
        "soul_sand",
        "soul_soil",
        "white_concrete_powder",
        "yellow_concrete_powder");

    private MineableShovel() {
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
