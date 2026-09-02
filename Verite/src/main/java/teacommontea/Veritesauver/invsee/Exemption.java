package teacommontea.veritesauver.invsee;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;


public final class Exemption {

    public static final String INVSEE_NODE = "drigz.invsee.exempt";
    public static final String ENDERSEE_NODE = "drigz.endersee.exempt";

    private Exemption() {}

    public static boolean isExempt(UUID targetUuid, String node) {
        Player online = targetUuid == null ? null : Bukkit.getPlayer(targetUuid);
        if (online != null) {
            return online.hasPermission(node);
        }
        return offlineHas(targetUuid, node);
    }

    private static boolean offlineHas(UUID uuid, String node) {
        if (uuid == null) {
            return false;
        }
        try {
            LuckPerms lp = LuckPermsProvider.get();
            User user = lp.getUserManager().getUser(uuid);
            boolean loaded = false;
            if (user == null) {
                user = lp.getUserManager().loadUser(uuid).join();
                loaded = true;
            }
            if (user == null) {
                return false;
            }
            Tristate state = user.getCachedData()
                .getPermissionData(QueryOptions.defaultContextualOptions())
                .checkPermission(node);
            boolean result = state == Tristate.TRUE;
            if (loaded) {
                lp.getUserManager().cleanupUser(user);
            }
            return result;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
