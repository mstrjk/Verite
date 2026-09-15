package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import org.bukkit.entity.EntityType;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Enchantments.Tags.Types.EntityTag;

public final class SensitiveToImpaling {

    public static final String KEY = "sensitive_to_impaling";

    public static final EntityTag TAG = EntityTag.of(KEY,
        "turtle",
        "axolotl",
        "guardian",
        "elder_guardian",
        "cod",
        "pufferfish",
        "salmon",
        "tropical_fish",
        "dolphin",
        "squid",
        "glow_squid",
        "tadpole",
        "nautilus",
        "zombie_nautilus");

    public static final EntityTag LEGACY_TAG = EntityTag.of(KEY,
        "turtle",
        "axolotl",
        "guardian",
        "elder_guardian",
        "cod",
        "pufferfish",
        "salmon",
        "tropical_fish",
        "dolphin",
        "squid",
        "glow_squid",
        "tadpole");

    private SensitiveToImpaling() {
    }

    public static boolean contains(EntityType type, Era era) {
        if (era == Era.ENCHANTS_AS_DATA) {
            return TAG.contains(type);
        }
        return LEGACY_TAG.contains(type);
    }
}
