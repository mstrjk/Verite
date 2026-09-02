package teacommontea.veritesauver.invsee;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryView;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class InvSeeListener implements Listener {

    public static final String INVSEE_EDIT = "drigz.staff.invsee.edit";
    public static final String ENDERSEE_EDIT = "drigz.staff.endersee.edit";
    public static final String NO_TAKEOUT = "invsee.no.takeout";

    private final OfflineInventories offline;
    private final InvSeeAccess access;
    private final org.bukkit.plugin.Plugin plugin;

    private record OfflineWindow(UUID uuid, String name, OfflineInventories.Kind kind, ContainerHandler top) {}

    private final Map<UUID, OfflineWindow> offlineWindows = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> enderWindows = new ConcurrentHashMap<>();
    private final Map<UUID, ContainerHandler> mainWindows = new ConcurrentHashMap<>();
    private final java.util.Set<UUID> openSpectators = ConcurrentHashMap.newKeySet();

    private static final java.util.Set<Integer> EQUIP_POSITIONS = java.util.Set.of(38, 39, 41, 42, 43, 44);

    public InvSeeListener(org.bukkit.plugin.Plugin plugin, OfflineInventories offline, InvSeeAccess access) {
        this.plugin = plugin;
        this.offline = offline;
        this.access = access;
    }

    public void trackOffline(UUID spectator, UUID targetUuid, String targetName,
                             OfflineInventories.Kind kind, ContainerHandler top) {
        offlineWindows.put(spectator, new OfflineWindow(targetUuid, targetName, kind, top));
    }

    public void markSpectating(UUID spectator, boolean ender) {
        openSpectators.add(spectator);
        enderWindows.put(spectator, ender);
    }

    public void trackMain(UUID spectator, ContainerHandler top) {
        mainWindows.put(spectator, top);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent event) {
        HumanEntity who = event.getWhoClicked();
        if (!isSpectateView(who)) {
            return;
        }
        if (!editable(who)) {
            event.setCancelled(true);
            return;
        }
        if (handlePlaceOverPane(event, who)) {
            return;
        }
        if (noTakeout(who) && isTakeout(event)) {
            event.setCancelled(true);
            return;
        }
        scrubPaneCursor(event);
    }

    private boolean handlePlaceOverPane(InventoryClickEvent event, HumanEntity who) {
        int raw = event.getRawSlot();
        if (!EQUIP_POSITIONS.contains(raw)) {
            return false;
        }
        int topSize = event.getView().getTopInventory().getSize();
        if (raw >= topSize) {
            return false;
        }
        org.bukkit.inventory.ItemStack current = event.getCurrentItem();
        if (!isPlaceholderPane(current)) {
            return false;
        }
        org.bukkit.inventory.ItemStack cursor = event.getCursor();
        ContainerHandler top = mainWindows.get(who.getUniqueId());
        if (top == null) {
            return false;
        }
        event.setCancelled(true);
        if (cursor == null || cursor.getType() == org.bukkit.Material.AIR) {
            return true;
        }
        try {
            Object nmsStack = access.asNmsCopy(cursor);
            top.setItem(raw, nmsStack);
            who.setItemOnCursor(null);
            teacommontea.util.sched.Sched.executeFor(who, () -> {
                if (who instanceof org.bukkit.entity.Player p) {
                    p.updateInventory();
                }
            });
        } catch (Throwable ignored) {
        }
        return true;
    }

    private void scrubPaneCursor(InventoryClickEvent event) {
        final org.bukkit.entity.HumanEntity who = event.getWhoClicked();
        teacommontea.util.sched.Sched.executeFor(who, () -> {
            org.bukkit.inventory.ItemStack cursor = who.getItemOnCursor();
            if (isPlaceholderPane(cursor)) {
                who.setItemOnCursor(null);
            }
        });
    }

    private static boolean isPlaceholderPane(org.bukkit.inventory.ItemStack stack) {
        return stack != null && InvSeeIcons.isPlaceholder(stack);
    }

    private static boolean noTakeout(HumanEntity who) {
        if (who.isOp()) {
            return false;
        }
        return who.isPermissionSet(NO_TAKEOUT) && who.hasPermission(NO_TAKEOUT);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent event) {
        HumanEntity who = event.getWhoClicked();
        if (!isSpectateView(who)) {
            return;
        }
        if (!editable(who)) {
            event.setCancelled(true);
            return;
        }
        if (noTakeout(who)) {
            int topSize = event.getView().getTopInventory().getSize();
            for (int raw : event.getRawSlots()) {
                if (raw >= topSize) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    private boolean isSpectateView(HumanEntity who) {
        return openSpectators.contains(who.getUniqueId());
    }

    private boolean editable(HumanEntity who) {
        Boolean ender = enderWindows.get(who.getUniqueId());
        if (ender == null) {
            return true;
        }
        return who.hasPermission(ender ? ENDERSEE_EDIT : INVSEE_EDIT);
    }

    private static boolean isTakeout(InventoryClickEvent event) {
        int topSize = event.getView().getTopInventory().getSize();
        boolean clickedTop = event.getRawSlot() >= 0 && event.getRawSlot() < topSize;
        return switch (event.getAction()) {
            case PICKUP_ALL, PICKUP_HALF, PICKUP_ONE, PICKUP_SOME,
                 HOTBAR_SWAP, HOTBAR_MOVE_AND_READD, SWAP_WITH_CURSOR,
                 DROP_ALL_SLOT, DROP_ONE_SLOT, COLLECT_TO_CURSOR -> clickedTop;
            case MOVE_TO_OTHER_INVENTORY -> clickedTop;
            default -> false;
        };
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        UUID spectator = event.getPlayer().getUniqueId();
        openSpectators.remove(spectator);
        enderWindows.remove(spectator);
        mainWindows.remove(spectator);
        OfflineWindow window = offlineWindows.remove(spectator);
        if (window == null) {
            return;
        }
        List<Object> contents = new java.util.ArrayList<>(window.top().getContents());
        teacommontea.util.sched.Sched.executeAsync(
                () -> offline.save(window.uuid(), window.name(), window.kind(), contents));
    }

}
