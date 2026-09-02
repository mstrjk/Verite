package teacommontea.util.chat;

import io.papermc.paper.event.player.AsyncChatEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Set;


public final class ComponentChatListener implements Listener {

    public ComponentChatListener() {}

    private static final class View implements ChatBridge.Event {
        private final AsyncChatEvent e;
        View(AsyncChatEvent e) { this.e = e; }
        @Override public Player sender() { return e.getPlayer(); }
        @Override public String message() { return AdventurePlain.plain(e.message()); }
        @Override public void setMessage(String message) {
            e.message(net.kyori.adventure.text.Component.text(message));
        }
        @Override public Set<Player> recipients() {
            Set<Player> out = new java.util.HashSet<>();
            for (Object viewer : e.viewers()) {
                if (viewer instanceof Player p) out.add(p);
            }
            return new ViewerSet(e, out);
        }
        @Override public boolean isCancelled() { return e.isCancelled(); }
        @Override public void setCancelled(boolean cancelled) { e.setCancelled(cancelled); }
    }

    private static final class ViewerSet extends java.util.AbstractSet<Player> {
        private final AsyncChatEvent e;
        private final Set<Player> snapshot;
        ViewerSet(AsyncChatEvent e, Set<Player> snapshot) { this.e = e; this.snapshot = snapshot; }
        @Override public java.util.Iterator<Player> iterator() { return snapshot.iterator(); }
        @Override public int size() { return snapshot.size(); }
        @Override public boolean removeIf(java.util.function.Predicate<? super Player> filter) {
            boolean[] changed = { false };
            e.viewers().removeIf(v -> {
                if (v instanceof Player p && filter.test(p)) { snapshot.remove(p); changed[0] = true; return true; }
                return false;
            });
            return changed[0];
        }
        @Override public boolean remove(Object o) {
            if (!(o instanceof Player p)) return false;
            boolean r = e.viewers().remove(p);
            if (r) snapshot.remove(p);
            return r;
        }
    }

    private static final class AdventurePlain {
        private static final java.lang.reflect.Method SERIALIZE;
        private static final Object SERIALIZER;
        static {
            java.lang.reflect.Method serialize = null;
            Object serializer = null;
            try {
                Class<?> plainSer = Class.forName(
                        "net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer");
                serializer = plainSer.getMethod("plainText").invoke(null);
                Class<?> comp = Class.forName("net.kyori.adventure.text.Component");
                serialize = plainSer.getMethod("serialize", comp);
            } catch (Throwable ignored) {
            }
            SERIALIZE = serialize;
            SERIALIZER = serializer;
        }

        static String plain(Object adventureComponent) {
            if (adventureComponent == null) return "";
            if (SERIALIZE != null && SERIALIZER != null) {
                try {
                    return (String) SERIALIZE.invoke(SERIALIZER, adventureComponent);
                } catch (Throwable ignored) {
                }
            }
            return teacommontea.util.text.Text.plainFrom(adventureComponent);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void lowest(AsyncChatEvent e) { ChatRouter.dispatch(EventPriority.LOWEST, new View(e)); }

    @EventHandler(priority = EventPriority.LOW)
    public void low(AsyncChatEvent e) { ChatRouter.dispatch(EventPriority.LOW, new View(e)); }

    @EventHandler(priority = EventPriority.HIGH)
    public void high(AsyncChatEvent e) { ChatRouter.dispatch(EventPriority.HIGH, new View(e)); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void monitor(AsyncChatEvent e) { ChatRouter.dispatch(EventPriority.MONITOR, new View(e)); }
}
