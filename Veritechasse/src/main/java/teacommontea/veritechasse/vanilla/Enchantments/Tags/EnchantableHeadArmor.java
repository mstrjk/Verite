package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import teacommontea.veritechasse.vanilla.Enchantments.Tags.Types.ItemTag;

public final class EnchantableHeadArmor {

    public static final String KEY = "enchantable/head_armor";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "leather_helmet",
        "copper_helmet",
        "chainmail_helmet",
        "golden_helmet",
        "iron_helmet",
        "diamond_helmet",
        "netherite_helmet",
        "turtle_helmet");

    private EnchantableHeadArmor() {
    }
}
