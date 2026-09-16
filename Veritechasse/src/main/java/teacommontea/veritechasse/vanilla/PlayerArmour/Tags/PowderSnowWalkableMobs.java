package teacommontea.veritechasse.Vanilla.PlayerArmour.Tags;

import java.util.Locale;
import java.util.Set;

public final class PowderSnowWalkableMobs {

    public static final String KEY = "powder_snow_walkable_mobs";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
        "endermite",
        "fox",
        "rabbit",
        "silverfish");

    public static final Set<String> LEGACY_TAG = Set.of(
        "endermite",
        "fox",
        "rabbit",
        "silverfish");

    private PowderSnowWalkableMobs() {
    }

    public static boolean contains(String entityName) {
        if (entityName == null) {
            return false;
        }
        return TAG.contains(entityName.toLowerCase(Locale.ROOT));
    }
}
