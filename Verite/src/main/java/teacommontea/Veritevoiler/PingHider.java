package teacommontea.veritevoiler;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;


public final class PingHider {

    private PingHider() {}

    public static void install(Plugin plugin, Vanish vanish) {
        if (classPresent("com.destroystokyo.paper.event.server.PaperServerListPingEvent")) {
            try {
                Class<?> c = Class.forName("teacommontea.veritevoiler.PaperPingListener");
                Object listener = c.getConstructor(Vanish.class).newInstance(vanish);
                Bukkit.getPluginManager().registerEvents((Listener) listener, plugin);
                return;
            } catch (Throwable ignored) {
                // fall through to the core hider
            }
        }
        Bukkit.getPluginManager().registerEvents(new BukkitPingListener(vanish), plugin);
    }

    private static boolean classPresent(String name) {
        try { Class.forName(name); return true; } catch (Throwable t) { return false; }
    }
}
