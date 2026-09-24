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
                err(sender, teacommontea.util.Lang.of("duration.invalid", "duration", rest.get(0))
                        + " " + teacommontea.util.Lang.of("punish.duration.forms"));
                return;
            }
        } else if (requireDuration) {
            usage(sender, "temp" + word + " <player> <duration> [-s] [reason]",
                    teacommontea.util.Lang.of("punish.description.temp." + word));
            return;
        }

        String reason = rest.isEmpty()
                ? teacommontea.util.Lang.of("punish.reason." + word)
                : String.join(" ", rest);

        long now = System.currentTimeMillis();
        long cd = SauverLimits.cooldownRemaining(sender, type, now);
        if (cd > 0) {
            err(sender, teacommontea.util.Lang.of("punish.cooldown." + word, "duration", SauverFormat.fancyTime(cd)));
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
                err(sender, teacommontea.util.Lang.of("punish.mass.none." + word));
                return;
            }
            SauverLimits.markUsed(sender, type, now);
            send(sender, teacommontea.util.Lang.counted("punish.mass." + word
                    + (duration == Entry.PERMANENT ? ".permanent" : ".temporary"), done,
                    "count", done, "duration", SauverFormat.fancyTime(duration), "reason", reason));
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
        send(sender, teacommontea.util.Lang.of((overwriting ? "punish.updated." : "punish.issued.") + word
                + (r.entry().permanent() ? ".permanent" : ".temporary"),
                "name", bestName(target, parsed.targetName()),
                "duration", SauverFormat.fancyTime(r.entry().duration()),
                "reason", reason, "id", r.entry().randomId()));
    }

    public void issueIp(CommandSender sender, String[] args, Entry.Type type) {
        String word = type == Entry.Type.BAN ? "ipban" : "ipmute";
        Parsed parsed = parse(args);
        if (parsed.targetName() == null) {
            usage(sender, word + " <player|IP> [-s] [duration] [reason]", teacommontea.util.Lang.of("punish.description." + word));
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
                ? teacommontea.util.Lang.of("punish.reason." + word)
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
                err(sender, teacommontea.util.Lang.of("punish.ip.none.mass." + word));
                return;
            }
            send(sender, teacommontea.util.Lang.counted("punish.mass." + word
                    + (duration == Entry.PERMANENT ? ".permanent" : ".temporary"), done,
                    "count", done, "duration", SauverFormat.fancyTime(duration), "reason", reason));
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
                err(sender, teacommontea.util.Lang.of("punish.ip.none.player", "name", parsed.targetName()));
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
        send(sender, teacommontea.util.Lang.of("punish.issued." + word
                + (r.entry().permanent() ? ".permanent" : ".temporary"),
                "name", labelName, "duration", SauverFormat.fancyTime(r.entry().duration()),
                "reason", reason, "id", r.entry().randomId()));
    }

    public void kick(CommandSender sender, String[] args) {
        Parsed parsed = parse(args);
        if (parsed.targetName() == null) {
            usage(sender, "kick <player> [-s] [reason]", teacommontea.util.Lang.of("punish.description.kick"));
            return;
        }
        String reason = parsed.rest().isEmpty() ? teacommontea.util.Lang.of("punish.reason.kick") : String.join(" ", parsed.rest());

        if (isAllTarget(parsed.targetName())) {
            List<Player> targets = new ArrayList<>();
            for (Player pl : Bukkit.getOnlinePlayers()) {
                if (SauverExempt.blockReason(sender, pl.getUniqueId(), Entry.Type.KICK) == null) {
                    targets.add(pl);
                }
            }
            if (targets.isEmpty()) {
                err(sender, teacommontea.util.Lang.of("punish.kick.none"));
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
            send(sender, teacommontea.util.Lang.counted("punish.kick.mass", done, "count", done, "reason", reason));
            return;
        }

        Player target = Bukkit.getPlayerExact(parsed.targetName());
        if (target == null) {
            err(sender, teacommontea.util.Lang.of("player.offline"));
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
        send(sender, teacommontea.util.Lang.of("punish.kick.issued", "name", target.getName(), "reason", reason));
    }

    public void pardon(CommandSender sender, String[] args, Entry.Type type) {
        String word = type == Entry.Type.BAN ? "ban" : "mute";
        if (args.length == 0) {
            usage(sender, "un" + word + " <player> [reason]", teacommontea.util.Lang.of("punish.description.un" + word));
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
                err(sender, teacommontea.util.Lang.of("punish.remove.none." + word));
                return;
            }
            send(sender, teacommontea.util.Lang.counted("punish.mass.removed." + word, done, "count", done));
            return;
        }

        UUID target = resolve(args[0]);
        if (target == null) {
            unknownPlayer(sender, args[0]);
            return;
        }

        Entry active = type == Entry.Type.BAN ? dao().activeBan(target) : dao().activeMute(target);
        if (active != null && !canRemove(sender, type, active)) {
            err(sender, teacommontea.util.Lang.of("punish.remove.own." + word));
            return;
        }
        SauverEngine.Result r = SauverEngine.pardon(type, target, bestName(target, args[0]),
                executorUuid(sender), executorName(sender), reason);
        if (!r.ok()) {
            err(sender, Colours.WARNING + r.error());
            return;
        }
        send(sender, teacommontea.util.Lang.of("punish.removed." + word, "name", bestName(target, args[0])));
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
            usage(sender, "check" + word + " <player>", teacommontea.util.Lang.of("punish.description.check" + word));
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
            send(sender, teacommontea.util.Lang.of("punish.check.clean." + word, "name", name));
            return;
        }
        long now = System.currentTimeMillis();
        send(sender, teacommontea.util.Lang.of("punish.check.active." + word, "name", name));
        raw(sender, teacommontea.util.Lang.of("punish.check.by", "name", e.executorName()));
        raw(sender, teacommontea.util.Lang.of("punish.check.reason", "reason", e.reason()));
        raw(sender, e.permanent()
                ? teacommontea.util.Lang.of("punish.check.duration.permanent")
                : teacommontea.util.Lang.of("punish.check.duration.expires", "duration", SauverFormat.fancyTime(e.remaining(now))));
        raw(sender, teacommontea.util.Lang.of("punish.check.id", "id", e.randomId()));
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
