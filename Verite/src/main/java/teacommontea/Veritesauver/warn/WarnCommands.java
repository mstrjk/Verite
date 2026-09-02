package teacommontea.veritesauver.warn;

import teacommontea.util.Colours;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.command.CommandBase;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverEngine;
import teacommontea.veritesauver.util.SauverFormat;

public final class WarnCommands extends CommandBase {

    private static final int PAGE_SIZE = 8;

    public WarnCommands(Sauver sauver) {
        super(sauver);
    }

    public void warn(CommandSender sender, String[] args) {
        Parsed parsed = parse(args);
        if (parsed.targetName() == null) {
            usage(sender, "warn <player> [-s] [reason]", "warn a player");
            return;
        }
        String reason = parsed.rest().isEmpty() ? "No reason specified." : String.join(" ", parsed.rest());

        if (isAllTarget(parsed.targetName())) {
            int done = 0;
            for (UUID u : expandTargets(parsed.targetName())) {
                if (teacommontea.veritesauver.punish.SauverExempt.blockReason(sender, u, Entry.Type.WARNING) != null) {
                    continue;
                }
                SauverEngine.Result r = SauverEngine.warn(u, bestName(u, "?"), reason,
                        executorUuid(sender), executorName(sender), parsed.silent());
                if (r.ok()) {
                    done++;
                }
            }
            if (done == 0) {
                err(sender, Colours.WARNING + "No online players could be warned.");
                return;
            }
            send(sender, Colours.BRAND + "You warned " + Colours.BRAND_ACCENT_SECONDARY + done + Colours.BRAND
                    + " " + SauverFormat.pluralize(done, "player") + Colours.BRAND + ". " + Colours.BRAND_ACCENT_SECONDARY + "Reason: " + Colours.BRAND_ACCENT_SECONDARY + reason);
            return;
        }

        UUID target = resolve(parsed.targetName());
        if (target == null) {
            unknownPlayer(sender, parsed.targetName());
            return;
        }
        String exempt = teacommontea.veritesauver.punish.SauverExempt.blockReason(sender, target, Entry.Type.WARNING);
        if (exempt != null) {
            err(sender, Colours.WARNING + exempt);
            return;
        }
        SauverEngine.Result r = SauverEngine.warn(target, bestName(target, parsed.targetName()), reason,
                executorUuid(sender), executorName(sender), parsed.silent());
        if (!r.ok()) {
            err(sender, Colours.WARNING + r.error());
            return;
        }
        int active = dao().activeWarnings(target, System.currentTimeMillis()).size();
        send(sender, Colours.BRAND + "You warned " + Colours.BRAND_ACCENT_SECONDARY + bestName(target, parsed.targetName())
                + Colours.BRAND + ". " + Colours.BRAND_ACCENT_SECONDARY + "Active warnings: " + Colours.BRAND_ACCENT_SECONDARY + active + " " + Colours.BRAND_ACCENT_SECONDARY + "(#" + r.entry().randomId() + ")");
    }

    public void unwarn(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "unwarn <player> [reason]", "remove a player's most recent warning");
            return;
        }
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                : DEFAULT_REMOVE_REASON;

        if (isAllTarget(args[0])) {
            long now = System.currentTimeMillis();
            java.util.Set<UUID> seen = new java.util.LinkedHashSet<>();
            for (Entry e : dao().allActiveWarnings(now)) {
                if (e.uuid() != null) {
                    seen.add(e.uuid());
                }
            }
            int done = 0;
            for (UUID u : seen) {
                SauverEngine.Result r = SauverEngine.unwarn(u, bestName(u, "?"),
                        executorUuid(sender), executorName(sender), reason);
                if (r.ok()) {
                    done++;
                }
            }
            if (done == 0) {
                err(sender, Colours.WARNING + "There are no active warnings to remove.");
                return;
            }
            send(sender, Colours.BRAND + "You removed a warning from " + Colours.BRAND_ACCENT_SECONDARY + done
                    + Colours.BRAND + " " + SauverFormat.pluralize(done, "player") + Colours.BRAND + ".");
            return;
        }

        UUID target = resolve(args[0]);
        if (target == null) {
            unknownPlayer(sender, args[0]);
            return;
        }
        SauverEngine.Result r = SauverEngine.unwarn(target, bestName(target, args[0]),
                executorUuid(sender), executorName(sender), reason);
        if (!r.ok()) {
            err(sender, Colours.WARNING + r.error());
            return;
        }
        int active = dao().activeWarnings(target, System.currentTimeMillis()).size();
        send(sender, Colours.BRAND + "You removed a warning from " + Colours.BRAND_ACCENT_SECONDARY + bestName(target, args[0])
                + Colours.BRAND + ". " + Colours.BRAND_ACCENT_SECONDARY + "Active warnings: " + Colours.BRAND_ACCENT_SECONDARY + active);
    }

    public void warnings(CommandSender sender, String[] args) {
        UUID target;
        String name;
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                err(sender, Colours.WARNING + "Console must specify a player.");
                return;
            }
            target = p.getUniqueId();
            name = p.getName();
        } else {
            if (!sender.hasPermission("veritesauver.warnings")) {
                err(sender, Colours.WARNING + "You may only view your own warnings.");
                return;
            }
            target = resolve(args[0]);
            if (target == null) {
                unknownPlayer(sender, args[0]);
                return;
            }
            name = bestName(target, args[0]);
        }
        List<Entry> active = dao().activeWarnings(target, System.currentTimeMillis());
        if (active.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + name + " " + Colours.BRAND + "has no active warnings.");
            return;
        }
        send(sender, Colours.BRAND_ACCENT_SECONDARY + name + " " + Colours.BRAND_ACCENT_SECONDARY + "has " + Colours.BRAND_ACCENT_SECONDARY + active.size()
                + " " + Colours.BRAND_ACCENT_SECONDARY + "active " + SauverFormat.pluralize(active.size(), "warning") + ".");
        long now = System.currentTimeMillis();
        for (Entry e : active) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  #" + e.randomId() + " " + Colours.BRAND_ACCENT_SECONDARY + e.reason()
                    + " " + Colours.BRAND_ACCENT_SECONDARY + "by " + Colours.BRAND_ACCENT_SECONDARY + e.executorName() + " " + Colours.BRAND_ACCENT_SECONDARY + "(expires in " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(e.remaining(now)) + Colours.BRAND_ACCENT_SECONDARY + ")");
        }
    }

    public void checkwarn(CommandSender sender, String[] args) {
        UUID u = lookupTarget(sender, args, "checkwarn");
        if (u == null) {
            return;
        }
        int n = dao().activeWarnings(u, System.currentTimeMillis()).size();
        String name = bestName(u, args[0]);
        if (n == 0) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + name + " " + Colours.BRAND + "has no active warnings.");
        } else {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + name + " " + Colours.WARNING + "has " + Colours.BRAND_ACCENT_SECONDARY + n + " " + Colours.WARNING + "active "
                    + SauverFormat.pluralize(n, "warning") + ". " + Colours.BRAND_ACCENT_SECONDARY + "See " + Colours.BRAND_ACCENT_SECONDARY + "/warnings " + name);
        }
    }

    public List<String> tabUnwarn(CommandSender sender, String permission, String[] args) {
        if (!sender.hasPermission(permission)) {
            return List.of();
        }
        if (args.length != 1) {
            return List.of();
        }
        java.util.Set<String> names = new java.util.LinkedHashSet<>();
        for (Entry e : dao().allActiveWarnings(System.currentTimeMillis())) {
            if (e.uuid() != null) {
                names.add(bestName(e.uuid(), null));
            }
        }
        return matchNamesOrAll(new java.util.ArrayList<>(names), args[0]);
    }

    public void warnlist(CommandSender sender, String[] args) {
        long now = System.currentTimeMillis();
        List<Entry> all = dao().allActiveWarnings(now);
        if (all.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + "There are no active warnings.");
            return;
        }
        int page = args.length > 0 ? Math.max(1, parseIntOr(args[0], 1)) : 1;
        int pages = (all.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        page = Math.min(page, pages);
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, all.size());
        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Active warnings (" + Colours.BRAND_ACCENT_SECONDARY + all.size()
                + Colours.BRAND_ACCENT_SECONDARY + ") - page " + Colours.BRAND_ACCENT_SECONDARY + page + Colours.BRAND_ACCENT_SECONDARY + "/" + Colours.BRAND_ACCENT_SECONDARY + pages + Colours.BRAND_ACCENT_SECONDARY + ".");
        for (int i = from; i < to; i++) {
            Entry e = all.get(i);
            String tname = e.uuid() != null ? bestName(e.uuid(), "?") : "?";
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  #" + e.randomId() + " " + Colours.BRAND_ACCENT_SECONDARY + tname + " " + Colours.BRAND_ACCENT_SECONDARY + "by " + Colours.BRAND_ACCENT_SECONDARY
                    + e.executorName() + " " + Colours.BRAND_ACCENT_SECONDARY + "(expires " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(e.remaining(now)) + Colours.BRAND_ACCENT_SECONDARY + ") " + e.reason());
        }
    }
}
