package teacommontea.util.text;

import org.bukkit.Bukkit;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Method;

public final class Server {

    private Server() {}

    private static volatile Method broadcaster;
    private static volatile boolean broadcasterResolved;

    private static volatile Method inventoryFactory;
    private static volatile boolean inventoryResolved;

    private static volatile Method disallower;
    private static volatile boolean disallowResolved;

    private static volatile Method joinSetter;
    private static volatile boolean joinResolved;

    private static volatile Method quitSetter;
    private static volatile boolean quitResolved;

    public static void broadcast(String legacy) {
        if (!broadcasterResolved) {
            broadcasterResolved = true;
            broadcaster = method(Bukkit.getServer().getClass(), "broadcastMessage", String.class);
        }
        Method m = broadcaster;
        if (m == null) return;
        try {
            m.invoke(Bukkit.getServer(), legacy);
        } catch (Throwable ignored) {
            broadcaster = null;
        }
    }

    public static Inventory inventory(InventoryHolder holder, int size, String legacyTitle) {
        if (!inventoryResolved) {
            inventoryResolved = true;
            inventoryFactory = method(Bukkit.getServer().getClass(), "createInventory",
                    InventoryHolder.class, int.class, String.class);
        }
        Method m = inventoryFactory;
        if (m != null) {
            try {
                return (Inventory) m.invoke(Bukkit.getServer(), holder, size, legacyTitle);
            } catch (Throwable ignored) {
                inventoryFactory = null;
            }
        }
        return Bukkit.createInventory(holder, size);
    }

    public static void disallow(AsyncPlayerPreLoginEvent event,
                                AsyncPlayerPreLoginEvent.Result result, String legacyReason) {
        if (!disallowResolved) {
            disallowResolved = true;
            disallower = method(AsyncPlayerPreLoginEvent.class, "disallow",
                    AsyncPlayerPreLoginEvent.Result.class, String.class);
        }
        Method m = disallower;
        if (m == null) return;
        try {
            m.invoke(event, result, legacyReason);
        } catch (Throwable ignored) {
            disallower = null;
        }
    }

    public static void joinMessage(PlayerJoinEvent event, String legacy) {
        if (!joinResolved) {
            joinResolved = true;
            joinSetter = method(PlayerJoinEvent.class, "setJoinMessage", String.class);
        }
        invoke(joinSetter, event, legacy);
    }

    public static void quitMessage(PlayerQuitEvent event, String legacy) {
        if (!quitResolved) {
            quitResolved = true;
            quitSetter = method(PlayerQuitEvent.class, "setQuitMessage", String.class);
        }
        invoke(quitSetter, event, legacy);
    }

    private static void invoke(Method m, Object target, Object arg) {
        if (m == null) return;
        try {
            m.invoke(target, arg);
        } catch (Throwable ignored) {
            return;
        }
    }

    private static Method method(Class<?> owner, String name, Class<?>... params) {
        try {
            Method m = owner.getMethod(name, params);
            m.setAccessible(true);
            return m;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
