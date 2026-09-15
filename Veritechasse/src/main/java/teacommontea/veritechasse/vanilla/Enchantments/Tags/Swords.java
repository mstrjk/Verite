package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import teacommontea.veritechasse.vanilla.Enchantments.Tags.Types.ItemTag;

public final class Swords {

    public static final String KEY = "swords";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "wooden_sword",
        "stone_sword",
        "copper_sword",
        "golden_sword",
        "iron_sword",
        "diamond_sword",
        "netherite_sword");

    private Swords() {
    }
}
