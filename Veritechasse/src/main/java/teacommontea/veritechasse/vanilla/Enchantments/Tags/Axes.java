package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types.ItemTag;

public final class Axes {

    public static final String KEY = "axes";

    public static final ItemTag TAG = ItemTag.of(KEY,
        "wooden_axe",
        "stone_axe",
        "copper_axe",
        "golden_axe",
        "iron_axe",
        "diamond_axe",
        "netherite_axe");

    private Axes() {
    }
}
