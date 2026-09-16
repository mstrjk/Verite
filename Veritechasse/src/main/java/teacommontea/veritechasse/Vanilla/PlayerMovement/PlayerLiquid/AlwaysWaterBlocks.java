package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class AlwaysWaterBlocks {

    public static final String KEY = "always_water_blocks";

    public static final Set<String> NAMES = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "water",
            "bubble_column",
            "kelp",
            "kelp_plant",
            "seagrass",
            "tall_seagrass")));

    private AlwaysWaterBlocks() {
    }

    public static String normalise(String blockName) {
        if (blockName == null) {
            return "";
        }
        String trimmed = blockName.toLowerCase(Locale.ROOT).trim();
        int colon = trimmed.indexOf(':');
        return colon < 0 ? trimmed : trimmed.substring(colon + 1);
    }

    public static boolean contains(String blockName) {
        return NAMES.contains(normalise(blockName));
    }

    public static boolean isFullSource(String blockName) {
        return contains(blockName);
    }

    public static boolean hasWaterloggedProperty(String blockName) {
        return false;
    }
}
