package teacommontea.veritevoiler;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class VanishListener implements Listener {

    private final Vanish vanish;

    public VanishListener(Vanish vanish) {
        this.vanish = vanish;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        vanish.applyOnJoin(e.getPlayer());
    }
}
