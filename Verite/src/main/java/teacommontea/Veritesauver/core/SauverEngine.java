package teacommontea.veritesauver.core;

import teacommontea.util.Colours;
import teacommontea.veritesauver.util.SauverFormat;
import teacommontea.veritesauver.util.SauverMessages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.util.SauverConfig;
import teacommontea.veritesauver.warn.SauverWarnActions;
import teacommontea.veritesauver.core.Entry;

public final class SauverEngine {

    public static final String CONSOLE_NAME = "Console";

    private SauverEngine() {}

    private static SauverDAO dao() {
        return Sauver.instance().dao();
    }

    private static SauverMessages msg() {
        return Sauver.instance().messages();
    }

    public record Result(boolean ok, Entry entry, String error) {
        static Result fail(String why) { return new Result(false, null, why); }
        static Result done(Entry e)    { return new Result(true, e, null); }
    }

    public static Result issue(Entry.Type type, UUID targetUuid, String targetIp, String targetName,
                               String reason, UUID executorUuid, String executorName,
                               long durationMillis, boolean silent, boolean ipban) {
        if (targetUuid == null && targetIp == null) {
            return Result.fail("A punishment needs a target uuid or IP.");
        }

        if (targetUuid != null) {
            Entry active = type == Entry.Type.BAN ? dao().activeBan(targetUuid)
                    : type == Entry.Type.MUTE ? dao().activeMute(targetUuid) : null;
            if (active != null && active.active()) {
                Entry superseded = active.withRemoval(executorUuid, executorName, "superseded by a new " + type.id());
                dao().save(superseded);
                SauverEvents.fireRemoved(superseded);
            }
        }
        long now = System.currentTimeMillis();
        long dateEnd = durationMillis == Entry.PERMANENT ? Entry.PERMANENT : now + durationMillis;
        long id = dao().nextId();
        Entry e = new Entry(
            id, String.valueOf(id), type, targetUuid, targetIp, reason,
            executorUuid, executorName, null, null, null,
            now, dateEnd, Entry.GLOBAL_SCOPE, Entry.GLOBAL_SCOPE,
            0, silent, ipban, true);
        dao().save(e);

        enforce(e, targetName);
        announce(e, targetName);
        SauverEvents.fireAdded(e);
        return Result.done(e);
    }

    public static Result kick(UUID targetUuid, String targetName, String reason,
                              UUID executorUuid, String executorName, boolean silent) {
        Player online = targetUuid == null ? null : Bukkit.getPlayer(targetUuid);
        if (online == null) {
            return Result.fail(targetName + " is not online.");
        }
        long now = System.currentTimeMillis();
        long id = dao().nextId();
        Entry e = new Entry(
            id, String.valueOf(id), Entry.Type.KICK, targetUuid,
            addressOf(online), reason, executorUuid, executorName, null, null, null,
            now, now, Entry.GLOBAL_SCOPE, Entry.GLOBAL_SCOPE, 0, silent, false, false);
        dao().save(e);

        online.kickPlayer(teacommontea.util.text.Text.toLegacy(renderTemplate("kick.template",
                "You have been kicked by @enforcer.\\n@reason", e, targetName, 0)));
        announce(e, targetName);
        SauverEvents.fireAdded(e);
        return Result.done(e);
    }

    private static String addressOf(Player p) {
        return p.getAddress() == null || p.getAddress().getAddress() == null
                ? null : p.getAddress().getAddress().getHostAddress();
    }

    public static long warningExpire() {
        return SauverConfig.warningExpire();
    }

    public static Result warn(UUID targetUuid, String targetName, String reason,
                              UUID executorUuid, String executorName, boolean silent) {
        if (targetUuid == null) {
            return Result.fail("A warning needs a known player.");
        }
        long now = System.currentTimeMillis();
        long id = dao().nextId();
        Entry e = new Entry(
            id, String.valueOf(id), Entry.Type.WARNING, targetUuid, null, reason,
            executorUuid, executorName, null, null, null,
            now, now + warningExpire(), Entry.GLOBAL_SCOPE, Entry.GLOBAL_SCOPE,
            0, silent, false, true);
        dao().save(e);

        announce(e, targetName);
        SauverEvents.fireAdded(e);

        int active = dao().activeWarnings(targetUuid, now).size();
        Player online = Bukkit.getPlayer(targetUuid);
        if (online != null && !silent) {
            msg().send(online, renderTemplate("warn.template",
                    "You have been warned by @enforcer. Active warnings: @total.warns\\n@reason",
                    e, targetName, active));
        }
        SauverWarnActions.onWarn(targetUuid, targetName, active);
        return Result.done(e);
    }

    public static Result unwarn(UUID targetUuid, String targetName, UUID removedByUuid,
                                String removedByName, String removalReason) {
        long now = System.currentTimeMillis();
        java.util.List<Entry> active = dao().activeWarnings(targetUuid, now);
        if (active.isEmpty()) {
            return Result.fail(targetName + " has no active warnings.");
        }
        Entry latest = active.get(0);
        Entry removed = latest.withRemoval(removedByUuid, removedByName, removalReason);
        dao().save(removed);
        SauverEvents.fireRemoved(removed);
        return Result.done(removed);
    }

    public static Result issueIp(Entry.Type type, UUID targetUuid, String ip, String targetName,
                                 String reason, UUID executorUuid, String executorName,
                                 long durationMillis, boolean silent) {
        if (ip == null) {
            return Result.fail("No IP is known for " + targetName + " (they must have logged in once).");
        }
        long now = System.currentTimeMillis();
        long dateEnd = durationMillis == Entry.PERMANENT ? Entry.PERMANENT : now + durationMillis;
        long id = dao().nextId();
        Entry e = new Entry(
            id, String.valueOf(id), type, targetUuid, ip, reason,
            executorUuid, executorName, null, null, null,
            now, dateEnd, Entry.GLOBAL_SCOPE, Entry.GLOBAL_SCOPE,
            0, silent, true, true);
        dao().save(e);

        if (type == Entry.Type.BAN) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (ip.equals(addressOf(p))) {
                    p.kickPlayer(teacommontea.util.text.Text.toLegacy(banScreen(e)));
                }
            }
        }
        announce(e, targetName);
        SauverEvents.fireAdded(e);
        return Result.done(e);
    }

    public static Result pardon(Entry.Type type, UUID targetUuid, String targetName,
                                UUID removedByUuid, String removedByName, String removalReason) {
        Entry active = type == Entry.Type.BAN ? dao().activeBan(targetUuid) : dao().activeMute(targetUuid);
        if (active == null || !active.active()) {
            return Result.fail("That player is not " + (type == Entry.Type.BAN ? "banned" : "muted") + ".");
        }
        Entry pardoned = active.withRemoval(removedByUuid, removedByName, removalReason);
        dao().save(pardoned);

        SauverEvents.fireRemoved(pardoned);
        return Result.done(pardoned);
    }

    private static void enforce(Entry e, String targetName) {
        if (e.uuid() == null) {
            return;
        }
        Player online = Bukkit.getPlayer(e.uuid());
        if (online == null) {
            return;
        }
        if (e.type() == Entry.Type.BAN) {
            online.kickPlayer(teacommontea.util.text.Text.toLegacy(banScreen(e)));
        } else if (e.type() == Entry.Type.MUTE && !e.silent()) {
            msg().send(online, muteLine(e));
        }
    }

    private static String renderTemplate(String key, String def, Entry e, String targetName, int totalWarns) {
        String tmpl = SauverConfig.template(key, def);
        String duration = e.permanent() ? "" : SauverFormat.fancyTime(e.duration());
        String remaining = e.permanent() ? "" : SauverFormat.fancyTime(e.remaining(System.currentTimeMillis()));
        String verb = switch (e.type()) {
            case BAN -> e.permanent() ? "permanently banned" : "banned";
            case MUTE -> e.permanent() ? "permanently muted" : "muted";
            case WARNING -> "warned";
            case KICK -> "kicked";
        };
        return tmpl
                .replace("\\n", "<newline>")
                .replace("@duration", duration)
                .replace("@remaining", remaining)
                .replace("@enforcer", stripLegacy(e.executorName() == null ? "console" : e.executorName()))
                .replace("@target", stripLegacy(targetName == null ? "" : targetName))
                .replace("@punishment", verb)
                .replace("@total.warns", String.valueOf(totalWarns))
                .replace("@reason", stripLegacy(safeReason(e.reason())));
    }

    private static final java.util.regex.Pattern LEGACY_SECTION =
            java.util.regex.Pattern.compile("§[0-9A-Fa-fK-ORXk-orx]");

    public static String stripLegacy(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return LEGACY_SECTION.matcher(s).replaceAll("");
    }

    public static String banScreen(Entry e) {
        String key = e.permanent() ? "ban.template.permanent" : "ban.template.temporary";
        String def = e.permanent()
                ? "You have been permanently banned.\\n@reason"
                : "You have been banned for @duration.\\n@reason";
        return renderTemplate(key, def, e, null, 0);
    }

    public static String muteLine(Entry e) {
        String key = e.permanent() ? "mute.template.permanent" : "mute.template.temporary";
        String def = e.permanent()
                ? "You have been muted by @enforcer.\\n@reason"
                : "You have been muted by @enforcer for @duration.\\n@reason";
        return renderTemplate(key, def, e, null, 0);
    }

    public static String muteNotice(Entry e) {
        String key = e.permanent() ? "mute.notice.permanent" : "mute.notice";
        String def = e.permanent()
                ? "You are muted."
                : "You are muted. Time remaining: @remaining";
        return renderTemplate(key, def, e, null, 0);
    }

    private static void announce(Entry e, String targetName) {
        String receipt = renderTemplate("admin.receipt.template",
                "@target was @punishment by @enforcer.\\n@reason", e, targetName, 0);
        if (e.silent()) {
            String line = Colours.BRAND_ACCENT + "[" + Colours.BRAND_ACCENT_SECONDARY + "Silent" + Colours.BRAND_ACCENT + "] " + Colours.BRAND_ACCENT_SECONDARY + receipt;
            msg().notify("veritesauver.notify.silent", p -> isExecutor(p, e), line);
            SauverEvents.fireBroadcast(line, targetName);
            return;
        }
        msg().notify("veritesauver.notify.broadcast", p -> isExecutor(p, e), receipt);
        SauverEvents.fireBroadcast(receipt, targetName);
    }

    private static boolean isExecutor(Player p, Entry e) {
        return e.executorUuid() != null && e.executorUuid().equals(p.getUniqueId());
    }

    private static String safeReason(String reason) {
        return reason == null || reason.isBlank() ? "No reason specified." : reason;
    }
}
