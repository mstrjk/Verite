package teacommontea.veritechasse.Vanilla.PlayerArmour.Support;

import java.util.Locale;
import java.util.Map;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class Materials {

    public static final Material LEATHER = new Material("leather", 5, 1, 2, 3, 1, 3, 15, 0.0F, 0.0F);
    public static final Material COPPER = new Material("copper", 11, 1, 3, 4, 2, 4, 8, 0.0F, 0.0F);
    public static final Material CHAINMAIL = new Material("chainmail", 15, 1, 4, 5, 2, 4, 12, 0.0F, 0.0F);
    public static final Material IRON = new Material("iron", 15, 2, 5, 6, 2, 5, 9, 0.0F, 0.0F);
    public static final Material GOLD = new Material("gold", 7, 1, 3, 5, 2, 7, 25, 0.0F, 0.0F);
    public static final Material DIAMOND = new Material("diamond", 33, 3, 6, 8, 3, 11, 10, 2.0F, 0.0F);
    public static final Material TURTLE_SCUTE = new Material("turtle_scute", 25, 2, 5, 6, 2, 5, 9, 0.0F, 0.0F);
    public static final Material NETHERITE = new Material("netherite", 37, 3, 6, 8, 3, 19, 15, 3.0F, 0.1F);

    private static final Map<String, Material> BY_KEY = Map.of(
        "leather", LEATHER,
        "copper", COPPER,
        "chainmail", CHAINMAIL,
        "iron", IRON,
        "gold", GOLD,
        "diamond", DIAMOND,
        "turtle_scute", TURTLE_SCUTE,
        "netherite", NETHERITE);

    private Materials() {
    }

    public static boolean copperExists(Protocol protocol) {
        return protocol.atLeast(1, 21, 9);
    }

    public static boolean existsIn(Material material, Protocol protocol) {
        if (material == null) {
            return false;
        }
        if (material == COPPER) {
            return copperExists(protocol);
        }
        return true;
    }

    public static Material byKey(String key) {
        if (key == null) {
            return null;
        }
        return BY_KEY.get(key.toLowerCase(Locale.ROOT));
    }

    public static Material fromItemName(String itemName) {
        if (itemName == null) {
            return null;
        }
        String name = itemName.toLowerCase(Locale.ROOT);
        if (name.equals("turtle_helmet")) {
            return TURTLE_SCUTE;
        }
        if (name.startsWith("leather_")) {
            return LEATHER;
        }
        if (name.startsWith("copper_")) {
            return COPPER;
        }
        if (name.startsWith("chainmail_")) {
            return CHAINMAIL;
        }
        if (name.startsWith("iron_")) {
            return IRON;
        }
        if (name.startsWith("golden_")) {
            return GOLD;
        }
        if (name.startsWith("diamond_")) {
            return DIAMOND;
        }
        if (name.startsWith("netherite_")) {
            return NETHERITE;
        }
        return null;
    }
}
