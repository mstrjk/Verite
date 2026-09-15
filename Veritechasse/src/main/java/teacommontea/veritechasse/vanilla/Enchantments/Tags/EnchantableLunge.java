package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import teacommontea.veritechasse.vanilla.Enchantments.Tags.Types.ItemTag;

public final class EnchantableLunge {

    public static final String KEY = "enchantable/lunge";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "wooden_spear",
        "stone_spear",
        "copper_spear",
        "golden_spear",
        "iron_spear",
        "diamond_spear",
        "netherite_spear");

    private EnchantableLunge() {
    }
}
