package teacommontea.veriteproxy;

import teacommontea.util.Colours;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverDAO;
import teacommontea.veritesauver.util.SauverFormat;

import java.util.UUID;

public final class PunishmentGate {

    private final SauverDAO dao;
    private final boolean banAlts;

    public PunishmentGate(SauverDAO dao, boolean banAlts) {
        this.dao = dao;
        this.banAlts = banAlts;
    }

    public Entry resolveLoginBan(UUID uuid, String ip, long now) {
        Entry own = dao.activeBan(uuid);
        if (own != null && own.inForce(now)) {
            return own;
        }
        Entry ipBan = dao.activeIpPunishment(Entry.Type.BAN, ip, now);
        if (ipBan != null) {
            return ipBan;
        }
        if (banAlts) {
            Entry alt = dao.bannedAltOnIp(uuid, ip, now);
            if (alt != null) {
                return alt;
            }
        }
        return null;
    }

    public Entry resolveMute(UUID uuid, String ip, long now) {
        Entry own = dao.activeMute(uuid);
        if (own != null && own.inForce(now)) {
            return own;
        }
        return dao.activeIpPunishment(Entry.Type.MUTE, ip, now);
    }

    public static String banScreen(Entry e) {
        long now = System.currentTimeMillis();
        String head = e.permanent()
                ? Colours.WARNING + "You have been permanently banned."
                : Colours.WARNING + "You have been banned for "
                        + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(e.remaining(now)) + Colours.WARNING + ".";
        return head + "\n" + Colours.BRAND_ACCENT_SECONDARY + reason(e);
    }

    public static String muteNotice(Entry e) {
        long now = System.currentTimeMillis();
        if (e.permanent()) {
            return Colours.WARNING + "You are muted." + "\n" + Colours.BRAND_ACCENT_SECONDARY + reason(e);
        }
        return Colours.WARNING + "You are muted. Time remaining: "
                + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(e.remaining(now));
    }

    private static String reason(Entry e) {
        String r = e.reason();
        return r == null || r.isBlank() ? "No reason specified." : r;
    }

    public static String stripTags(String miniMessage) {
        if (miniMessage == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(miniMessage.length());
        int i = 0;
        int n = miniMessage.length();
        while (i < n) {
            char c = miniMessage.charAt(i);
            if (c == '<') {
                int close = miniMessage.indexOf('>', i);
                if (close > i) {
                    i = close + 1;
                    continue;
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }
}
