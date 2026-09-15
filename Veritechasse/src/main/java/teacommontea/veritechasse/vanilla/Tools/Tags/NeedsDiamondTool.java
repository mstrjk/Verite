package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class NeedsDiamondTool {

    public static final String KEY = "needs_diamond_tool";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
        "ancient_debris",
        "crying_obsidian",
        "netherite_block",
        "obsidian",
        "respawn_anchor");

    public static final Set<String> LEGACY_TAG = Set.of(
        "ancient_debris",
        "crying_obsidian",
        "netherite_block",
        "obsidian",
        "respawn_anchor");

    private NeedsDiamondTool() {
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
