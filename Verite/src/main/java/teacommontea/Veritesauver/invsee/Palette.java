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
        m.put(Kind.INACCESSIBLE, named(Material.BLACK_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.inaccessible")));
        m.put(Kind.ARMOUR_HELMET, named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.helmet")));
        m.put(Kind.ARMOUR_CHESTPLATE, named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.chestplate")));
        m.put(Kind.ARMOUR_LEGGINGS, named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.leggings")));
        m.put(Kind.ARMOUR_BOOTS, named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.boots")));
        m.put(Kind.OFFHAND, named(Material.YELLOW_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.offhand")));
        m.put(Kind.BODY, named(Material.GRAY_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.body")));
        m.put(Kind.SADDLE, named(Material.ORANGE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.saddle")));
        m.put(Kind.CURSOR, named(Material.WHITE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.cursor")));
        m.put(Kind.GENERIC, named(Material.CYAN_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.empty")));
        m.put(Kind.CRAFTING, named(Material.ORANGE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.crafting")));
        m.put(Kind.ANVIL, named(Material.GRAY_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.anvil")));
        m.put(Kind.MERCHANT, named(Material.LIME_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.trade")));
        m.put(Kind.CARTOGRAPHY, named(Material.BROWN_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.cartography")));
        m.put(Kind.ENCHANTING_ITEM, named(Material.BLUE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.enchanting")));
        m.put(Kind.ENCHANTING_FUEL, named(Material.BLUE_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.lapis")));
        m.put(Kind.GRINDSTONE, named(Material.MAGENTA_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.grindstone")));
        m.put(Kind.LOOM, named(Material.PINK_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.loom")));
        m.put(Kind.SMITHING_BASE, named(Material.GRAY_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.smithing.base")));
        m.put(Kind.SMITHING_TEMPLATE, named(Material.GRAY_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.smithing.template")));
        m.put(Kind.SMITHING_ADDITION, named(Material.GRAY_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.smithing.addition")));
        m.put(Kind.STONECUTTER, named(Material.LIGHT_GRAY_STAINED_GLASS_PANE, teacommontea.util.Lang.of("invsee.pane.stonecutter")));
        return new Palette(m);
    }

    private static ItemStack named(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            teacommontea.util.text.Items.name(meta, name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
