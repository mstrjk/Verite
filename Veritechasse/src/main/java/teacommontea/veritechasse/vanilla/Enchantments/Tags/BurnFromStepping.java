package teacommontea.veritechasse.vanilla.Enchantments.Tags;

import java.util.Locale;
import java.util.Set;

public final class BurnFromStepping {

    public static final String KEY = "burn_from_stepping";

    public static final Set<String> MEMBERS = Set.of(
        "campfire",
        "hot_floor",
        "sulfur_cube_hot");

    private BurnFromStepping() {
    }

    public static boolean contains(String damageType) {
        return damageType != null && MEMBERS.contains(damageType.toLowerCase(Locale.ROOT));
    }
}
