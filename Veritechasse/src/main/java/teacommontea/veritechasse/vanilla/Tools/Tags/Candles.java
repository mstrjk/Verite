package teacommontea.veritechasse.vanilla.Tools.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class Candles {

    public static final String KEY = "candles";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
        "black_candle",
        "blue_candle",
        "brown_candle",
        "candle",
        "cyan_candle",
        "gray_candle",
        "green_candle",
        "light_blue_candle",
        "light_gray_candle",
        "lime_candle",
        "magenta_candle",
        "orange_candle",
        "pink_candle",
        "purple_candle",
        "red_candle",
        "white_candle",
        "yellow_candle");

    public static final Set<String> LEGACY_TAG = Set.of(
        "black_candle",
        "blue_candle",
        "brown_candle",
        "candle",
        "cyan_candle",
        "gray_candle",
        "green_candle",
        "light_blue_candle",
        "light_gray_candle",
        "lime_candle",
        "magenta_candle",
        "orange_candle",
        "pink_candle",
        "purple_candle",
        "red_candle",
        "white_candle",
        "yellow_candle");

    private Candles() {
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
