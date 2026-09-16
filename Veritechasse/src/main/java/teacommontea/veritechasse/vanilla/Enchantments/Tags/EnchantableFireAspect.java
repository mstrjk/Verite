package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types.ItemTag;

public final class EnchantableFireAspect {

    public static final String KEY = "enchantable/fire_aspect";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "wooden_sword",
        "stone_sword",
        "copper_sword",
        "golden_sword",
        "iron_sword",
        "diamond_sword",
        "netherite_sword",
        "wooden_spear",
        "stone_spear",
        "copper_spear",
        "golden_spear",
        "iron_spear",
        "diamond_spear",
        "netherite_spear",
        "mace");

    private EnchantableFireAspect() {
    }
}
