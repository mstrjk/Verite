package teacommontea.veritechasse.vanilla.Tools.Axes.Support;

import java.util.Locale;
import java.util.Map;

import teacommontea.veritechasse.vanilla.Protocol;

public final class Strippables {

    public static final Map<String, String> MAP = Map.ofEntries(
        Map.entry("oak_wood", "stripped_oak_wood"),
        Map.entry("oak_log", "stripped_oak_log"),
        Map.entry("dark_oak_wood", "stripped_dark_oak_wood"),
        Map.entry("dark_oak_log", "stripped_dark_oak_log"),
        Map.entry("pale_oak_wood", "stripped_pale_oak_wood"),
        Map.entry("pale_oak_log", "stripped_pale_oak_log"),
        Map.entry("acacia_wood", "stripped_acacia_wood"),
        Map.entry("acacia_log", "stripped_acacia_log"),
        Map.entry("cherry_wood", "stripped_cherry_wood"),
        Map.entry("cherry_log", "stripped_cherry_log"),
        Map.entry("birch_wood", "stripped_birch_wood"),
        Map.entry("birch_log", "stripped_birch_log"),
        Map.entry("jungle_wood", "stripped_jungle_wood"),
        Map.entry("jungle_log", "stripped_jungle_log"),
        Map.entry("spruce_wood", "stripped_spruce_wood"),
        Map.entry("spruce_log", "stripped_spruce_log"),
        Map.entry("warped_stem", "stripped_warped_stem"),
        Map.entry("warped_hyphae", "stripped_warped_hyphae"),
        Map.entry("crimson_stem", "stripped_crimson_stem"),
        Map.entry("crimson_hyphae", "stripped_crimson_hyphae"),
        Map.entry("mangrove_wood", "stripped_mangrove_wood"),
        Map.entry("mangrove_log", "stripped_mangrove_log"),
        Map.entry("bamboo_block", "stripped_bamboo_block"));

    public static final Map<String, String> LEGACY_MAP = Map.ofEntries(
        Map.entry("oak_wood", "stripped_oak_wood"),
        Map.entry("oak_log", "stripped_oak_log"),
        Map.entry("dark_oak_wood", "stripped_dark_oak_wood"),
        Map.entry("dark_oak_log", "stripped_dark_oak_log"),
        Map.entry("acacia_wood", "stripped_acacia_wood"),
        Map.entry("acacia_log", "stripped_acacia_log"),
        Map.entry("birch_wood", "stripped_birch_wood"),
        Map.entry("birch_log", "stripped_birch_log"),
        Map.entry("jungle_wood", "stripped_jungle_wood"),
        Map.entry("jungle_log", "stripped_jungle_log"),
        Map.entry("spruce_wood", "stripped_spruce_wood"),
        Map.entry("spruce_log", "stripped_spruce_log"),
        Map.entry("warped_stem", "stripped_warped_stem"),
        Map.entry("warped_hyphae", "stripped_warped_hyphae"),
        Map.entry("crimson_stem", "stripped_crimson_stem"),
        Map.entry("crimson_hyphae", "stripped_crimson_hyphae"),
        Map.entry("mangrove_wood", "stripped_mangrove_wood"),
        Map.entry("mangrove_log", "stripped_mangrove_log"));

    public static final Map<String, String> BAMBOO_AND_CHERRY_MAP = Map.ofEntries(
        Map.entry("oak_wood", "stripped_oak_wood"),
        Map.entry("oak_log", "stripped_oak_log"),
        Map.entry("dark_oak_wood", "stripped_dark_oak_wood"),
        Map.entry("dark_oak_log", "stripped_dark_oak_log"),
        Map.entry("acacia_wood", "stripped_acacia_wood"),
        Map.entry("acacia_log", "stripped_acacia_log"),
        Map.entry("cherry_wood", "stripped_cherry_wood"),
        Map.entry("cherry_log", "stripped_cherry_log"),
        Map.entry("birch_wood", "stripped_birch_wood"),
        Map.entry("birch_log", "stripped_birch_log"),
        Map.entry("jungle_wood", "stripped_jungle_wood"),
        Map.entry("jungle_log", "stripped_jungle_log"),
        Map.entry("spruce_wood", "stripped_spruce_wood"),
        Map.entry("spruce_log", "stripped_spruce_log"),
        Map.entry("warped_stem", "stripped_warped_stem"),
        Map.entry("warped_hyphae", "stripped_warped_hyphae"),
        Map.entry("crimson_stem", "stripped_crimson_stem"),
        Map.entry("crimson_hyphae", "stripped_crimson_hyphae"),
        Map.entry("mangrove_wood", "stripped_mangrove_wood"),
        Map.entry("mangrove_log", "stripped_mangrove_log"),
        Map.entry("bamboo_block", "stripped_bamboo_block"));

    private Strippables() {
    }

    private static Map<String, String> mapFor(Protocol protocol) {
        if (protocol.atLeast(1, 21, 2)) {
            return MAP;
        }
        if (protocol.atLeast(1, 19, 4)) {
            return BAMBOO_AND_CHERRY_MAP;
        }
        return LEGACY_MAP;
    }

    public static boolean strippable(String blockName, Protocol protocol) {
        return strippedForm(blockName, protocol) != null;
    }

    public static String strippedForm(String blockName, Protocol protocol) {
        if (blockName == null) {
            return null;
        }
        return mapFor(protocol).get(blockName.toLowerCase(Locale.ROOT));
    }
}
