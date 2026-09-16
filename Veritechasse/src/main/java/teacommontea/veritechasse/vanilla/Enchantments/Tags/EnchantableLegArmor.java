package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types.ItemTag;

public final class EnchantableLegArmor {

    public static final String KEY = "enchantable/leg_armor";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "leather_leggings",
        "copper_leggings",
        "chainmail_leggings",
        "golden_leggings",
        "iron_leggings",
        "diamond_leggings",
        "netherite_leggings");

    private EnchantableLegArmor() {
    }
}
