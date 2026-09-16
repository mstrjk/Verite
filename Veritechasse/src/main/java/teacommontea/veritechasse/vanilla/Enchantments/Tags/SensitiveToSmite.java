package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types.EntityTag;

public final class SensitiveToSmite {

    public static final String KEY = "sensitive_to_smite";

    public static final EntityTag TAG = EntityTag.of(KEY,
        "skeleton",
        "stray",
        "wither_skeleton",
        "skeleton_horse",
        "bogged",
        "parched",
        "zombie",
        "zombie_villager",
        "zombified_piglin",
        "zoglin",
        "drowned",
        "husk",
        "zombie_horse",
        "camel_husk",
        "zombie_nautilus",
        "wither",
        "phantom");

    private SensitiveToSmite() {
    }
}
