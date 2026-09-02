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
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED,
                    teacommontea.util.text.Text.toLegacy(SauverEngine.banScreen(block)));
            notifyBannedJoin(event.getName(), block, now);
            return;
        }

        if (SauverLockdown.active() && !hasOfflineBypass(uuid)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
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

    private static final long BRAND_POLL_TICKS = 20L;
    private static final int BRAND_POLL_MAX_ATTEMPTS = 15;

    private void recordClientDetails(Player p) {
        int protocol = protocolOf(p);
        String referrer = virtualHostOf(p);
        dao().recordClient(p.getUniqueId(), null, protocol, referrer, System.currentTimeMillis());
        pollClientBrand(p, 1);
    }

    private void pollClientBrand(Player p, int attempt) {
        teacommontea.util.sched.Sched.executeFor(p, () -> {
            if (!p.isOnline()) {
                return;
            }
            String brand = p.getClientBrandName();
            boolean known = brand != null && !brand.isBlank();
            if (!known && attempt < BRAND_POLL_MAX_ATTEMPTS) {
                pollClientBrand(p, attempt + 1);
                return;
            }
            String resolved = known ? brand : "vanilla";
            dao().recordClient(p.getUniqueId(), resolved, 0, null, System.currentTimeMillis());
            notifyClientBrand(p, resolved);
        }, BRAND_POLL_TICKS);
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
        String head = (flagged ? Colours.WARNING + "⚠ " : Colours.BRAND_ACCENT_SECONDARY) + Colours.BRAND_ACCENT_SECONDARY + joining.getName()
                + " " + Colours.BRAND_ACCENT_SECONDARY + "shares an IP with: " + String.join(Colours.BRAND_ACCENT_SECONDARY + ", ", alts);
        sauver.messages().notify("veritesauver.notify.dupeip_join", head);
    }

    private void notifyClientBrand(Player joining, String brand) {
        if (brand == null || brand.equalsIgnoreCase("vanilla")) {
            return;
        }
        String head = Colours.BRAND_ACCENT_SECONDARY + joining.getName()
                + " " + Colours.BRAND_ACCENT_SECONDARY + "joined using " + Colours.BRAND + prettyBrand(brand) + Colours.BRAND_ACCENT_SECONDARY + ".";
        sauver.messages().notify("veritesauver.notify.client_join", head);
    }

    private static String prettyBrand(String brand) {
        if (brand.isEmpty()) {
            return brand;
        }
        return Character.toUpperCase(brand.charAt(0)) + brand.substring(1);
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
                s.plugin().getLogger().warning("[Veritesauver] mute-blacklist EVE parse failed: " + t.getMessage());
            }
            blacklistCompiled = null;
        }
        return blacklistCompiled;
    }
}
