package teacommontea.veritechasse.Vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class NeedsIronTool {

    public static final String KEY = "needs_iron_tool";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
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
        "raw_gold_block",
        "redstone_ore");

    public static final Set<String> LEGACY_TAG = Set.of(
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
        "raw_gold_block",
        "redstone_ore");

    private NeedsIronTool() {
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
