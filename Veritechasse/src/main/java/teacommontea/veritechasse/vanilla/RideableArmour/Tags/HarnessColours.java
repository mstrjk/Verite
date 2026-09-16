package teacommontea.veritechasse.Vanilla.RideableArmour.Tags;

import java.util.Locale;
import java.util.Set;

public final class HarnessColours {

    public static final String SUFFIX = "_harness";

    public static final boolean EXISTS_IN_LEGACY_ERA = false;

    public static final Set<String> TAG = Set.of(
        "black_harness",
        "blue_harness",
        "brown_harness",
        "cyan_harness",
        "gray_harness",
        "green_harness",
        "light_blue_harness",
        "light_gray_harness",
        "lime_harness",
        "magenta_harness",
        "orange_harness",
        "pink_harness",
        "purple_harness",
        "red_harness",
        "white_harness",
        "yellow_harness");

    private HarnessColours() {
    }

    public static boolean contains(String itemName) {
        if (itemName == null) {
            return false;
        }
        return TAG.contains(itemName.toLowerCase(Locale.ROOT));
    }

    public static int count() {
        return TAG.size();
    }
}
