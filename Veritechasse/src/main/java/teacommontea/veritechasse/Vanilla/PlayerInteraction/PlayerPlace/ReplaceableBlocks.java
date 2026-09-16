package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerPlace;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class ReplaceableBlocks {

    public static final int SHORT_GRASS_MAJOR = 1;
    public static final int SHORT_GRASS_MINOR = 20;
    public static final int SHORT_GRASS_PATCH = 3;

    public static final int RESIN_MAJOR = 1;
    public static final int RESIN_MINOR = 21;
    public static final int RESIN_PATCH = 4;

    public static final int DRY_GRASS_MAJOR = 1;
    public static final int DRY_GRASS_MINOR = 21;
    public static final int DRY_GRASS_PATCH = 5;

    public static final Set<String> BASE = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "air",
            "void_air",
            "cave_air",
            "water",
            "lava",
            "bubble_column",
            "fern",
            "large_fern",
            "tall_grass",
            "dead_bush",
            "seagrass",
            "tall_seagrass",
            "fire",
            "soul_fire",
            "snow",
            "vine",
            "glow_lichen",
            "light",
            "structure_void",
            "hanging_roots",
            "warped_roots",
            "crimson_roots",
            "nether_sprouts")));

    public static final String LEGACY_GRASS = "grass";

    public static final String SHORT_GRASS = "short_grass";

    public static final String RESIN_CLUMP = "resin_clump";

    public static final Set<String> DRY_GRASS_ADDITIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "bush",
            "leaf_litter",
            "short_dry_grass",
            "tall_dry_grass")));

    private ReplaceableBlocks() {
    }

    public static boolean usesShortGrassName(Protocol protocol) {
        return protocol.atLeast(SHORT_GRASS_MAJOR, SHORT_GRASS_MINOR, SHORT_GRASS_PATCH);
    }

    public static boolean hasResinClump(Protocol protocol) {
        return protocol.atLeast(RESIN_MAJOR, RESIN_MINOR, RESIN_PATCH);
    }

    public static boolean hasDryGrass(Protocol protocol) {
        return protocol.atLeast(DRY_GRASS_MAJOR, DRY_GRASS_MINOR, DRY_GRASS_PATCH);
    }

    public static String normalise(String blockName) {
        if (blockName == null) {
            return "";
        }
        String trimmed = blockName.toLowerCase(Locale.ROOT).trim();
        int colon = trimmed.indexOf(':');
        return colon < 0 ? trimmed : trimmed.substring(colon + 1);
    }

    public static boolean contains(String blockName, Protocol protocol) {
        String key = normalise(blockName);
        if (key.isEmpty()) {
            return false;
        }
        if (BASE.contains(key)) {
            return true;
        }
        if (usesShortGrassName(protocol)) {
            if (SHORT_GRASS.equals(key)) {
                return true;
            }
        } else if (LEGACY_GRASS.equals(key)) {
            return true;
        }
        if (hasResinClump(protocol) && RESIN_CLUMP.equals(key)) {
            return true;
        }
        return hasDryGrass(protocol) && DRY_GRASS_ADDITIONS.contains(key);
    }

    public static int count(Protocol protocol) {
        int total = BASE.size() + 1;
        if (hasResinClump(protocol)) {
            total = total + 1;
        }
        if (hasDryGrass(protocol)) {
            total = total + DRY_GRASS_ADDITIONS.size();
        }
        return total;
    }
}
