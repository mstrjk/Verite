package teacommontea.veritechasse.Vanilla.Potions.Tags;

import java.util.Locale;
import java.util.Set;

public final class BypassesEffects {

    public static final String KEY = "bypasses_effects";

    public static final Set<String> TAG = Set.of(
        "starve");

    private BypassesEffects() {
    }

    public static boolean contains(String damageType) {
        if (damageType == null) {
            return false;
        }
        return TAG.contains(damageType.toLowerCase(Locale.ROOT));
    }
}
