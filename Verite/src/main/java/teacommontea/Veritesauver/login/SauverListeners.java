package teacommontea.veritesauver.login;

import teacommontea.util.Colours;
import teacommontea.veritesauver.util.SauverFormat;
import teacommontea.veritesauver.util.SauverMessages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.util.SauverConfig;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverEngine;
import teacommontea.veritesauver.core.SauverDAO;
import teacommontea.veritesauver.lockdown.SauverLockdown;
import teacommontea.veritesauver.chat.SauverChat;

public final class SauverListeners implements Listener {

    private final Sauver sauver;

    public SauverListeners(Sauver sauver) {
        this.sauver = sauver;
        teacommontea.util.chat.ChatRouter.register(EventPriority.HIGH, true, event -> {
            if (off()) return;
            Player p = event.sender();
            SauverChat chat = sauver.chat();
            if (chat.mutedGate(p) || chat.slowmodeGate(p)) {
                event.setCancelled(true);
            }
        });
    }

    private SauverDAO dao() {
        return sauver.dao();
    }

    private static boolean off() {
        return !SauverConfig.moderationEnabled();
    }

    private final java.util.Map<UUID, String> pendingRealIp = new java.util.concurrent.ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (off()) return;
        UUID uuid = event.getUniqueId();
        String ip = realClientIp(event);
        if (ip != null) {
            pendingRealIp.put(uuid, ip);
        }
        long now = System.currentTimeMillis();

        Entry block = resolveLoginBan(uuid, ip, now);
        if (block != null) {
            teacommontea.util.text.Server.disallow(event, AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    teacommontea.util.text.Text.toLegacy(SauverEngine.banScreen(block)));
            notifyBannedJoin(event.getName(), block, now);
            return;
        }

        if (SauverLockdown.active() && !hasOfflineBypass(uuid)) {
            teacommontea.util.text.Server.disallow(event, AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                    teacommontea.util.text.Text.toLegacy(Colours.WARNING + "<bold>Server locked down.</bold><newline><newline>" + Colours.BRAND_ACCENT_SECONDARY
                            + SauverLockdown.reason()));
        }
    }

    private boolean hasOfflineBypass(UUID uuid) {
        try {
            Class<?> provider = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object api = provider.getMethod("get").invoke(null);
            Object userManager = api.getClass().getMethod("getUserManager").invoke(api);
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, uuid);
            if (user == null) {
                return false;
            }
            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object permData = cachedData.getClass().getMethod("getPermissionData").invoke(cachedData);
            Object result = permData.getClass().getMethod("checkPermission", String.class)
                    .invoke(permData, "veritesauver.lockdown.bypass");
            Object asBool = result.getClass().getMethod("asBoolean").invoke(result);
            return asBool instanceof Boolean b && b;
        } catch (Throwable t) {
            return false;
        }
    }

    private Entry resolveLoginBan(UUID uuid, String ip, long now) {
        Entry own = dao().activeBan(uuid);
        if (own != null && own.inForce(now)) {
            return own;
        }
        Entry ipBan = dao().activeIpPunishment(Entry.Type.BAN, ip, now);
        if (ipBan != null) {
            return ipBan;
        }
        if (SauverConfig.banAlts()) {
            Entry alt = dao().bannedAltOnIp(uuid, ip, now);
            if (alt != null) {
                return alt;
            }
        }
        return null;
    }

    private void notifyBannedJoin(String name, Entry block, long now) {
        if (name == null || block == null) {
            return;
        }
        String duration = block.permanent()
                ? Colours.WARNING + "permanently banned"
                : Colours.BRAND_ACCENT_SECONDARY + "banned for " + Colours.WARNING + SauverFormat.fancyTime(block.remaining(now));
        String head = Colours.WARNING + "⚠ " + Colours.BRAND_ACCENT_SECONDARY + name + " " + Colours.BRAND_ACCENT_SECONDARY + "tried to join, but is " + duration;
        teacommontea.util.sched.Sched.executeGlobal(() ->
                sauver.messages().notify("veritesauver.notify.banned_join", head));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (off()) return;
        Player p = event.getPlayer();

        String ip = pendingRealIp.remove(p.getUniqueId());
        if (ip == null) {
            ip = p.getAddress() == null || p.getAddress().getAddress() == null
                    ? null : p.getAddress().getAddress().getHostAddress();
        }

        dao().recordLogin(p.getUniqueId(), p.getName(), ip, System.currentTimeMillis());
        recordClientDetails(p);
        notifyDupeIp(p, ip);
    }

    private static String realClientIp(AsyncPlayerPreLoginEvent event) {
        String injected = parseForwardedHost(event.getHostname());
        if (injected != null) {
            return injected;
        }
        return event.getAddress() == null ? null : event.getAddress().getHostAddress();
    }

    private static String parseForwardedHost(String hostname) {
        if (hostname == null || !hostname.contains("///")) {
            return null;
        }
        String[] parts = hostname.split("///");
        if (parts.length < 2) {
            return null;
        }
        String candidate = parts[1].trim();
        int colon = candidate.lastIndexOf(':');
        if (colon > 0 && candidate.indexOf(':') == colon) {

            candidate = candidate.substring(0, colon);
        }
        return candidate.isEmpty() ? null : candidate;
    }

    private void recordClientDetails(Player p) {
        int protocol = protocolOf(p);
        String referrer = virtualHostOf(p);
        dao().recordClient(p.getUniqueId(), null, protocol, referrer, System.currentTimeMillis());

        teacommontea.veritesauver.client.ClientDetect detect = sauver.clientDetect();
        if (detect == null) {
            return;
        }
        detect.inspect(p, profile -> {
            describe(p, profile);
            dao().recordClientProfile(p.getUniqueId(), profile, System.currentTimeMillis());
            if (teacommontea.veritesauver.client.ClientNotice.worthReporting(profile)) {
                sauver.messages().notify("veritesauver.notify.client_join",
                        teacommontea.veritesauver.client.ClientNotice.line(p, profile));
            }
        });
    }

    private void describe(Player p, teacommontea.veritesauver.client.ClientProfile profile) {
        int protocol = protocolOf(p);
        if (protocol > 0) {
            profile.fact("protocol", teacommontea.util.Lang.of("login.protocol", "protocol", protocol));
        }
        String version = teacommontea.veritesauver.util.SauverProtocol.versionName(protocol);
        if (version != null && !version.isBlank()) {
            profile.fact("version", teacommontea.util.Lang.of("login.version", "version", version));
        }
        String referrer = virtualHostOf(p);
        if (referrer != null && !referrer.isBlank()) {
            profile.fact("host", teacommontea.util.Lang.of("login.connected.via", "host", referrer));
        }
        profile.fact("locale", teacommontea.util.Lang.of("login.locale", "locale", localeOf(p)));
    }

    private static String localeOf(Player p) {
        try {
            Object l = Player.class.getMethod("locale").invoke(p);
            if (l != null) {
                return String.valueOf(l);
            }
        } catch (Throwable ignored) {
        }
        try {
            Object l = Player.class.getMethod("getLocale").invoke(p);
            return l == null ? "unknown" : String.valueOf(l);
        } catch (Throwable t) {
            return "unknown";
        }
    }

    private static int protocolOf(Player p) {
        try {
            Object v = Player.class.getMethod("getProtocolVersion").invoke(p);
            return v instanceof Integer i ? i : 0;
        } catch (Throwable t) {
            return 0;
        }
    }

    private static String virtualHostOf(Player p) {
        try {
            Object host = Player.class.getMethod("getVirtualHost").invoke(p);
            if (host instanceof java.net.InetSocketAddress addr) {
                return addr.getHostString() + ":" + addr.getPort();
            }
            return host == null ? null : String.valueOf(host);
        } catch (Throwable t) {
            return null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        if (off()) return;
        UUID u = event.getPlayer().getUniqueId();
        pendingRealIp.remove(u);
        teacommontea.veritesauver.client.ClientDetect detect = sauver.clientDetect();
        if (detect != null) {
            detect.cancel(event.getPlayer());
            detect.forget(u);
        }
        dao().recordLogout(u, System.currentTimeMillis());
    }

    private void notifyDupeIp(Player joining, String ip) {
        if (ip == null) {
            return;
        }
        long now = System.currentTimeMillis();
        List<UUID> shared = dao().usersOfIp(ip);
        List<String> alts = new ArrayList<>();
        boolean flagged = false;
        for (UUID other : shared) {
            if (other.equals(joining.getUniqueId())) {
                continue;
            }
            String name = dao().nameOf(other);
            if (name == null) {
                name = other.toString().substring(0, 8);
            }
            if (name.equalsIgnoreCase(joining.getName())) {
                continue;
            }
            Entry ban = dao().activeBan(other);
            Entry mute = dao().activeMute(other);
            boolean banned = ban != null && ban.inForce(now);
            boolean muted = mute != null && mute.inForce(now);
            if (banned || muted) {
                flagged = true;
                alts.add(Colours.WARNING + name + (banned ? " (banned)" : " (muted)"));
            } else {
                alts.add(Colours.BRAND_ACCENT_SECONDARY + name);
            }
        }
        if (alts.isEmpty()) {
            return;
        }
        String head = teacommontea.util.Lang.of(flagged ? "login.dupeip.notice.flagged" : "login.dupeip.notice",
                "name", joining.getName(),
                "alts", String.join(Colours.BRAND_ACCENT_SECONDARY + ", ", alts));
        sauver.messages().notify("veritesauver.notify.dupeip_join", head);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onMutedCommand(PlayerCommandPreprocessEvent event) {
        if (off()) return;
        Player p = event.getPlayer();
        Entry mute = sauver.activeMute(p.getUniqueId());
        if (mute == null) {
            return;
        }
        if (isBlockedWhileMuted(event.getMessage())) {
            event.setCancelled(true);
            sauver.messages().send(p, SauverEngine.muteNotice(mute));
        }
    }

    private static boolean isBlockedWhileMuted(String message) {
        String line = message.startsWith("/") ? message.substring(1) : message;
        int sp = line.indexOf(' ');
        String word = (sp < 0 ? line : line.substring(0, sp)).toLowerCase(Locale.ROOT);
        if (word.isEmpty()) {
            return false;
        }
        teacommontea.veritedoux.util.Eve eve = compiledBlacklist();
        return eve != null && !eve.scan(word, word).isEmpty();
    }

    private static List<String> blacklistSource;
    private static teacommontea.veritedoux.util.Eve blacklistCompiled;

    private static String blacklistStatement(String entry) {
        String t = entry == null ? "" : entry.trim();
        if (t.isEmpty()) {
            return "";
        }
        String lower = t.toLowerCase();
        for (String verb : new String[]{"eve:", "hear ", "find ", "match ", "realm ", "let ", "define "}) {
            if (lower.startsWith(verb)) {
                return t;
            }
        }
        return "hear rule as [+[($^):]]" + t + "[^^]";
    }

    private static synchronized teacommontea.veritedoux.util.Eve compiledBlacklist() {
        List<String> source = SauverConfig.muteCommandBlacklist();
        if (source.equals(blacklistSource)) {
            return blacklistCompiled;
        }
        blacklistSource = new ArrayList<>(source);
        if (!teacommontea.veritedoux.util.Eve.nativeAvailable()) {
            blacklistCompiled = null;
            return null;
        }
        StringBuilder src = new StringBuilder();
        for (String stmt : source) {
            src.append(blacklistStatement(stmt)).append('\n');
        }
        try {
            blacklistCompiled = teacommontea.veritedoux.util.Eve.parse(src.toString());
        } catch (Throwable t) {
            Sauver s = Sauver.instance();
            if (s != null && s.plugin() != null) {
                s.plugin().getLogger().warning(teacommontea.util.ConsoleColours.bad(teacommontea.util.Lang.of("login.blacklist.failed")) + teacommontea.util.Trace.of(t));
            }
            blacklistCompiled = null;
        }
        return blacklistCompiled;
    }
}
