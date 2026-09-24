package teacommontea.dashboard;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.plugin.Plugin;

import teacommontea.util.ConsoleColours;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverEngine;

public final class DashboardChanges {

    private DashboardChanges() {}

    public static void apply(Plugin plugin, String changeJson, String actor, String actorUuid)
            throws Exception {
        Map<String, String> envelope = Json.flat(changeJson);
        String payload = envelope.get("payload");

        String source = payload != null ? payload : changeJson;
        List<String> actions = Json.objects(extractArray(source, "actions"));

        if (actions.isEmpty()) {
            actions = List.of(source);
        }

        for (String action : actions) {
            Map<String, String> parsed = Json.flat(action);
            parsed.putIfAbsent("byName", actor);
            parsed.putIfAbsent("byUuid", actorUuid);

            if (needsMainThread(parsed.get("action"))) {
                onMainThread(plugin, parsed);
            } else {
                applyOne(plugin, parsed);
            }
            record(plugin, parsed, actor);
        }
    }

    private static void record(Plugin plugin, Map<String, String> action, String actor) {
        String kind = action.get("action");
        if (kind == null) {
            return;
        }

        String who = action.getOrDefault("name", action.getOrDefault("uuid", "someone"));
        String detail = switch (kind) {
            case "punish" -> action.getOrDefault("type", "punished") + " " + who
                    + " for " + action.getOrDefault("reason", "no reason given");
            case "pardon" -> "removed a " + action.getOrDefault("type", "punishment")
                    + " from " + who;
            case "kick" -> "kicked " + who;
            case "vanish" -> ("true".equals(action.get("value")) ? "vanished " : "unvanished ") + who;
            case "gamemode" -> "set " + who + " to " + action.getOrDefault("value", "a gamemode");
            case "config-set" -> "set " + action.getOrDefault("path", "a setting")
                    + " to " + action.getOrDefault("value", "a value");
            case "config" -> teacommontea.util.Lang.of("dashboard.config.replaced");
            default -> kind;
        };

        plugin.getLogger().info(ConsoleColours.note("Dashboard: ")
                + ConsoleColours.value(actor)
                + ConsoleColours.note(" " + detail + "."));
    }

    private static boolean needsMainThread(String kind) {
        return "punish".equals(kind)
                || "pardon".equals(kind)
                || "kick".equals(kind)
                || "vanish".equals(kind)
                || "gamemode".equals(kind);
    }

    private static void onMainThread(Plugin plugin, Map<String, String> action) throws Exception {
        java.util.concurrent.CompletableFuture<Exception> done =
                new java.util.concurrent.CompletableFuture<>();

        teacommontea.util.sched.Sched.executeGlobal(() -> {
            try {
                applyOne(plugin, action);
                done.complete(null);
            } catch (Exception e) {
                done.complete(e);
            }
        });

        Exception failure;
        try {
            failure = done.get(15, java.util.concurrent.TimeUnit.SECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new IllegalStateException(teacommontea.util.Lang.of("dashboard.change.timeout"));
        }

        if (failure != null) {
            throw failure;
        }
    }

    private static void applyOne(Plugin plugin, Map<String, String> action) throws Exception {
        String kind = action.get("action");
        if (kind == null) {
            return;
        }

        switch (kind) {
            case "punish" -> punish(action);
            case "pardon" -> pardon(action);
            case "config" -> config(plugin, action);
            case "config-set" -> configSet(plugin, action);
            case "vanish" -> vanish(action);
            case "gamemode" -> gamemode(action);
            case "kick" -> kick(action);
            default -> plugin.getLogger().warning(ConsoleColours.bad(
                    "The dashboard asked for an unknown change: " + kind));
        }
    }

    private static void punish(Map<String, String> action) {
        Entry.Type type = parseType(action.get("type"));
        if (type == null) {
            throw new IllegalArgumentException(teacommontea.util.Lang.of("dashboard.punish.type.unknown"));
        }

        UUID target = parseUuid(action.get("uuid"));
        String ip = action.get("ip");
        if (target == null && (ip == null || ip.isBlank())) {
            throw new IllegalArgumentException(teacommontea.util.Lang.of("dashboard.punish.target.missing"));
        }

        SauverEngine.Result result = SauverEngine.issue(
                type,
                target,
                ip,
                action.getOrDefault("name", "unknown"),
                action.getOrDefault("reason", "No reason given"),
                parseUuid(action.get("byUuid")),
                action.getOrDefault("byName", "Dashboard"),
                parseLong(action.get("duration"), 0L),
                Boolean.parseBoolean(action.getOrDefault("silent", "false")),
                Boolean.parseBoolean(action.getOrDefault("ipban", "false"))
        );

        fail(result);
    }

    private static void pardon(Map<String, String> action) {
        Entry.Type type = parseType(action.get("type"));
        UUID target = parseUuid(action.get("uuid"));
        if (type == null || target == null) {
            throw new IllegalArgumentException(teacommontea.util.Lang.of("dashboard.pardon.incomplete"));
        }

        fail(SauverEngine.pardon(
                type,
                target,
                action.getOrDefault("name", "unknown"),
                parseUuid(action.get("byUuid")),
                action.getOrDefault("byName", "Dashboard"),
                action.getOrDefault("reason", teacommontea.util.Lang.of("dashboard.pardon.source"))
        ));
    }

    private static void vanish(Map<String, String> action) {
        UUID target = parseUuid(action.get("uuid"));
        if (target == null) {
            throw new IllegalArgumentException(teacommontea.util.Lang.of("dashboard.vanish.target.missing"));
        }
        boolean on = Boolean.parseBoolean(action.getOrDefault("value", "false"));
        if (!DashboardState.vanish(target, on)) {
            throw new IllegalArgumentException("That player is not online.");
        }
    }

    private static void gamemode(Map<String, String> action) {
        UUID target = parseUuid(action.get("uuid"));
        if (target == null) {
            throw new IllegalArgumentException(teacommontea.util.Lang.of("dashboard.gamemode.target.missing"));
        }
        if (!DashboardState.gamemode(target, action.get("value"))) {
            throw new IllegalArgumentException(teacommontea.util.Lang.of("dashboard.gamemode.unknown"));
        }
    }

    private static void kick(Map<String, String> action) {
        UUID target = parseUuid(action.get("uuid"));
        if (target == null) {
            throw new IllegalArgumentException(teacommontea.util.Lang.of("dashboard.kick.target.missing"));
        }

        fail(SauverEngine.kick(
                target,
                action.getOrDefault("name", "unknown"),
                action.getOrDefault("reason", teacommontea.util.Lang.of("dashboard.kick.source")),
                parseUuid(action.get("byUuid")),
                action.getOrDefault("byName", "Dashboard"),
                Boolean.parseBoolean(action.getOrDefault("silent", "false"))
        ));
    }

    private static void configSet(Plugin plugin, Map<String, String> action) throws Exception {
        String path = action.get("path");
        String value = action.get("value");
        if (path == null || value == null) {
            throw new IllegalArgumentException(teacommontea.util.Lang.of("dashboard.config.incomplete"));
        }
        if (!DashboardConfig.set(plugin, path, value)) {
            throw new IllegalArgumentException("There is no config key called " + path + ".");
        }
        reload(plugin);
    }

    private static void config(Plugin plugin, Map<String, String> action) throws Exception {
        String body = action.get("body");
        if (body == null) {
            return;
        }

        File target = new File(plugin.getDataFolder(), "config.yml");
        File backup = new File(plugin.getDataFolder(), "config.yml.bak");

        if (target.isFile()) {
            Files.copy(target.toPath(), backup.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        Files.write(target.toPath(), body.getBytes(StandardCharsets.UTF_8));
        reload(plugin);
    }

    private static void reload(Plugin plugin) {
        teacommontea.util.sched.Sched.executeGlobal(() -> {
            plugin.getLogger().info(ConsoleColours.ok(
                    teacommontea.util.Lang.of("dashboard.config.reloading")));
            try {
                plugin.getServer().dispatchCommand(
                        plugin.getServer().getConsoleSender(), "verite reload");
            } catch (Exception ignored) {
            }
        });
    }

    private static void fail(SauverEngine.Result result) {
        if (result != null && !result.ok()) {
            throw new IllegalArgumentException(result.error());
        }
    }

    private static String extractArray(String json, String key) {
        String needle = "\"" + key + "\"";
        int at = json.indexOf(needle);
        if (at < 0) {
            return "";
        }
        int open = json.indexOf('[', at);
        if (open < 0) {
            return "";
        }
        int depth = 0;
        boolean inString = false;
        for (int i = open; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '[') {
                depth++;
            } else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return json.substring(open, i + 1);
                }
            }
        }
        return "";
    }

    private static Entry.Type parseType(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Entry.Type.of(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static UUID parseUuid(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static long parseLong(String raw, long fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
