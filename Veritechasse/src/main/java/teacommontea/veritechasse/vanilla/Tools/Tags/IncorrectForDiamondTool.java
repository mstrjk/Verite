package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class IncorrectForDiamondTool {

    public static final String KEY = "incorrect_for_diamond_tool";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final Set<String> TAG = Set.of();

    private IncorrectForDiamondTool() {
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
