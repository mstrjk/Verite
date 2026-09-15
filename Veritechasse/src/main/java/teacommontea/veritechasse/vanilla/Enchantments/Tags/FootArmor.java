package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import teacommontea.veritechasse.vanilla.Enchantments.Tags.Types.ItemTag;

public final class FootArmor {

    public static final String KEY = "foot_armor";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "leather_boots",
        "copper_boots",
        "chainmail_boots",
        "golden_boots",
        "iron_boots",
        "diamond_boots",
        "netherite_boots");

    private FootArmor() {
    }
}
