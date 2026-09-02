package teacommontea.api;

import java.util.List;
import java.util.UUID;

import teacommontea.api.internal.ApiBridge;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.core.SauverDAO;

public final class VeritePlayerData {

    private VeritePlayerData() {}

    private static SauverDAO dao() {
        Sauver s = Sauver.instance();
        return s == null ? null : s.dao();
    }

    public static boolean enabled() {
        return dao() != null;
    }

    public static boolean hasProfile(UUID player) {
        SauverDAO d = dao();
        return d != null && d.hasProfile(player);
    }

    public static PlayerProfile profile(UUID player) {
        SauverDAO d = dao();
        return d == null ? null : ApiBridge.toProfile(d.profile(player));
    }

    public static UUID uuidByName(String name) {
        SauverDAO d = dao();
        return d == null ? null : d.uuidByName(name);
    }

    public static String nameOf(UUID player) {
        SauverDAO d = dao();
        return d == null ? null : d.nameOf(player);
    }

    public static List<String> knownNames() {
        SauverDAO d = dao();
        return d == null ? List.of() : d.knownNames();
    }

    public static List<String> namesOf(UUID player) {
        SauverDAO d = dao();
        return d == null ? List.of() : d.namesOf(player);
    }

    public static List<String> ipsOf(UUID player) {
        SauverDAO d = dao();
        return d == null ? List.of() : d.ipsOf(player);
    }

    public static List<UUID> usersOfIp(String ip) {
        SauverDAO d = dao();
        return d == null ? List.of() : d.usersOfIp(ip);
    }

    public static List<String> clientsOf(UUID player) {
        SauverDAO d = dao();
        return d == null ? List.of() : d.clientsOf(player);
    }

    public static void recordLogin(UUID player, String name, String ip, long now) {
        SauverDAO d = dao();
        if (d != null) {
            d.recordLogin(player, name, ip, now);
        }
    }

    public static void recordClient(UUID player, String brand, int protocol, String referrer, long now) {
        SauverDAO d = dao();
        if (d != null) {
            d.recordClient(player, brand, protocol, referrer, now);
        }
    }

    public static void flushSession(UUID player, long now) {
        SauverDAO d = dao();
        if (d != null) {
            d.flushSession(player, now);
        }
    }

    public static void recordLogout(UUID player, long now) {
        SauverDAO d = dao();
        if (d != null) {
            d.recordLogout(player, now);
        }
    }
}
