package teacommontea.util.chat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Set;


public final class StringChatListener implements Listener {

    public StringChatListener() {}

    private static final class View implements ChatBridge.Event {
        private final AsyncPlayerChatEvent e;
        View(AsyncPlayerChatEvent e) { this.e = e; }
        @Override public Player sender() { return e.getPlayer(); }
        @Override public String message() { return e.getMessage(); }
        @Override public void setMessage(String message) { e.setMessage(message); }
        @Override public Set<Player> recipients() { return e.getRecipients(); }
        @Override public boolean isCancelled() { return e.isCancelled(); }
        @Override public void setCancelled(boolean cancelled) { e.setCancelled(cancelled); }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void lowest(AsyncPlayerChatEvent e) { ChatRouter.dispatch(EventPriority.LOWEST, new View(e)); }

    @EventHandler(priority = EventPriority.LOW)
    public void low(AsyncPlayerChatEvent e) { ChatRouter.dispatch(EventPriority.LOW, new View(e)); }

    @EventHandler(priority = EventPriority.HIGH)
    public void high(AsyncPlayerChatEvent e) { ChatRouter.dispatch(EventPriority.HIGH, new View(e)); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void monitor(AsyncPlayerChatEvent e) { ChatRouter.dispatch(EventPriority.MONITOR, new View(e)); }
}
