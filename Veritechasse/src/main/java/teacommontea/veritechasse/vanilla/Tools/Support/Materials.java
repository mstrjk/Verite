package teacommontea.veritechasse.vanilla.Tools.Support;

import java.util.Locale;
import java.util.Map;

import teacommontea.veritechasse.vanilla.Protocol;

public final class Materials {

    public static final Material WOOD = new Material("wood", 0, 59, 2.0F, 0.0F, 15);
    public static final Material STONE = new Material("stone", 1, 131, 4.0F, 1.0F, 5);
    public static final Material COPPER = new Material("copper", 1, 190, 5.0F, 1.0F, 13);
    public static final Material IRON = new Material("iron", 2, 250, 6.0F, 2.0F, 14);
    public static final Material DIAMOND = new Material("diamond", 3, 1561, 8.0F, 3.0F, 10);
    public static final Material GOLD = new Material("gold", 0, 32, 12.0F, 0.0F, 22);
    public static final Material NETHERITE = new Material("netherite", 4, 2031, 9.0F, 4.0F, 15);

    private static final Map<String, Material> BY_KEY = Map.of(
        "wood", WOOD,
        "stone", STONE,
        "copper", COPPER,
        "iron", IRON,
        "diamond", DIAMOND,
        "gold", GOLD,
        "netherite", NETHERITE);

    private Materials() {
    }

    public static boolean copperExists(Protocol protocol) {
        return protocol.atLeast(1, 21, 9);
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
        if (name.startsWith("wooden_")) {
            return WOOD;
        }
        if (name.startsWith("stone_")) {
            return STONE;
        }
        if (name.startsWith("copper_")) {
            return COPPER;
        }
        if (name.startsWith("iron_")) {
            return IRON;
        }
        if (name.startsWith("diamond_")) {
            return DIAMOND;
        }
        if (name.startsWith("golden_")) {
            return GOLD;
        }
        if (name.startsWith("netherite_")) {
            return NETHERITE;
        }
        return null;
    }
}
