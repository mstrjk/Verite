package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types.ItemTag;

public final class EnchantableSweeping {

    public static final String KEY = "enchantable/sweeping";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "wooden_sword",
        "stone_sword",
        "copper_sword",
        "golden_sword",
        "iron_sword",
        "diamond_sword",
        "netherite_sword");

    private EnchantableSweeping() {
    }
}
