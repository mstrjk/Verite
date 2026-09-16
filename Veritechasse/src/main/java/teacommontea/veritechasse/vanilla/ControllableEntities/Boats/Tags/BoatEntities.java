package teacommontea.veritechasse.Vanilla.ControllableEntities.Boats.Tags;

import java.util.Locale;
import java.util.Set;

public final class BoatEntities {

    public static final Set<String> BOATS = Set.of(
        "acacia_boat",
        "birch_boat",
        "cherry_boat",
        "dark_oak_boat",
        "jungle_boat",
        "mangrove_boat",
        "oak_boat",
        "pale_oak_boat",
        "spruce_boat");

    public static final Set<String> CHEST_BOATS = Set.of(
        "acacia_chest_boat",
        "birch_chest_boat",
        "cherry_chest_boat",
        "dark_oak_chest_boat",
        "jungle_chest_boat",
        "mangrove_chest_boat",
        "oak_chest_boat",
        "pale_oak_chest_boat",
        "spruce_chest_boat");

    public static final Set<String> RAFTS = Set.of("bamboo_raft");

    public static final Set<String> CHEST_RAFTS = Set.of("bamboo_chest_raft");

    private BoatEntities() {
    }

    public static boolean contains(String entityName) {
        if (entityName == null) {
            return false;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        return BOATS.contains(name)
            || CHEST_BOATS.contains(name)
            || RAFTS.contains(name)
            || CHEST_RAFTS.contains(name);
    }

    public static boolean isRaft(String entityName) {
        if (entityName == null) {
            return false;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        return RAFTS.contains(name) || CHEST_RAFTS.contains(name);
    }

    public static boolean isChested(String entityName) {
        if (entityName == null) {
            return false;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        return CHEST_BOATS.contains(name) || CHEST_RAFTS.contains(name);
    }

    public static int count() {
        return BOATS.size() + CHEST_BOATS.size() + RAFTS.size() + CHEST_RAFTS.size();
    }
}
