package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types.ItemTag;

public final class EnchantableChestArmor {

    public static final String KEY = "enchantable/chest_armor";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "leather_chestplate",
        "copper_chestplate",
        "chainmail_chestplate",
        "golden_chestplate",
        "iron_chestplate",
        "diamond_chestplate",
        "netherite_chestplate");

    private EnchantableChestArmor() {
    }
}
