package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class IncorrectForStoneTool {

    public static final String KEY = "incorrect_for_stone_tool";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final Set<String> TAG = Set.of(
        "ancient_debris",
        "crying_obsidian",
        "deepslate_diamond_ore",
        "deepslate_emerald_ore",
        "deepslate_gold_ore",
        "deepslate_redstone_ore",
        "diamond_block",
        "diamond_ore",
        "emerald_block",
        "emerald_ore",
        "gold_block",
        "gold_ore",
        "netherite_block",
        "obsidian",
        "raw_gold_block",
        "redstone_ore",
        "respawn_anchor");

    private IncorrectForStoneTool() {
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
