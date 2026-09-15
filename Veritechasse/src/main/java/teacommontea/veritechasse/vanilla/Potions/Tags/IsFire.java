package teacommontea.veritechasse.vanilla.Potions.Tags;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Potions.Support.EffectEra;

public final class IsFire {

    public static final String KEY = "is_fire";

    public static final Set<String> TAG = Set.of(
        "in_fire",
        "campfire",
        "on_fire",
        "lava",
        "hot_floor",
        "sulfur_cube_hot",
        "unattributed_fireball",
        "fireball");

    public static final Set<String> LEGACY_TAG = Set.of(
        "in_fire",
        "on_fire",
        "lava",
        "hot_floor",
        "fireball");

    private IsFire() {
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
