package teacommontea.veritesauver.invsee;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;


public final class InvSeeTab {

    public static final String TABCOMPLETE = "drigz.staff.invsee.tabcomplete";

    private final boolean offlineNames;

    public InvSeeTab(boolean offlineNames) {
        this.offlineNames = offlineNames;
    }

    public List<String> complete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(TABCOMPLETE)) {
            return List.of();
        }
        if (args.length > 1) {
            return List.of();
        }
        String prefix = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        Player viewer = sender instanceof Player p ? p : null;

        TreeSet<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (viewer == null || viewer.canSee(online)) {
                names.add(online.getName());
            }
        }
        if (offlineNames) {
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                String name = op.getName();
                if (name != null) {
                    names.add(name);
                }
            }
        }

        List<String> out = new ArrayList<>();
        for (String name : names) {
            if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                out.add(name);
            }
        }
        return out;
    }
}
