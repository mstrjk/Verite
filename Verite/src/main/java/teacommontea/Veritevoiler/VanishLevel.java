package teacommontea.veritevoiler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.Map;

final class VanishLevel {

    private static final String PREFIX = "verite.vanish.level.";

    private static Boolean lpPresent;

    private VanishLevel() {}

    static int tierOf(Player p) {
        if (p == null) return 0;
        if (!luckPermsPresent()) return 0;
        try {
            return readTier(p);
        } catch (Throwable t) {
            return 0;
        }
    }

    private static int readTier(Player p) {
        User user = LuckPermsProvider.get().getUserManager().getUser(p.getUniqueId());
        if (user == null) return 0;

        Map<String, Boolean> perms = user.getCachedData()
                .getPermissionData(net.luckperms.api.query.QueryOptions.defaultContextualOptions())
                .getPermissionMap();
        int best = 0;
        for (Map.Entry<String, Boolean> e : perms.entrySet()) {
            if (!e.getValue()) continue;
            String key = e.getKey();
            if (key.startsWith(PREFIX)) {
                try {
                    best = Math.max(best, Integer.parseInt(key.substring(PREFIX.length())));
                } catch (NumberFormatException ignored) {

                }
            }
        }
        return best;
    }

    private static boolean luckPermsPresent() {
        Boolean cached = lpPresent;
        if (cached != null) return cached;
        boolean present = Bukkit.getPluginManager().getPlugin("LuckPerms") != null;
        lpPresent = present;
        return present;
    }
}
