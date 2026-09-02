package teacommontea.util.chat;

import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;


public final class ChatRouter {

    private static final Map<EventPriority, List<Sub>> SUBS = new EnumMap<>(EventPriority.class);

    private ChatRouter() {}

    private static final class Sub {
        final ChatBridge.Handler handler;
        final boolean ignoreCancelled;
        Sub(ChatBridge.Handler h, boolean ic) { this.handler = h; this.ignoreCancelled = ic; }
    }

    public static synchronized void register(EventPriority priority, boolean ignoreCancelled, ChatBridge.Handler handler) {
        SUBS.computeIfAbsent(priority, k -> new CopyOnWriteArrayList<>()).add(new Sub(handler, ignoreCancelled));
    }

    public static void dispatch(EventPriority priority, ChatBridge.Event event) {
        List<Sub> subs = SUBS.get(priority);
        if (subs == null) return;
        for (Sub s : subs) {
            if (s.ignoreCancelled && event.isCancelled()) continue;
            s.handler.onChat(event);
        }
    }

    public static void install(Plugin plugin) {
        boolean componentEvent = classPresent("io.papermc.paper.event.player.AsyncChatEvent");
        if (componentEvent) {
            try {
                Class<?> c = Class.forName("teacommontea.util.chat.ComponentChatListener");
                Object listener = c.getConstructor().newInstance();
                Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) listener, plugin);
                return;
            } catch (Throwable ignored) {
            }
        }
        Bukkit.getPluginManager().registerEvents(new StringChatListener(), plugin);
    }

    private static boolean classPresent(String name) {
        try { Class.forName(name); return true; } catch (Throwable t) { return false; }
    }
}
