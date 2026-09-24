package teacommontea.dashboard;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import teacommontea.util.Colours;
import teacommontea.util.ConsoleColours;
import teacommontea.util.Trace;

public final class Dashboard {

    private static final String DEFAULT_BASE = "https://verite.gg";

    private static final Map<UUID, DashboardSession> SESSIONS = new ConcurrentHashMap<>();
    private static final long SWEEP_MS = 60L * 1000L;

    private static DashboardKeys keys;
    private static Plugin owner;
    private static teacommontea.util.sched.TaskHandle sweeper;

    private Dashboard() {}

    private static void msg(CommandSender to, String tagged) {
        teacommontea.util.text.Text.send(to,
                teacommontea.util.Messages.prefix() + " " + tagged);
    }

    public static void load(Plugin plugin) {
        try {
            keys = DashboardKeys.load(plugin.getDataFolder());
            owner = plugin;
            plugin.getServer().getPluginManager()
                    .registerEvents(new DashboardListener(plugin), plugin);
            sweeper = teacommontea.util.sched.Sched.executeAsyncRepeating(
                    () -> sweep(plugin), SWEEP_MS, SWEEP_MS);
        } catch (Exception e) {
            keys = null;
            plugin.getLogger().warning(ConsoleColours.bad(
                    teacommontea.util.Lang.of("dashboard.keys.failed"))
                    + Trace.of(e));
        }
    }

    private static void sweep(Plugin plugin) {
        if (SESSIONS.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, DashboardSession> e : SESSIONS.entrySet()) {
            DashboardSession s = e.getValue();
            if (!s.expired()) {
                continue;
            }
            SESSIONS.remove(e.getKey());
            s.close();
            plugin.getLogger().info(ConsoleColours.note(teacommontea.util.Lang.of("dashboard.session.expired.console",
                    "name", ConsoleColours.value(s.ownerName()))));
            teacommontea.util.sched.Sched.executeGlobal(() -> {
                Player p = plugin.getServer().getPlayer(e.getKey());
                if (p != null) {
                    msg(p, Colours.BRAND_ACCENT_SECONDARY
                            + teacommontea.util.Lang.of("dashboard.session.expired"));
                }
            });
        }
    }

    public static void shutdown() {
        if (sweeper != null) {
            sweeper.cancel();
            sweeper = null;
        }
        for (DashboardSession s : SESSIONS.values()) {
            s.close();
        }
        SESSIONS.clear();
    }

    public static void changed(Plugin plugin) {
        if (SESSIONS.isEmpty()) {
            return;
        }
        teacommontea.util.sched.Sched.executeGlobal(() -> {
            String players = DashboardState.players(null);
            String state = DashboardState.server(plugin, null);
            String active = DashboardQueries.activePunishments();

            for (DashboardSession s : SESSIONS.values()) {
                s.pushState(players, state, active);
            }
        });
    }

    public static void playersChanged(Plugin plugin) {
        playersChanged(plugin, null);
    }

    public static void playersChanged(Plugin plugin, UUID leaving) {
        if (SESSIONS.isEmpty()) {
            return;
        }

        String players = DashboardState.players(leaving);
        String state = DashboardState.server(plugin, leaving);

        for (DashboardSession s : SESSIONS.values()) {
            s.pushState(players, state, null);
        }
    }

    public static void open(Plugin plugin, CommandSender sender) {
        if (keys == null) {
            msg(sender,
                    teacommontea.util.Lang.of("dashboard.unsupported"));
            return;
        }
        if (!(sender instanceof Player player)) {
            msg(sender,
                    teacommontea.util.Lang.of("dashboard.request.ingame"));
            return;
        }

        DashboardSession existing = SESSIONS.remove(player.getUniqueId());
        if (existing != null) {
            existing.close();
        }

        msg(sender,
                teacommontea.util.Lang.of("dashboard.preparing"));

        String base = base(plugin);
        String serverName = plugin.getServer().getName();
        String snapshot = DashboardSnapshot.build(plugin, serverName);

        teacommontea.util.sched.Sched.executeAsync(() -> {
            DashboardSession session = new DashboardSession(
                    plugin, keys, base, player.getUniqueId(), player.getName());
            try {
                String key = session.start(serverName, snapshot);
                SESSIONS.put(player.getUniqueId(), session);

                String url = base + "/dashboard/" + key;
                plugin.getLogger().info(ConsoleColours.note(teacommontea.util.Lang.of("dashboard.session.opened",
                        "name", ConsoleColours.value(player.getName()))));
                teacommontea.util.sched.Sched.executeGlobal(() -> {
                    msg(player,
                            teacommontea.util.Lang.of("dashboard.ready", "url", url));
                    teacommontea.util.text.Text.send(player,
                            Colours.BRAND + "<click:open_url:'" + url + "'>"
                            + url + "</click>");
                });
            } catch (Exception e) {
                session.close();
                plugin.getLogger().warning(ConsoleColours.bad(
                        teacommontea.util.Lang.of("dashboard.open.failed")) + Trace.of(e));
                teacommontea.util.sched.Sched.executeGlobal(() ->
                        msg(player,
                                teacommontea.util.Lang.of("dashboard.connect.failed")));
            }
        });
    }

    public static void trust(CommandSender sender, String nonce) {
        DashboardSession session = null;
        for (DashboardSession s : SESSIONS.values()) {
            if (s.nonce() != null && !s.trusted()) {
                session = s;
                break;
            }
        }

        if (session == null) {
            msg(sender,
                    teacommontea.util.Lang.of("dashboard.not.awaiting"));
            return;
        }
        if (session.approvalExpired()) {
            msg(sender,
                    teacommontea.util.Lang.of("dashboard.approval.expired"));
            return;
        }
        if (!session.nonce().equals(nonce)) {
            msg(sender,
                    teacommontea.util.Lang.of("dashboard.approval.mismatch"));
            return;
        }

        session.approve(sender.getName());
        if (owner != null) {
            session.pushState(DashboardState.players(), DashboardState.server(owner),
                    DashboardQueries.activePunishments());
        }
        msg(sender,
                teacommontea.util.Lang.of("dashboard.trusted"));
    }

    public static void close(CommandSender sender) {
        if (SESSIONS.isEmpty()) {
            msg(sender,
                    teacommontea.util.Lang.of("dashboard.none.active"));
            return;
        }
        for (DashboardSession s : SESSIONS.values()) {
            s.close();
        }
        SESSIONS.clear();
        msg(sender,
                teacommontea.util.Lang.of("dashboard.closed"));
    }

    private static String base(Plugin plugin) {
        try {
            java.io.File file = new java.io.File(plugin.getDataFolder(), "config.yml");
            String configured = teacommontea.util.Yaml.loadYaml(file)
                    .getString("general.dashboard.url", null);
            if (configured != null && !configured.isBlank()) {
                return configured.trim();
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_BASE;
    }
}
