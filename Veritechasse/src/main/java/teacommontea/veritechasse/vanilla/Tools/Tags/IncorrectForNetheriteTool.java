package teacommontea.veritechasse.Vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class IncorrectForNetheriteTool {

    public static final String KEY = "incorrect_for_netherite_tool";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final Set<String> TAG = Set.of();

    private IncorrectForNetheriteTool() {
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
