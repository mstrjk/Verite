package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import java.util.Locale;
import java.util.Set;

public final class SoulSpeedBlocks {

    public static final String KEY = "soul_speed_blocks";

    public static final Set<String> MEMBERS = Set.of(
        "soul_sand",
        "soul_soil");

    private SoulSpeedBlocks() {
    }

    public static boolean contains(String blockName) {
        return blockName != null && MEMBERS.contains(blockName.toLowerCase(Locale.ROOT));
    }
}
