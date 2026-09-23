package teacommontea.dashboard;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.core.Entry;

public final class DashboardSnapshot {

    private static final int ACTIVE_LIMIT = 500;

    private DashboardSnapshot() {}

    public static String build(Plugin plugin, String serverName) {
        long now = System.currentTimeMillis();

        List<String> punishments = new ArrayList<>();
        Sauver sauver = Sauver.instance();
        if (sauver != null && sauver.dao() != null) {
            for (Entry.Type type : new Entry.Type[] { Entry.Type.BAN, Entry.Type.MUTE }) {
                for (Entry e : sauver.dao().activeOfType(type, now)) {
                    punishments.add(encode(sauver.dao(), e, now));
                    if (punishments.size() >= ACTIVE_LIMIT) {
                        break;
                    }
                }
            }
        }

        return Json.object(
                "server", serverName,
                "version", plugin.getDescription().getVersion(),
                "generatedAt", now,
                "punishments", Json.raw(Json.array(punishments)),
                "config", Json.raw(config(plugin)),
                "settings", Json.raw(DashboardConfig.describe(plugin)),
                "state", Json.raw(DashboardState.server(plugin)),
                "online", Json.raw(DashboardState.players())
        );
    }

    private static String encode(teacommontea.veritesauver.core.SauverDAO dao, Entry e, long now) {
        return Json.object(
                "id", e.randomId(),
                "type", e.type().id(),
                "target", e.uuid() == null ? e.ip() : e.uuid().toString(),
                "targetName", e.uuid() == null ? null : dao.nameOf(e.uuid()),
                "reason", e.reason(),
                "issuedBy", e.executorName(),
                "start", e.dateStart(),
                "end", e.permanent() ? -1L : e.dateEnd(),
                "permanent", e.permanent(),
                "active", e.inForce(now),
                "removedBy", e.removedByName(),
                "ip", e.ip(),
                "silent", e.silent()
        );
    }

    private static String config(Plugin plugin) {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.isFile()) {
            return "null";
        }
        try {
            String body = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            return Json.object("file", "config.yml", "body", body);
        } catch (Exception e) {
            return "null";
        }
    }

}
