package teacommontea.veritesauver.invsee;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import teacommontea.util.text.Text;

import java.util.ArrayList;
import java.util.List;


final class InvSeeIcons {


    private static org.bukkit.NamespacedKey markerKey() {
        return new org.bukkit.NamespacedKey("verite", "invsee_placeholder");
    }

    static boolean isPlaceholder(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer()
                .has(markerKey(), org.bukkit.persistence.PersistentDataType.BYTE);
    }

    private InvSeeIcons() {}

    static ItemStack placeholder() {
        return named(Material.BARRIER, teacommontea.util.Colours.BRAND_ACCENT_SECONDARY + "Verité Placeholder");
    }

    static ItemStack heldLabel() {
        Material pane = mat("LIGHT_BLUE_STAINED_GLASS_PANE", Material.PAPER);
        return named(pane, teacommontea.util.Colours.INVSEE_BLUE + "Held Item Slot");
    }

    static ItemStack offhandLabel() {
        Material pane = mat("RED_STAINED_GLASS_PANE", Material.PAPER);
        return named(pane, teacommontea.util.Colours.WARNING + "Off-hand Slot");
    }

    static ItemStack vehicleLabel() {
        Material pane = mat("YELLOW_STAINED_GLASS_PANE", Material.PAPER);
        return named(pane, teacommontea.util.Colours.WARN + "Vehicle Slot");
    }

    static ItemStack armourLabel(String piece) {
        return namedWithLore(mat("GRAY_STAINED_GLASS_PANE", Material.PAPER),
                teacommontea.util.Colours.BRAND_ACCENT + "Armour Slot",
                teacommontea.util.Colours.BRAND + piece);
    }

    private static ItemStack named(Material material, String miniName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Text.itemName(meta, miniName);
            mark(meta);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack namedWithLore(Material material, String miniName, String miniLore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Text.itemName(meta, miniName);
            Text.itemLore(meta, java.util.List.of(miniLore));
            mark(meta);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void mark(ItemMeta meta) {
        meta.getPersistentDataContainer().set(markerKey(),
                org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
    }

    private static Material mat(String name, Material fallback) {
        Material found = Material.getMaterial(name);
        return found != null ? found : fallback;
    }
}
