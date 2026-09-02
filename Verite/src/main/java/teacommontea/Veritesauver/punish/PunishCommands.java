package teacommontea.veritesauver.punish;

import teacommontea.util.Colours;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.util.SauverDuration;
import teacommontea.veritesauver.command.CommandBase;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverEngine;
import teacommontea.veritesauver.util.SauverFormat;

public final class PunishCommands extends CommandBase {

    private static final String DEFAULT_BAN_REASON  = "The Ban Hammer has spoken!";
    private static final String DEFAULT_MUTE_REASON = "Spamming";

    public PunishCommands(Sauver sauver) {
        super(sauver);
    }

    public void issueBanOrMute(CommandSender sender, String[] args, Entry.Type type, boolean requireDuration) {
        Parsed parsed = parse(args);
        String word = type == Entry.Type.BAN ? "ban" : "mute";
        if (parsed.targetName() == null) {
            String c = (requireDuration ? "temp" : "") + word;
            usage(sender, c + " <player> " + (requireDuration ? "<duration> " : "") + "[-s] [reason]",
                    (requireDuration ? "temporarily " : "") + word + " a player");
            return;
        }
        boolean all = isAllTarget(parsed.targetName());
        UUID target = all ? null : resolve(parsed.targetName());
        if (!all && target == null) {
            unknownPlayer(sender, parsed.targetName());
            return;
        }

        List<String> rest = new ArrayList<>(parsed.rest());

        long duration = Entry.PERMANENT;
        if (!rest.isEmpty()) {
            long maybe = SauverDuration.parse(rest.get(0));
            if (maybe != -1) {
                duration = maybe;
                rest.remove(0);
            } else if (requireDuration) {
                err(sender, Colours.WARNING + "That duration is invalid: " + Colours.BRAND_ACCENT_SECONDARY + rest.get(0)
                        + Colours.WARNING + ". Use forms like " + Colours.BRAND_ACCENT_SECONDARY + "7d" + Colours.WARNING + ", " + Colours.BRAND_ACCENT_SECONDARY + "12h" + Colours.WARNING + ", " + Colours.BRAND_ACCENT_SECONDARY + "2w" + Colours.WARNING + ", " + Colours.BRAND_ACCENT_SECONDARY + "1mo" + Colours.WARNING + ".");
                return;
            }
        } else if (requireDuration) {
            usage(sender, "temp" + word + " <player> <duration> [-s] [reason]", "temporarily " + word + " a player");
            return;
        }

        String reason = rest.isEmpty()
                ? (type == Entry.Type.BAN ? DEFAULT_BAN_REASON : DEFAULT_MUTE_REASON)
                : String.join(" ", rest);

        long now = System.currentTimeMillis();
        long cd = SauverLimits.cooldownRemaining(sender, type, now);
        if (cd > 0) {
            err(sender, Colours.WARNING + "Slow down. Wait " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(cd)
                    + " " + Colours.WARNING + "before your next " + type.id() + ".");
            return;
        }
        SauverLimits.Check cap = SauverLimits.capDuration(sender, type, duration);
        if (!cap.ok()) {
            err(sender, Colours.WARNING + cap.error());
            return;
        }
        duration = cap.durationMillis();

        if (all) {
            int done = 0;
            for (UUID u : expandTargets(parsed.targetName())) {
                if (SauverExempt.blockReason(sender, u, type) != null) {
                    continue;
                }
                SauverEngine.Result r = SauverEngine.issue(
                        type, u, null, bestName(u, "?"), reason,
                        executorUuid(sender), executorName(sender), duration, parsed.silent(), false);
                if (r.ok()) {
                    done++;
                }
            }
            if (done == 0) {
                err(sender, Colours.WARNING + "No online players could be " + pastTense(word) + ".");
                return;
            }
            SauverLimits.markUsed(sender, type, now);
            String durAll = duration == Entry.PERMANENT ? "permanently" : "for " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(duration);
            send(sender, Colours.BRAND + "You " + pastTense(word) + " " + Colours.BRAND_ACCENT_SECONDARY + done + Colours.BRAND
                    + " " + SauverFormat.pluralize(done, "player") + " " + Colours.BRAND + durAll + Colours.BRAND + ". " + Colours.BRAND_ACCENT_SECONDARY + "Reason: " + Colours.BRAND_ACCENT_SECONDARY + reason);
            return;
        }

        boolean overwriting = (type == Entry.Type.BAN ? sauver.activeBan(target) : sauver.activeMute(target)) != null;
        String exempt = SauverExempt.blockReason(sender, target, type);
        if (exempt != null) {
            err(sender, Colours.WARNING + exempt);
            return;
        }

        SauverEngine.Result r = SauverEngine.issue(
                type, target, null, bestName(target, parsed.targetName()), reason,
                executorUuid(sender), executorName(sender), duration, parsed.silent(), false);
        if (!r.ok()) {
            err(sender, Colours.WARNING + r.error());
            return;
        }
        SauverLimits.markUsed(sender, type, now);
        String dur = r.entry().permanent() ? "permanently" : "for " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(r.entry().duration());
        String verb = overwriting ? "updated the " + word + " on" : pastTense(word);
        send(sender, Colours.BRAND + "You " + verb + " " + Colours.BRAND_ACCENT_SECONDARY + bestName(target, parsed.targetName())
                + " " + Colours.BRAND + dur + Colours.BRAND + ". " + Colours.BRAND_ACCENT_SECONDARY + "Reason: " + Colours.BRAND_ACCENT_SECONDARY + reason + " " + Colours.BRAND_ACCENT_SECONDARY + "(#" + r.entry().randomId() + ")");
    }

    public void issueIp(CommandSender sender, String[] args, Entry.Type type) {
        String word = type == Entry.Type.BAN ? "ipban" : "ipmute";
        Parsed parsed = parse(args);
        if (parsed.targetName() == null) {
            usage(sender, word + " <player|IP> [-s] [duration] [reason]", word + " a player or IP");
            return;
        }
        List<String> rest = new ArrayList<>(parsed.rest());
        long duration = Entry.PERMANENT;
        if (!rest.isEmpty()) {
            long maybe = SauverDuration.parse(rest.get(0));
            if (maybe != -1) {
                duration = maybe;
                rest.remove(0);
            }
        }
        String reason = rest.isEmpty()
                ? (type == Entry.Type.BAN ? DEFAULT_BAN_REASON : DEFAULT_MUTE_REASON)
                : String.join(" ", rest);

        if (isAllTarget(parsed.targetName())) {
            int done = 0;
            for (UUID u : expandTargets(parsed.targetName())) {
                if (SauverExempt.blockReason(sender, u, type) != null) {
                    continue;
                }
                List<String> ips = dao().ipsOf(u);
                if (ips.isEmpty()) {
                    continue;
                }
                SauverEngine.Result r = SauverEngine.issueIp(type, u, ips.get(0), bestName(u, "?"), reason,
                        executorUuid(sender), executorName(sender), duration, parsed.silent());
                if (r.ok()) {
                    done++;
                }
            }
            if (done == 0) {
                err(sender, Colours.WARNING + "No online players had an IP on record to " + word + ".");
                return;
            }
            String durAll = duration == Entry.PERMANENT ? "permanently" : "for " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(duration);
            send(sender, Colours.BRAND + "You " + pastTense(word) + " " + Colours.BRAND_ACCENT_SECONDARY + done + Colours.BRAND
                    + " " + SauverFormat.pluralize(done, "player") + " " + Colours.BRAND + durAll + Colours.BRAND + ". " + Colours.BRAND_ACCENT_SECONDARY + "Reason: " + Colours.BRAND_ACCENT_SECONDARY + reason);
            return;
        }

        UUID targetUuid = null;
        String ip;
        String labelName;
        if (isIpLiteral(parsed.targetName())) {
            ip = parsed.targetName();
            labelName = ip;
        } else {
            targetUuid = resolve(parsed.targetName());
            if (targetUuid == null) {
                unknownPlayer(sender, parsed.targetName());
                return;
            }
            List<String> ips = dao().ipsOf(targetUuid);
            if (ips.isEmpty()) {
                err(sender, Colours.WARNING + "No IP on record for " + Colours.BRAND_ACCENT_SECONDARY + parsed.targetName()
                        + Colours.WARNING + ". They must have logged in at least once.");
                return;
            }
            ip = ips.get(0);
            labelName = bestName(targetUuid, parsed.targetName());
        }

        SauverEngine.Result r = SauverEngine.issueIp(type, targetUuid, ip, labelName, reason,
                executorUuid(sender), executorName(sender), duration, parsed.silent());
        if (!r.ok()) {
            err(sender, Colours.WARNING + r.error());
            return;
        }
        String dur = r.entry().permanent() ? "permanently" : "for " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(r.entry().duration());
        send(sender, Colours.BRAND + "You " + pastTense(word) + " " + Colours.BRAND_ACCENT_SECONDARY + labelName
                + " " + Colours.BRAND + dur + Colours.BRAND + ". " + Colours.BRAND_ACCENT_SECONDARY + "Reason: " + Colours.BRAND_ACCENT_SECONDARY + reason + " " + Colours.BRAND_ACCENT_SECONDARY + "(#" + r.entry().randomId() + ")");
    }

    public void kick(CommandSender sender, String[] args) {
        Parsed parsed = parse(args);
        if (parsed.targetName() == null) {
            usage(sender, "kick <player> [-s] [reason]", "kick a player");
            return;
        }
        String reason = parsed.rest().isEmpty() ? "Kicked by an operator." : String.join(" ", parsed.rest());

        if (isAllTarget(parsed.targetName())) {
            List<Player> targets = new ArrayList<>();
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (SauverExempt.blockReason(sender, pl.getUniqueId(), Entry.Type.KICK) == null) {
                    targets.add(pl);
                }
            }
            if (targets.isEmpty()) {
                err(sender, Colours.WARNING + "No online players can be kicked.");
                return;
            }
            int done = 0;
            for (Player pl : targets) {
                SauverEngine.Result r = SauverEngine.kick(pl.getUniqueId(), pl.getName(), reason,
                        executorUuid(sender), executorName(sender), parsed.silent());
                if (r.ok()) {
                    done++;
                }
            }
            send(sender, Colours.BRAND + "You kicked " + Colours.BRAND_ACCENT_SECONDARY + done + Colours.BRAND
                    + " " + SauverFormat.pluralize(done, "player") + ". " + Colours.BRAND_ACCENT_SECONDARY + "Reason: " + Colours.BRAND_ACCENT_SECONDARY + reason);
            return;
        }

        Player target = Bukkit.getPlayerExact(parsed.targetName());
        if (target == null) {
            err(sender, Colours.WARNING + "That player is not online.");
            return;
        }
        String exempt = SauverExempt.blockReason(sender, target.getUniqueId(), Entry.Type.KICK);
        if (exempt != null) {
            err(sender, Colours.WARNING + exempt);
            return;
        }
        SauverEngine.Result r = SauverEngine.kick(target.getUniqueId(), target.getName(), reason,
                executorUuid(sender), executorName(sender), parsed.silent());
        if (!r.ok()) {
            err(sender, Colours.WARNING + r.error());
            return;
        }
        send(sender, Colours.BRAND + "You kicked " + Colours.BRAND_ACCENT_SECONDARY + target.getName()
                + Colours.BRAND + ". " + Colours.BRAND_ACCENT_SECONDARY + "Reason: " + Colours.BRAND_ACCENT_SECONDARY + reason);
    }

    public void pardon(CommandSender sender, String[] args, Entry.Type type) {
        String word = type == Entry.Type.BAN ? "ban" : "mute";
        if (args.length == 0) {
            usage(sender, "un" + word + " <player> [reason]", "un" + word + " a player");
            return;
        }
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                : DEFAULT_REMOVE_REASON;

        if (isAllTarget(args[0])) {
            int done = 0;
            for (Entry active : dao().activeOfType(type, System.currentTimeMillis())) {
                if (active.uuid() == null || !canRemove(sender, type, active)) {
                    continue;
                }
                SauverEngine.Result r = SauverEngine.pardon(type, active.uuid(), bestName(active.uuid(), "?"),
                        executorUuid(sender), executorName(sender), reason);
                if (r.ok()) {
                    done++;
                }
            }
            if (done == 0) {
                err(sender, Colours.WARNING + "There are no " + word + "s you can remove.");
                return;
            }
            send(sender, Colours.BRAND + "You " + pastTense("un" + word) + " " + Colours.BRAND_ACCENT_SECONDARY + done
                    + Colours.BRAND + " " + SauverFormat.pluralize(done, "player") + Colours.BRAND + ".");
            return;
        }

        UUID target = resolve(args[0]);
        if (target == null) {
            unknownPlayer(sender, args[0]);
            return;
        }

        Entry active = type == Entry.Type.BAN ? dao().activeBan(target) : dao().activeMute(target);
        if (active != null && !canRemove(sender, type, active)) {
            err(sender, Colours.WARNING + "You can only remove " + word + "s you issued.");
            return;
        }
        SauverEngine.Result r = SauverEngine.pardon(type, target, bestName(target, args[0]),
                executorUuid(sender), executorName(sender), reason);
        if (!r.ok()) {
            err(sender, Colours.WARNING + r.error());
            return;
        }
        send(sender, Colours.BRAND + "You " + pastTense("un" + word) + " " + Colours.BRAND_ACCENT_SECONDARY + bestName(target, args[0]) + Colours.BRAND + ".");
    }

    private boolean canRemove(CommandSender sender, Entry.Type type, Entry e) {
        String base = type == Entry.Type.BAN ? "veritesauver.unban" : "veritesauver.unmute";
        if (sender.hasPermission(base)) {
            return true;
        }
        if (!sender.hasPermission(base + ".own")) {
            return false;
        }
        UUID who = executorUuid(sender);
        return who != null && who.equals(e.executorUuid());
    }

    public void check(CommandSender sender, String[] args, Entry.Type type) {
        String word = type == Entry.Type.BAN ? "ban" : "mute";
        if (args.length == 0) {
            usage(sender, "check" + word + " <player>", "check a player's " + word + " status");
            return;
        }
        UUID target = resolve(args[0]);
        if (target == null) {
            unknownPlayer(sender, args[0]);
            return;
        }
        Entry e = type == Entry.Type.BAN ? sauver.activeBan(target) : sauver.activeMute(target);
        String name = bestName(target, args[0]);
        if (e == null) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + name + " " + Colours.BRAND + "is not " + pastTense(word) + ".");
            return;
        }
        long now = System.currentTimeMillis();
        String when = e.permanent() ? Colours.WARNING + "permanent" : Colours.BRAND_ACCENT_SECONDARY + "expires in " + SauverFormat.fancyTime(e.remaining(now));
        send(sender, Colours.BRAND_ACCENT_SECONDARY + name + " " + Colours.WARNING + "is " + pastTense(word) + Colours.BRAND_ACCENT_SECONDARY + ".");
        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  By: " + Colours.BRAND_ACCENT_SECONDARY + e.executorName());
        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Reason: " + Colours.BRAND_ACCENT_SECONDARY + e.reason());
        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Duration: " + when);
        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  ID: " + Colours.BRAND_ACCENT_SECONDARY + "#" + e.randomId());
    }

    public static String pastTense(String word) {
        if (word.endsWith("mute")) {
            return word + "d";
        }
        return word + "ned";
    }

    public List<String> tabIssue(CommandSender sender, String permission, String[] args, boolean temp) {
        if (!sender.hasPermission(permission)) {
            return List.of();
        }
        if (args.length == 1) {
            return matchOnlineOrAll(args[0]);
        }
        if (args.length == 2) {
            List<String> out = new ArrayList<>();
            if ("-s".startsWith(args[1].toLowerCase(java.util.Locale.ROOT))) {
                out.add("-s");
            }
            if (temp) {
                out.addAll(prefixed(SauverDuration.SUGGESTIONS, args[1]));
            }
            return out;
        }
        return List.of();
    }

    public List<String> tabTarget(CommandSender sender, String permission, String[] args) {
        if (!sender.hasPermission(permission)) {
            return List.of();
        }
        return args.length == 1 ? matchOnline(args[0]) : List.of();
    }

    public List<String> tabTargetAll(CommandSender sender, String permission, String[] args) {
        if (!sender.hasPermission(permission)) {
            return List.of();
        }
        return args.length == 1 ? matchOnlineOrAll(args[0]) : List.of();
    }

    public List<String> tabPardon(CommandSender sender, String permission, String[] args, Entry.Type type) {
        if (!sender.hasPermission(permission)) {
            return List.of();
        }
        if (args.length != 1) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (Entry e : dao().activeOfType(type, System.currentTimeMillis())) {
            if (e.uuid() != null) {
                names.add(bestName(e.uuid(), null));
            }
        }
        return matchNamesOrAll(names, args[0]);
    }
}
