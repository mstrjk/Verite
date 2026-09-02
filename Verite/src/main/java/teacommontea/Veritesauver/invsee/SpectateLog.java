package teacommontea.veritesauver.invsee;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;


public final class SpectateLog {

    private final Plugin plugin;
    private final String target;
    private final String viewer;
    private final ItemDelta session = new ItemDelta();

    public SpectateLog(Plugin plugin, String target, String viewer) {
        this.plugin = plugin;
        this.target = target;
        this.viewer = viewer;
    }

    public void onOpen() {
        plugin.getLogger().info(target + "'s inventory is being viewed by " + viewer + ".");
    }

    public void onClick(List<ItemStack> before, List<ItemStack> after) {
        session.mergeFrom(ItemDelta.between(before, after));
    }

    public void onClose() {
        if (!session.isEmpty()) {
            plugin.getLogger().info(target + "'s inventory was modified by " + viewer
                    + "\nChanges:\n" + session.changes());
        }
        plugin.getLogger().info(target + "'s inventory is no longer being viewed by " + viewer + ".");
    }
}
