package teacommontea.veritevoiler;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.Iterator;


public final class BukkitPingListener implements Listener {

    private final Vanish vanish;

    public BukkitPingListener(Vanish vanish) { this.vanish = vanish; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(ServerListPingEvent e) {
        if (!vanish.enabled() || !vanish.settings().serverPing) return;
        try {
            Iterator<Player> it = e.iterator();
            while (it.hasNext()) {
                Player p = it.next();
                if (p != null && vanish.isVanished(p.getUniqueId())) it.remove();
            }
        } catch (UnsupportedOperationException ignored) {
            // this build's ping event does not allow roster iteration; leave the sample as is
        }
    }
}
