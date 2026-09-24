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
        plugin.getLogger().info(teacommontea.util.Lang.of("spectate.viewing", "name", target, "viewer", viewer));
    }

    public void onClick(List<ItemStack> before, List<ItemStack> after) {
        session.mergeFrom(ItemDelta.between(before, after));
    }

    public void onClose() {
        if (!session.isEmpty()) {
            plugin.getLogger().info(teacommontea.util.Lang.of("spectate.modified",
                    "name", target, "viewer", viewer, "changes", session.changes()));
        }
        plugin.getLogger().info(teacommontea.util.Lang.of("spectate.stopped", "name", target, "viewer", viewer));
    }
}
