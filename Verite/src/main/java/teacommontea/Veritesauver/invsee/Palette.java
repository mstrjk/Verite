package teacommontea.veritesauver.invsee;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumMap;
import java.util.Map;


public final class Palette {

    public enum Kind {
        INACCESSIBLE, ARMOUR_HELMET, ARMOUR_CHESTPLATE, ARMOUR_LEGGINGS, ARMOUR_BOOTS,
        OFFHAND, BODY, SADDLE, CURSOR, GENERIC,
        CRAFTING, ANVIL, MERCHANT, CARTOGRAPHY, ENCHANTING_ITEM, ENCHANTING_FUEL,
        GRINDSTONE, LOOM, SMITHING_BASE, SMITHING_TEMPLATE, SMITHING_ADDITION, STONECUTTER
    }

    private final Map<Kind, ItemStack> items;

    private Palette(Map<Kind, ItemStack> items) {
        this.items = items;
    }

    public ItemStack get(Kind kind) {
        ItemStack item = items.get(kind);
        return item == null ? null : item.clone();
    }

    public static Palette glass() {
        Map<Kind, ItemStack> m = new EnumMap<>(Kind.class);
        m.put(Kind.INACCESSIBLE, named(Material.BLACK_STAINED_GLASS_PANE, "Inaccessible"));
        m.put(Kind.ARMOUR_HELMET, named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "Helmet"));
        m.put(Kind.ARMOUR_CHESTPLATE, named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "Chestplate"));
        m.put(Kind.ARMOUR_LEGGINGS, named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "Leggings"));
        m.put(Kind.ARMOUR_BOOTS, named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "Boots"));
        m.put(Kind.OFFHAND, named(Material.YELLOW_STAINED_GLASS_PANE, "Off-hand"));
        m.put(Kind.BODY, named(Material.GRAY_STAINED_GLASS_PANE, "Body"));
        m.put(Kind.SADDLE, named(Material.ORANGE_STAINED_GLASS_PANE, "Saddle"));
        m.put(Kind.CURSOR, named(Material.WHITE_STAINED_GLASS_PANE, "Cursor"));
        m.put(Kind.GENERIC, named(Material.CYAN_STAINED_GLASS_PANE, "Empty"));
        m.put(Kind.CRAFTING, named(Material.ORANGE_STAINED_GLASS_PANE, "Crafting"));
        m.put(Kind.ANVIL, named(Material.GRAY_STAINED_GLASS_PANE, "Anvil"));
        m.put(Kind.MERCHANT, named(Material.LIME_STAINED_GLASS_PANE, "Trade"));
        m.put(Kind.CARTOGRAPHY, named(Material.BROWN_STAINED_GLASS_PANE, "Cartography"));
        m.put(Kind.ENCHANTING_ITEM, named(Material.BLUE_STAINED_GLASS_PANE, "Enchanting"));
        m.put(Kind.ENCHANTING_FUEL, named(Material.BLUE_STAINED_GLASS_PANE, "Lapis"));
        m.put(Kind.GRINDSTONE, named(Material.MAGENTA_STAINED_GLASS_PANE, "Grindstone"));
        m.put(Kind.LOOM, named(Material.PINK_STAINED_GLASS_PANE, "Loom"));
        m.put(Kind.SMITHING_BASE, named(Material.GRAY_STAINED_GLASS_PANE, "Smithing Base"));
        m.put(Kind.SMITHING_TEMPLATE, named(Material.GRAY_STAINED_GLASS_PANE, "Smithing Template"));
        m.put(Kind.SMITHING_ADDITION, named(Material.GRAY_STAINED_GLASS_PANE, "Smithing Addition"));
        m.put(Kind.STONECUTTER, named(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "Stonecutter"));
        return new Palette(m);
    }

    private static ItemStack named(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
