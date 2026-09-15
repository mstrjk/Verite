package teacommontea.veritechasse.vanilla.PlayerArmour.Tags;

import java.util.Locale;
import java.util.Set;

public final class FreezeImmuneWearables {

    public static final String KEY = "freeze_immune_wearables";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
        "leather_boots",
        "leather_chestplate",
        "leather_helmet",
        "leather_horse_armor",
        "leather_leggings");

    public static final Set<String> LEGACY_TAG = Set.of(
        "leather_boots",
        "leather_chestplate",
        "leather_helmet",
        "leather_horse_armor",
        "leather_leggings");

    private FreezeImmuneWearables() {
    }

    public static boolean contains(String itemName) {
        if (itemName == null) {
            return false;
        }
        return TAG.contains(itemName.toLowerCase(Locale.ROOT));
    }
}
