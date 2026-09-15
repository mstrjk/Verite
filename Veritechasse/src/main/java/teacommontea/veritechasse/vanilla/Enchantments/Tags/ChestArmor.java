package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import teacommontea.veritechasse.vanilla.Enchantments.Tags.Types.ItemTag;

public final class ChestArmor {

    public static final String KEY = "chest_armor";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "leather_chestplate",
        "copper_chestplate",
        "chainmail_chestplate",
        "golden_chestplate",
        "iron_chestplate",
        "diamond_chestplate",
        "netherite_chestplate");

    private ChestArmor() {
    }
}
