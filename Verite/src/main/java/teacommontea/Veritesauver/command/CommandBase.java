package teacommontea.veritesauver.command;

import teacommontea.util.Colours;
import teacommontea.util.Complete;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.core.SauverDAO;
import teacommontea.veritesauver.core.SauverEngine;
import teacommontea.veritesauver.util.SauverMessages;

public abstract class CommandBase {

    protected static final String DEFAULT_REMOVE_REASON = "No reason specified.";

    protected final Sauver sauver;

    protected CommandBase(Sauver sauver) {
        this.sauver = sauver;
    }

    protected SauverMessages msg() {
        return sauver.messages();
    }

    protected SauverDAO dao() {
        return sauver.dao();
    }

    protected void send(CommandSender to, String body) {
        msg().send(to, SauverEngine.stripLegacy(body));
    }

    protected void err(CommandSender to, String body) {
        msg().err(to, SauverEngine.stripLegacy(body));
    }

    protected void raw(CommandSender to, String body) {
        msg().raw(to, SauverEngine.stripLegacy(body));
    }

    protected void usage(CommandSender to, String syntax, String desc) {
        send(to, Colours.BRAND_ACCENT_SECONDARY + "Usage: " + Colours.BRAND_ACCENT_SECONDARY + "/" + syntax + " " + Colours.BRAND_ACCENT_SECONDARY + "- " + desc);
    }

    protected record Parsed(String targetName, boolean silent, List<String> rest) {}

    protected static Parsed parse(String[] args) {
        boolean silent = false;
        List<String> kept = new ArrayList<>();
        for (String a : args) {
            if (a.equalsIgnoreCase("-s")) {
                silent = true;
            } else {
                kept.add(a);
            }
        }
        if (kept.isEmpty()) {
            return new Parsed(null, silent, List.of());
        }
        String target = kept.remove(0);
        return new Parsed(target, silent, kept);
    }

    protected static String executorName(CommandSender sender) {
        return sender instanceof Player p ? p.getName() : SauverEngine.CONSOLE_NAME;
    }

    protected static UUID executorUuid(CommandSender sender) {
        return sender instanceof Player p ? p.getUniqueId() : null;
    }

    protected UUID resolve(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return online.getUniqueId();
        }
        UUID known = dao().uuidByName(name);
        if (known != null) {
            return known;
        }
        OfflinePlayer off = offlinePlayerIfCached(name);
        return off == null ? null : off.getUniqueId();
    }

    private static final java.lang.reflect.Method OFFLINE_IF_CACHED = resolveOfflineIfCached();

    private static java.lang.reflect.Method resolveOfflineIfCached() {
        try {
            return Bukkit.class.getMethod("getOfflinePlayerIfCached", String.class);
        } catch (Throwable t) {
            return null;
        }
    }

    private static OfflinePlayer offlinePlayerIfCached(String name) {
        if (OFFLINE_IF_CACHED == null) {
            return null;
        }
        try {
            return (OfflinePlayer) OFFLINE_IF_CACHED.invoke(null, name);
        } catch (Throwable t) {
            return null;
        }
    }

    protected String bestName(UUID u, String fallback) {
        String n = dao().nameOf(u);
        if (n != null) {
            return n;
        }
        OfflinePlayer off = Bukkit.getOfflinePlayer(u);
        return off.getName() != null ? off.getName() : fallback;
    }

    protected void unknownPlayer(CommandSender sender, String name) {
        err(sender, Colours.WARNING + "That player is unknown: " + Colours.BRAND_ACCENT_SECONDARY + name + Colours.WARNING + ".");
    }

    protected UUID lookupTarget(CommandSender sender, String[] args, String cmd) {
        if (args.length == 0) {
            usage(sender, cmd + " <player>", "look up a player");
            return null;
        }
        UUID u = resolve(args[0]);
        if (u == null) {
            unknownPlayer(sender, args[0]);
        }
        return u;
    }

    protected static boolean isIpLiteral(String s) {
        return s.matches("\\d{1,3}(\\.\\d{1,3}){3}") || s.contains(":");
    }

    protected static int parseIntOr(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    protected static final String ALL_STAR = "*";
    protected static final String ALL_WORD = "all";

    protected static boolean isAllTarget(String name) {
        return name != null && (name.equals(ALL_STAR) || name.equalsIgnoreCase(ALL_WORD));
    }

    protected List<UUID> expandTargets(String name) {
        if (isAllTarget(name)) {
            List<UUID> out = new ArrayList<>();
            for (Player pl : Bukkit.getOnlinePlayers()) {
                out.add(pl.getUniqueId());
            }
            return out;
        }
        UUID one = resolve(name);
        return one == null ? List.of() : List.of(one);
    }

    protected static List<String> matchOnline(String prefix) {
        return Complete.onlineNames(prefix);
    }

    protected static List<String> matchOnlineOrAll(String prefix) {
        List<String> out = withAll(prefix);
        out.addAll(matchOnline(prefix));
        return out;
    }

    protected static List<String> withAll(String prefix) {
        String p = prefix.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        if (ALL_STAR.startsWith(p)) {
            out.add(ALL_STAR);
        }
        if (ALL_WORD.startsWith(p)) {
            out.add(ALL_WORD);
        }
        return out;
    }

    protected static List<String> matchNamesOrAll(List<String> names, String prefix) {
        List<String> out = withAll(prefix);
        out.addAll(Complete.prefix(names, prefix));
        return out;
    }

    protected static List<String> prefixed(List<String> options, String prefix) {
        return Complete.prefix(options, prefix);
    }
}
