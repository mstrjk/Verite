package teacommontea.veritechasse.vanilla.Potions.Tags;

import org.bukkit.entity.EntityType;

import teacommontea.veritechasse.vanilla.Enchantments.Tags.Types.EntityTag;
import teacommontea.veritechasse.vanilla.Potions.Support.EffectEra;

public final class Undead {

    public static final String KEY = "undead";

    public static final EntityTag TAG = EntityTag.of(KEY,
        "skeleton",
        "stray",
        "wither_skeleton",
        "skeleton_horse",
        "bogged",
        "parched",
        "zombie",
        "zombie_villager",
        "zombie_horse",
        "zombified_piglin",
        "zoglin",
        "drowned",
        "husk",
        "camel_husk",
        "zombie_nautilus",
        "wither",
        "phantom");

    public static final EntityTag LEGACY_TAG = EntityTag.of(KEY,
        "skeleton",
        "stray",
        "wither_skeleton",
        "skeleton_horse",
        "zombie",
        "zombie_villager",
        "zombie_horse",
        "zombified_piglin",
        "zoglin",
        "drowned",
        "husk",
        "wither",
        "phantom");

    private Undead() {
    }

    public static boolean contains(EntityType type, EffectEra era) {
        if (era == EffectEra.EFFECTS_AS_MODIFIERS) {
            return TAG.contains(type);
        }
        return LEGACY_TAG.contains(type);
    }

    public static boolean contains(String typeName, EffectEra era) {
        if (era == EffectEra.EFFECTS_AS_MODIFIERS) {
            return TAG.contains(typeName);
        }
        return LEGACY_TAG.contains(typeName);
    }
}
