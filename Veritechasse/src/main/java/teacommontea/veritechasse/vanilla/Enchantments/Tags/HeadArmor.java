package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types.ItemTag;

public final class HeadArmor {

    public static final String KEY = "head_armor";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "leather_helmet",
        "copper_helmet",
        "chainmail_helmet",
        "golden_helmet",
        "iron_helmet",
        "diamond_helmet",
        "netherite_helmet",
        "turtle_helmet");

    private HeadArmor() {
    }
}
