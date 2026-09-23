package teacommontea.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.plugin.Plugin;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverDAO;

public final class DashboardQueries {

    private static final int LIMIT = 200;

    private DashboardQueries() {}

    public static String activePunishments() {
        SauverDAO dao = dao();
        return dao == null ? "[]" : active(dao, "all");
    }

    public static boolean readsServer(String what) {
        return "players".equals(what) || "server".equals(what) || "online".equals(what);
    }

    public static String run(Plugin plugin, Map<String, String> body) {
        String what = body.get("what");
        if (what == null) {
            return "null";
        }

        SauverDAO dao = dao();
        if (dao == null) {
            return "null";
        }

        return switch (what) {
            case "player" -> player(dao, body.get("target"));
            case "history" -> history(dao, body.get("uuid"));
            case "punishment" -> punishment(dao, body.get("uuid"), body.get("id"));
            case "online" -> online();
            case "players" -> DashboardState.players();
            case "server" -> DashboardState.server(plugin);
            case "config" -> DashboardConfig.describe(plugin);
            case "active" -> active(dao, body.get("type"));
            case "ip" -> byIp(dao, body.get("ip"));
            case "names" -> names(dao);
            default -> "null";
        };
    }

    private static String active(SauverDAO dao, String rawType) {
        long now = System.currentTimeMillis();
        List<String> out = new ArrayList<>();

        Entry.Type[] types = rawType == null || rawType.isBlank() || "all".equals(rawType)
                ? new Entry.Type[] { Entry.Type.BAN, Entry.Type.MUTE }
                : new Entry.Type[] { parseType(rawType) };

        for (Entry.Type type : types) {
            if (type == null) {
                continue;
            }
            for (Entry e : dao.activeOfType(type, now)) {
                out.add(encodeFull(dao, e, now));
                if (out.size() >= LIMIT) {
                    return Json.array(out);
                }
            }
        }

        if (types.length == 2) {
            for (Entry e : dao.allActiveWarnings(now)) {
                out.add(encodeFull(dao, e, now));
                if (out.size() >= LIMIT) {
                    break;
                }
            }
        }

        return Json.array(out);
    }

    private static String byIp(SauverDAO dao, String ip) {
        if (ip == null || ip.isBlank()) {
            return "null";
        }
        long now = System.currentTimeMillis();
        List<String> out = new ArrayList<>();
        for (Entry e : dao.byIp(ip, LIMIT)) {
            out.add(encodeFull(dao, e, now));
        }
        return Json.array(out);
    }

    private static String names(SauverDAO dao) {
        List<String> out = new ArrayList<>();
        for (String name : dao.knownNames()) {
            out.add(Json.escape(name));
            if (out.size() >= 2000) {
                break;
            }
        }
        return Json.array(out);
    }

    private static Entry.Type parseType(String raw) {
        try {
            return Entry.Type.of(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static String encodeFull(SauverDAO dao, Entry e, long now) {
        String target = e.uuid() == null ? e.ip() : e.uuid().toString();
        String name = null;
        if (e.uuid() != null) {
            name = dao.nameOf(e.uuid());
        }

        return Json.object(
                "id", e.randomId(),
                "type", e.type().id(),
                "target", target,
                "targetName", name,
                "reason", e.reason(),
                "issuedBy", e.executorName(),
                "start", e.dateStart(),
                "end", e.permanent() ? -1L : e.dateEnd(),
                "permanent", e.permanent(),
                "active", e.inForce(now),
                "removedBy", e.removedByName(),
                "removalReason", e.removalReason(),
                "ip", e.ip(),
                "silent", e.silent()
        );
    }

    private static String player(SauverDAO dao, String target) {
        if (target == null || target.isBlank()) {
            return "null";
        }

        UUID uuid = null;
        String name = target;

        try {
            uuid = UUID.fromString(target);
            String known = dao.nameOf(uuid);
            if (known != null) {
                name = known;
            }
        } catch (IllegalArgumentException ignored) {
            uuid = dao.uuidByName(target);
        }

        if (uuid == null) {
            return Json.object("found", false, "name", target);
        }

        long now = System.currentTimeMillis();
        Entry ban = dao.activeBan(uuid);
        Entry mute = dao.activeMute(uuid);

        return Json.object(
                "found", true,
                "uuid", uuid.toString(),
                "name", name,
                "banned", ban != null && ban.inForce(now),
                "muted", mute != null && mute.inForce(now),
                "ips", Json.raw(strings(dao.ipsOf(uuid))),
                "names", Json.raw(strings(dao.namesOf(uuid)))
        );
    }

    private static String history(SauverDAO dao, String uuid) {
        if (uuid == null) {
            return "null";
        }
        UUID id;
        try {
            id = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return "null";
        }

        long now = System.currentTimeMillis();
        List<String> out = new ArrayList<>();
        for (Entry e : dao.history(id, LIMIT)) {
            out.add(encode(e, now));
        }
        return Json.array(out);
    }

    private static String punishment(SauverDAO dao, String uuid, String id) {
        if (uuid == null || id == null) {
            return "null";
        }
        UUID owner;
        try {
            owner = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            return "null";
        }

        long now = System.currentTimeMillis();
        for (Entry e : dao.history(owner, LIMIT)) {
            if (id.equalsIgnoreCase(e.randomId())) {
                return encode(e, now);
            }
        }
        return "null";
    }

    private static String online() {
        List<String> players = new ArrayList<>();
        try {
            for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
                players.add(Json.object("uuid", p.getUniqueId().toString(), "name", p.getName()));
            }
        } catch (Exception ignored) {
        }
        return Json.array(players);
    }

    private static String encode(Entry e, long now) {
        return Json.object(
                "id", e.randomId(),
                "type", e.type().id(),
                "reason", e.reason(),
                "issuedBy", e.executorName(),
                "start", e.dateStart(),
                "end", e.permanent() ? -1L : e.dateEnd(),
                "permanent", e.permanent(),
                "active", e.inForce(now),
                "removedBy", e.removedByName(),
                "removalReason", e.removalReason(),
                "ip", e.ip(),
                "silent", e.silent()
        );
    }

    private static String strings(List<String> values) {
        List<String> out = new ArrayList<>();
        for (String v : values) {
            out.add(Json.escape(v));
        }
        return Json.array(out);
    }

    private static SauverDAO dao() {
        Sauver s = Sauver.instance();
        return s == null ? null : s.dao();
    }
}
