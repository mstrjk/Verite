package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import teacommontea.veritechasse.vanilla.Enchantments.Tags.Types.EntityTag;

public final class Zombies {

    public static final String KEY = "zombies";

    public static final EntityTag TAG = EntityTag.of(KEY,
        "zombie",
        "zombie_villager",
        "zombified_piglin",
        "zoglin",
        "drowned",
        "husk",
        "zombie_horse",
        "camel_husk",
        "zombie_nautilus");

    private Zombies() {
    }
}
