package teacommontea.veritechasse.vanilla.Potions.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Potions.Support.EffectEra;

public final class BypassesResistance {

    public static final String KEY = "bypasses_resistance";

    public static final Set<String> TAG = Set.of(
        "out_of_world",
        "generic_kill");

    public static final Set<String> LEGACY_TAG = Set.of(
        "out_of_world");

    private BypassesResistance() {
    }

    public static boolean contains(String damageType, EffectEra era) {
        if (damageType == null) {
            return false;
        }
        String name = damageType.toLowerCase(Locale.ROOT);
        if (era == EffectEra.EFFECTS_AS_MODIFIERS) {
            return TAG.contains(name);
        }
        return LEGACY_TAG.contains(name);
    }
}
