package teacommontea.veritechasse.vanilla.PlayerArmour.Tags;

import java.util.Locale;
import java.util.Set;

public final class FreezeHurtsExtraTypes {

    public static final String KEY = "freeze_hurts_extra_types";

    public static final boolean EXISTS_IN_LEGACY_ERA = true;

    public static final Set<String> TAG = Set.of(
        "blaze",
        "magma_cube",
        "strider");

    public static final Set<String> LEGACY_TAG = Set.of(
        "blaze",
        "magma_cube",
        "strider");

    private FreezeHurtsExtraTypes() {
    }

    public static boolean contains(String entityName) {
        if (entityName == null) {
            return false;
        }
        return TAG.contains(entityName.toLowerCase(Locale.ROOT));
    }
}
