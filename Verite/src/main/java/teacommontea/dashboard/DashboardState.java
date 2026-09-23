package teacommontea.dashboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import teacommontea.api.VeriteVanish;

public final class DashboardState {

    private DashboardState() {}

    public static String players() {
        return players(null);
    }

    public static String players(UUID leaving) {
        List<String> out = new ArrayList<>();
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (leaving != null && leaving.equals(p.getUniqueId())) {
                    continue;
                }
                out.add(encode(p));
            }
        } catch (Exception ignored) {
        }
        return Json.array(out);
    }

    public static String encode(Player p) {
        String ip = null;
        try {
            if (p.getAddress() != null && p.getAddress().getAddress() != null) {
                ip = p.getAddress().getAddress().getHostAddress();
            }
        } catch (Exception ignored) {
        }

        return Json.object(
                "uuid", p.getUniqueId().toString(),
                "name", p.getName(),
                "vanished", VeriteVanish.isVanished(p),
                "op", p.isOp(),
                "gamemode", p.getGameMode().name().toLowerCase(java.util.Locale.ROOT),
                "world", p.getWorld() == null ? null : p.getWorld().getName(),
                "health", Math.round(p.getHealth()),
                "level", p.getLevel(),
                "ping", ping(p),
                "ip", ip
        );
    }

    public static String server(Plugin plugin) {
        return server(plugin, null);
    }

    public static String server(Plugin plugin, UUID leaving) {
        Runtime rt = Runtime.getRuntime();
        long mb = 1024L * 1024L;

        int online = Bukkit.getOnlinePlayers().size();
        if (leaving != null && Bukkit.getPlayer(leaving) != null) {
            online--;
        }

        return Json.object(
                "name", plugin.getServer().getName(),
                "minecraft", plugin.getServer().getVersion(),
                "verite", plugin.getDescription().getVersion(),
                "online", online,
                "max", Bukkit.getMaxPlayers(),
                "vanished", vanishedCount(leaving),
                "vanishEnabled", VeriteVanish.enabled(),
                "memoryUsed", (rt.totalMemory() - rt.freeMemory()) / mb,
                "memoryMax", rt.maxMemory() / mb,
                "tps", tps(),
                "uptime", uptime(),
                "startedAt", startedAt()
        );
    }

    private static int vanishedCount(UUID leaving) {
        int n = VeriteVanish.getVanished().size();
        if (leaving != null && VeriteVanish.isVanished(leaving)) {
            n--;
        }
        return Math.max(0, n);
    }

    public static boolean vanish(UUID uuid, boolean on) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            return false;
        }
        return on ? VeriteVanish.vanish(p) : VeriteVanish.unvanish(p);
    }

    public static boolean gamemode(UUID uuid, String mode) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null || mode == null) {
            return false;
        }
        try {
            p.setGameMode(org.bukkit.GameMode.valueOf(
                    mode.toUpperCase(java.util.Locale.ROOT)));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static int ping(Player p) {
        try {
            return p.getPing();
        } catch (Throwable t) {
            return -1;
        }
    }

    private static String tps() {
        try {
            double[] recent = Bukkit.getServer().getTPS();
            if (recent.length > 0) {
                return String.format(java.util.Locale.ROOT, "%.2f",
                        Math.min(20.0D, recent[0]));
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static long uptime() {
        try {
            return java.lang.management.ManagementFactory
                    .getRuntimeMXBean().getUptime();
        } catch (Throwable t) {
            return -1L;
        }
    }

    private static long startedAt() {
        try {
            return java.lang.management.ManagementFactory
                    .getRuntimeMXBean().getStartTime();
        } catch (Throwable t) {
            return -1L;
        }
    }
}
