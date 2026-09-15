package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class CandleCakes {

    public static final String KEY = "candle_cakes";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
        "black_candle_cake",
        "blue_candle_cake",
        "brown_candle_cake",
        "candle_cake",
        "cyan_candle_cake",
        "gray_candle_cake",
        "green_candle_cake",
        "light_blue_candle_cake",
        "light_gray_candle_cake",
        "lime_candle_cake",
        "magenta_candle_cake",
        "orange_candle_cake",
        "pink_candle_cake",
        "purple_candle_cake",
        "red_candle_cake",
        "white_candle_cake",
        "yellow_candle_cake");

    public static final Set<String> LEGACY_TAG = Set.of(
        "black_candle_cake",
        "blue_candle_cake",
        "brown_candle_cake",
        "candle_cake",
        "cyan_candle_cake",
        "gray_candle_cake",
        "green_candle_cake",
        "light_blue_candle_cake",
        "light_gray_candle_cake",
        "lime_candle_cake",
        "magenta_candle_cake",
        "orange_candle_cake",
        "pink_candle_cake",
        "purple_candle_cake",
        "red_candle_cake",
        "white_candle_cake",
        "yellow_candle_cake");

    private CandleCakes() {
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
