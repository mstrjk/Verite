package teacommontea.veritechasse.Vanilla.Enchantments.Tags;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Types.EntityTag;

public final class SensitiveToBaneOfArthropods {

    public static final String KEY = "sensitive_to_bane_of_arthropods";

    public static final EntityTag TAG = EntityTag.of(KEY,
        "bee",
        "endermite",
        "silverfish",
        "spider",
        "cave_spider");

    private SensitiveToBaneOfArthropods() {
    }
}
