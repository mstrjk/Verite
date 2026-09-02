package teacommontea.veritevoiler;

import com.destroystokyo.paper.event.server.PaperServerListPingEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;


public final class PaperPingListener implements Listener {

    private final Vanish vanish;

    public PaperPingListener(Vanish vanish) { this.vanish = vanish; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(PaperServerListPingEvent e) {
        if (!vanish.enabled() || !vanish.settings().serverPing) return;
        int vanishedOnline = 0;
        for (UUID id : vanish.getVanished()) {
            if (Bukkit.getPlayer(id) != null) vanishedOnline++;
        }
        if (vanishedOnline > 0) {
            e.setNumPlayers(Math.max(0, e.getNumPlayers() - vanishedOnline));
        }
        e.getPlayerSample().removeIf(profile ->
                profile.getId() != null && vanish.isVanished(profile.getId()));
    }
}
