package teacommontea.veritesauver.lookup;

import teacommontea.util.Colours;
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
import teacommontea.veritesauver.util.SauverDuration;
import teacommontea.veritesauver.geoip.SauverGeoIp;
import teacommontea.veritesauver.mojang.SauverMojang;
import teacommontea.veritesauver.command.CommandBase;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverEngine;
import teacommontea.veritesauver.util.SauverFormat;
import teacommontea.veritesauver.util.SauverProtocol;

public final class LookupCommands extends CommandBase {

    private static final int PAGE_SIZE = 8;

    public LookupCommands(Sauver sauver) {
        super(sauver);
    }

    public void dupeip(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "dupeip <player|IP>", "list accounts sharing an IP");
            return;
        }
        String ip;
        String labelName;
        if (isIpLiteral(args[0])) {
            ip = args[0];
            labelName = ip;
        } else {
            UUID u = resolve(args[0]);
            if (u == null) {
                unknownPlayer(sender, args[0]);
                return;
            }
            List<String> ips = dao().ipsOf(u);
            if (ips.isEmpty()) {
                err(sender, Colours.WARNING + "No IP on record for " + Colours.BRAND_ACCENT_SECONDARY + args[0] + Colours.WARNING + ".");
                return;
            }
            ip = ips.get(0);
            labelName = bestName(u, args[0]);
        }
        List<UUID> users = dao().usersOfIp(ip);
        if (users.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + "No accounts share that IP.");
            return;
        }
        long now = System.currentTimeMillis();
        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Accounts sharing " + Colours.BRAND_ACCENT_SECONDARY + labelName + Colours.BRAND_ACCENT_SECONDARY + "'s IP (" + Colours.BRAND_ACCENT_SECONDARY + users.size() + Colours.BRAND_ACCENT_SECONDARY + ").");
        for (UUID u : users) {
            String name = dao().nameOf(u);
            if (name == null) {
                name = u.toString().substring(0, 8);
            }
            Entry b = dao().activeBan(u);
            Entry m = dao().activeMute(u);
            String flag = "";
            if (b != null && b.inForce(now)) {
                flag = " " + Colours.WARNING + "(banned)";
            } else if (m != null && m.inForce(now)) {
                flag = " " + Colours.BRAND_ACCENT_SECONDARY + "(muted)";
            }
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  " + Colours.BRAND_ACCENT_SECONDARY + name + flag);
        }
    }

    public void iphistory(CommandSender sender, String[] args) {
        UUID u = lookupTarget(sender, args, "iphistory");
        if (u == null) {
            return;
        }
        List<String> ips = dao().ipsOf(u);
        if (ips.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + "No IP history on record.");
            return;
        }
        send(sender, Colours.BRAND_ACCENT_SECONDARY + "IP history for " + Colours.BRAND_ACCENT_SECONDARY + bestName(u, args[0]) + Colours.BRAND_ACCENT_SECONDARY + ".");
        for (String ip : ips) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  " + Colours.BRAND_ACCENT_SECONDARY + ip);
        }
    }

    public void namehistory(CommandSender sender, String[] args) {
        UUID u = lookupTarget(sender, args, "namehistory");
        if (u == null) {
            return;
        }
        List<String> names = dao().namesOf(u);
        if (names.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + "No name history on record.");
            return;
        }
        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Name history for " + Colours.BRAND_ACCENT_SECONDARY + bestName(u, args[0]) + Colours.BRAND_ACCENT_SECONDARY + ".");
        for (String n : names) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  " + Colours.BRAND_ACCENT_SECONDARY + n);
        }
    }

    public void lastuuid(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "lastuuid <player>", "show the UUID last seen for a name");
            return;
        }
        UUID u = dao().uuidByName(args[0]);
        if (u == null) {
            err(sender, Colours.WARNING + "No UUID on record for " + Colours.BRAND_ACCENT_SECONDARY + args[0] + Colours.WARNING + ".");
            return;
        }
        send(sender, Colours.BRAND_ACCENT_SECONDARY + args[0] + " " + Colours.BRAND_ACCENT_SECONDARY + "-> " + Colours.BRAND_ACCENT_SECONDARY + u);
    }

    public void listActive(CommandSender sender, String[] args, Entry.Type type) {
        String word = type == Entry.Type.BAN ? "ban" : "mute";
        long now = System.currentTimeMillis();
        List<Entry> all = dao().activeOfType(type, now);
        if (all.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + "There are no active " + word + "s.");
            return;
        }
        int page = args.length > 0 ? Math.max(1, parseIntOr(args[0], 1)) : 1;
        int pages = (all.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        page = Math.min(page, pages);
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, all.size());
        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Active " + word + "s (" + Colours.BRAND_ACCENT_SECONDARY + all.size()
                + Colours.BRAND_ACCENT_SECONDARY + ") - page " + Colours.BRAND_ACCENT_SECONDARY + page + Colours.BRAND_ACCENT_SECONDARY + "/" + Colours.BRAND_ACCENT_SECONDARY + pages + Colours.BRAND_ACCENT_SECONDARY + ".");
        for (int i = from; i < to; i++) {
            Entry e = all.get(i);
            String tname = e.uuid() != null ? bestName(e.uuid(), "?") : e.ip();
            String when = e.permanent() ? Colours.WARNING + "perm" : Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(e.remaining(now));
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  #" + e.randomId() + " " + Colours.BRAND_ACCENT_SECONDARY + tname + " " + Colours.BRAND_ACCENT_SECONDARY + "by " + Colours.BRAND_ACCENT_SECONDARY
                    + e.executorName() + " " + Colours.BRAND_ACCENT_SECONDARY + "(" + when + Colours.BRAND_ACCENT_SECONDARY + ") " + e.reason());
        }
    }

    private static final int HISTORY_PAGE_SIZE = 5;

    private static final String SORT_DATE_NEW = "date";
    private static final String SORT_DATE_OLD = "date-old";
    private static final String SORT_TYPE = "type";
    private static final String SORT_DURATION = "duration";

    public void history(CommandSender sender, String[] args) {
        UUID u = lookupTarget(sender, args, "history");
        if (u == null) {
            return;
        }
        List<Entry> hist = new java.util.ArrayList<>(dao().history(u, 500));
        if (hist.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + bestName(u, args[0]) + " " + Colours.BRAND_ACCENT_SECONDARY + "has no punishment history.");
            return;
        }
        long now = System.currentTimeMillis();
        int page = args.length > 1 ? Math.max(1, parseIntOr(args[1], 1)) : 1;
        String sort = args.length > 2 ? args[2].toLowerCase(Locale.ROOT) : SORT_DATE_NEW;
        sortHistory(hist, sort, now);

        int pages = (hist.size() + HISTORY_PAGE_SIZE - 1) / HISTORY_PAGE_SIZE;
        page = Math.min(page, pages);
        int from = (page - 1) * HISTORY_PAGE_SIZE;
        int to = Math.min(from + HISTORY_PAGE_SIZE, hist.size());

        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Punishment history for " + Colours.BRAND_ACCENT_SECONDARY + bestName(u, args[0])
                + " " + Colours.BRAND_ACCENT_SECONDARY + "(" + Colours.BRAND_ACCENT_SECONDARY + hist.size() + Colours.BRAND_ACCENT_SECONDARY + ") - page "
                + Colours.BRAND_ACCENT_SECONDARY + page + Colours.BRAND_ACCENT_SECONDARY + "/" + Colours.BRAND_ACCENT_SECONDARY + pages + Colours.BRAND_ACCENT_SECONDARY + ".");
        for (int i = from; i < to; i++) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  " + historyLine(hist.get(i), now, args[0]));
        }
        raw(sender, "  <italic>" + Colours.BRAND_ACCENT_SECONDARY
                + "Hover a punishment for details, or click it to open the full record.</italic>");
        raw(sender, navBar(args[0], page, pages, sort));
    }

    public void punishment(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "punishment <id>", "show everything recorded about one punishment");
            return;
        }
        String id = args[0].startsWith("#") ? args[0].substring(1) : args[0];
        Entry found = null;
        if (args.length > 1) {
            UUID owner = resolve(args[1]);
            if (owner != null) {
                found = findById(dao().history(owner, 500), id);
            }
        }
        if (found == null && sender instanceof Player self) {
            found = findById(dao().history(self.getUniqueId(), 500), id);
        }
        if (found == null) {
            err(sender, Colours.WARNING + "No punishment found with ID " + Colours.BRAND_ACCENT_SECONDARY + "#" + id + Colours.WARNING + ".");
            return;
        }
        long now = System.currentTimeMillis();
        String target = found.uuid() != null ? bestName(found.uuid(), "?") : found.ip();
        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Punishment " + Colours.WARNING + "#" + found.randomId()
                + Colours.BRAND_ACCENT_SECONDARY + " against " + Colours.BRAND_ACCENT_SECONDARY + target + Colours.BRAND_ACCENT_SECONDARY + ".");
        for (String line : compendium(found, now).split("<newline>")) {
            raw(sender, "  " + line);
        }
    }

    private static Entry findById(List<Entry> pool, String id) {
        for (Entry e : pool) {
            if (e.randomId() != null && e.randomId().equalsIgnoreCase(id)) {
                return e;
            }
        }
        return null;
    }

    private void sortHistory(List<Entry> hist, String sort, long now) {
        switch (sort) {
            case SORT_DATE_OLD -> hist.sort(java.util.Comparator.comparingLong(Entry::dateStart));
            case SORT_TYPE -> hist.sort(java.util.Comparator
                    .comparing((Entry e) -> e.type().id())
                    .thenComparing(java.util.Comparator.comparingLong(Entry::dateStart).reversed()));
            case SORT_DURATION -> hist.sort(java.util.Comparator
                    .comparingLong((Entry e) -> e.permanent() ? Long.MAX_VALUE : e.dateEnd() - e.dateStart())
                    .reversed());
            default -> hist.sort(java.util.Comparator.comparingLong(Entry::dateStart).reversed());
        }
    }

    private String navBar(String target, int page, int pages, String sort) {
        StringBuilder sb = new StringBuilder("  ");
        sb.append(pageButton("Back", target, page - 1, sort, page > 1));
        sb.append(' ');
        sb.append(pageButton("Next", target, page + 1, sort, page < pages));
        sb.append(' ');
        String dateNext = SORT_DATE_NEW.equals(sort) ? SORT_DATE_OLD : SORT_DATE_NEW;
        sb.append(sortButton("Date", target, dateNext,
                SORT_DATE_NEW.equals(sort) || SORT_DATE_OLD.equals(sort)));
        sb.append(' ');
        sb.append(sortButton("Punishment", target, SORT_TYPE, SORT_TYPE.equals(sort)));
        sb.append(' ');
        sb.append(sortButton("Duration", target, SORT_DURATION, SORT_DURATION.equals(sort)));
        return sb.toString();
    }

    private String pageButton(String label, String target, int toPage, String sort, boolean live) {
        if (!live) {
            return "<reset>" + Colours.BRAND_ACCENT + "[" + label + "]";
        }
        return "<reset><click:run_command:'/history " + target + " " + toPage + " " + sort + "'>"
                + "<hover:show_text:'" + Colours.BRAND_ACCENT_SECONDARY + "Go to page " + toPage + "'>"
                + Colours.BRAND + "[" + label + "]</hover></click>";
    }

    private String sortButton(String label, String target, String toSort, boolean current) {
        String colour = current ? Colours.BRAND_ACCENT_SECONDARY : Colours.BRAND;
        String hint = SORT_DATE_OLD.equals(toSort) ? "oldest first"
                : SORT_DATE_NEW.equals(toSort) ? "newest first"
                : SORT_TYPE.equals(toSort) ? "grouped by punishment"
                : "longest first";
        return "<reset><click:run_command:'/history " + target + " 1 " + toSort + "'>"
                + "<hover:show_text:'" + Colours.BRAND_ACCENT_SECONDARY + "Sort by " + hint + "'>"
                + colour + "[Sort by " + label + "]</hover></click>";
    }

    public void staffhistory(CommandSender sender, String[] args) {
        UUID u = lookupTarget(sender, args, "staffhistory");
        if (u == null) {
            return;
        }
        List<Entry> hist = dao().byStaff(u, 40);
        if (hist.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + bestName(u, args[0]) + " " + Colours.BRAND_ACCENT_SECONDARY + "has issued no punishments.");
            return;
        }
        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Punishments issued by " + Colours.BRAND_ACCENT_SECONDARY + bestName(u, args[0])
                + " " + Colours.BRAND_ACCENT_SECONDARY + "(" + Colours.BRAND_ACCENT_SECONDARY + hist.size() + Colours.BRAND_ACCENT_SECONDARY + ").");
        long now = System.currentTimeMillis();
        for (Entry e : hist) {
            String tname = e.uuid() != null ? bestName(e.uuid(), "?") : e.ip();
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  [" + e.type().id() + "] " + Colours.BRAND_ACCENT_SECONDARY + tname + " " + statusWord(e, now)
                    + " " + Colours.BRAND_ACCENT_SECONDARY + e.reason());
        }
    }

    private String historyLine(Entry e, long now, String owner) {
        String body = Colours.WARNING + e.type().id().toUpperCase(Locale.ROOT) + " " + statusWord(e, now)
                + Colours.WARNING + " by " + e.executorName() + " " + e.reason() + " (#" + e.randomId() + ")";
        return "<reset><click:run_command:'/punishment " + e.randomId() + " " + owner + "'>"
                + "<hover:show_text:'" + compendium(e, now) + "'>" + body + "</hover></click>";
    }

    private String compendium(Entry e, long now) {
        StringBuilder sb = new StringBuilder();
        sb.append(Colours.WARNING).append(e.type().id().toUpperCase(Locale.ROOT))
                .append(" #").append(e.randomId());
        sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Issued by ")
                .append(Colours.BRAND_ACCENT_SECONDARY).append(e.executorName());
        sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Reason ")
                .append(Colours.BRAND_ACCENT_SECONDARY).append(e.reason());
        sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Started ")
                .append(Colours.BRAND_ACCENT_SECONDARY).append(stamp(e.dateStart()));
        if (e.permanent()) {
            sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Duration ")
                    .append(Colours.WARNING).append("permanent");
        } else {
            sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Duration ")
                    .append(Colours.BRAND_ACCENT_SECONDARY).append(SauverFormat.fancyTime(e.duration()));
            sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Ends ")
                    .append(Colours.BRAND_ACCENT_SECONDARY).append(stamp(e.dateEnd()));
        }
        if (e.removedByName() != null) {
            sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Removed by ")
                    .append(Colours.BRAND_ACCENT_SECONDARY).append(e.removedByName());
            if (e.removalReason() != null && !e.removalReason().isBlank()) {
                sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Removal reason ")
                        .append(Colours.BRAND_ACCENT_SECONDARY).append(e.removalReason());
            }
        }
        if (e.ip() != null && !e.ip().isBlank()) {
            sb.append("<newline>").append(Colours.BRAND_ACCENT).append("IP ")
                    .append(Colours.BRAND_ACCENT_SECONDARY).append(e.ip());
        }
        if (e.serverOrigin() != null && !e.serverOrigin().isBlank()) {
            sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Server ")
                    .append(Colours.BRAND_ACCENT_SECONDARY).append(e.serverOrigin());
        }
        if (e.silent()) {
            sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Issued silently");
        }
        if (e.ipban()) {
            sb.append("<newline>").append(Colours.BRAND_ACCENT).append("Applied to the whole IP");
        }
        return sb.toString().replace("'", "’");
    }

    private static String stamp(long millis) {
        return new java.text.SimpleDateFormat("d MMM yyyy, HH:mm").format(new java.util.Date(millis));
    }

    private String statusWord(Entry e, long now) {
        if (e.removedByName() != null) {
            return Colours.BRAND_ACCENT + "(removed by " + e.removedByName() + ")";
        }
        if (e.type() == Entry.Type.KICK || e.type() == Entry.Type.WARNING) {
            return e.expired(now) ? Colours.BRAND_ACCENT + "(expired)" : Colours.SUCCESS + "(active)";
        }
        if (!e.active()) {
            return Colours.BRAND_ACCENT + "(expired)";
        }
        return e.inForce(now) ? Colours.SUCCESS + "(active)" : Colours.BRAND_ACCENT + "(expired)";
    }

    public void staffrollback(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "staffrollback <staff> [duration]", "reverse a staff member's recent punishments");
            return;
        }
        UUID staff = resolve(args[0]);
        if (staff == null) {
            unknownPlayer(sender, args[0]);
            return;
        }
        long now = System.currentTimeMillis();
        long cutoff = 0;
        if (args.length > 1) {
            long window = SauverDuration.parse(args[1]);
            if (window == -1 || window == Entry.PERMANENT) {
                err(sender, Colours.WARNING + "That duration is invalid: " + Colours.BRAND_ACCENT_SECONDARY + args[1] + Colours.WARNING + ".");
                return;
            }
            cutoff = now - window;
        }
        List<Entry> toLift = dao().activeByStaffSince(staff, cutoff, now);
        if (toLift.isEmpty()) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + "No active punishments by " + Colours.BRAND_ACCENT_SECONDARY + bestName(staff, args[0]) + " " + Colours.BRAND_ACCENT_SECONDARY + "to roll back.");
            return;
        }
        int lifted = 0;
        for (Entry e : toLift) {
            SauverEngine.Result r = SauverEngine.pardon(e.type(), e.uuid(),
                    e.uuid() != null ? bestName(e.uuid(), "?") : e.ip(),
                    executorUuid(sender), executorName(sender), "Staff rollback of " + bestName(staff, args[0]));
            if (r.ok()) {
                lifted++;
            }
        }
        send(sender, Colours.BRAND + "You rolled back " + Colours.BRAND_ACCENT_SECONDARY + lifted + " " + Colours.BRAND + "punishments by " + Colours.BRAND_ACCENT_SECONDARY
                + bestName(staff, args[0]) + Colours.BRAND + ".");
    }

    public void prunehistory(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "prunehistory <player> [duration]", "delete a player's inactive punishment history");
            return;
        }
        UUID u = resolve(args[0]);
        if (u == null) {
            unknownPlayer(sender, args[0]);
            return;
        }
        long now = System.currentTimeMillis();
        long cutoff = 0;
        if (args.length > 1) {
            long window = SauverDuration.parse(args[1]);
            if (window == -1 || window == Entry.PERMANENT) {
                err(sender, Colours.WARNING + "That duration is invalid: " + Colours.BRAND_ACCENT_SECONDARY + args[1] + Colours.WARNING + ".");
                return;
            }
            cutoff = now - window;
        }
        int removed = dao().pruneHistory(u, cutoff, now);
        send(sender, Colours.BRAND + "You pruned " + Colours.BRAND_ACCENT_SECONDARY + removed + " " + Colours.BRAND + "inactive records for " + Colours.BRAND_ACCENT_SECONDARY
                + bestName(u, args[0]) + Colours.BRAND + ".");
    }

    public void whois(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "whois <player>", "show a player's full account dossier");
            return;
        }
        String query = args[0];

        Player online = Bukkit.getPlayerExact(query);
        if (online != null) {
            renderWhois(sender, online.getUniqueId(), online.getName());
            return;
        }
        UUID known = resolve(query);
        if (known != null && dao().hasProfile(known)) {
            renderWhois(sender, known, bestName(known, query));
            return;
        }

        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Resolving " + Colours.BRAND_ACCENT_SECONDARY + query + " " + Colours.BRAND_ACCENT_SECONDARY + "via Mojang...");
        teacommontea.util.sched.Sched.executeAsync(() -> {
            SauverMojang.Profile prof = SauverMojang.lookup(query);
            Runnable render = () -> {
                switch (prof.status()) {
                    case FOUND -> {
                        if (dao().hasProfile(prof.uuid())) {
                            renderWhois(sender, prof.uuid(), prof.name());
                        } else {
                            send(sender, Colours.BRAND_ACCENT_SECONDARY + "Whois " + Colours.BRAND_ACCENT_SECONDARY + prof.name()
                                    + " " + Colours.BRAND_ACCENT_SECONDARY + "(never joined this server).");
                            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  UUID: " + Colours.BRAND_ACCENT_SECONDARY + prof.uuid());
                            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Source: " + Colours.BRAND_ACCENT_SECONDARY + "Mojang " + Colours.BRAND_ACCENT_SECONDARY + "(no local history)");
                        }
                    }
                    case NOT_FOUND -> err(sender, Colours.WARNING + "No Minecraft account exists with the name " + Colours.BRAND_ACCENT_SECONDARY
                            + query + Colours.WARNING + ".");
                    case UNKNOWN -> err(sender, Colours.WARNING + "Could not reach Mojang to resolve " + Colours.BRAND_ACCENT_SECONDARY
                            + query + Colours.WARNING + ". Try again shortly.");
                }
            };
            if (sender instanceof org.bukkit.entity.Player p) {
                teacommontea.util.sched.Sched.executeFor(p, render);
            } else {
                teacommontea.util.sched.Sched.executeGlobal(render);
            }
        });
    }

    private void renderWhois(CommandSender sender, UUID u, String fallbackName) {
        SauverDAO.Profile pr = dao().profile(u);
        String name = pr.name() != null ? pr.name() : fallbackName;
        long now = System.currentTimeMillis();

        send(sender, Colours.BRAND_ACCENT_SECONDARY + "Whois " + Colours.BRAND_ACCENT_SECONDARY + name + (pr.online() ? " " + Colours.BRAND + "(online)" : "") + Colours.BRAND_ACCENT_SECONDARY + ".");
        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  UUID: " + Colours.BRAND_ACCENT_SECONDARY + u);

        if (pr.names().size() > 1) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Known names: " + Colours.BRAND_ACCENT_SECONDARY + String.join(Colours.BRAND_ACCENT_SECONDARY + ", " + Colours.BRAND_ACCENT_SECONDARY, pr.names()));
        }

        String version = SauverProtocol.versionName(pr.protocol());
        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Version: " + Colours.BRAND_ACCENT_SECONDARY + (version != null ? version : "unknown")
                + (pr.protocol() > 0 ? " " + Colours.BRAND_ACCENT_SECONDARY + "(protocol " + pr.protocol() + ")" : ""));

        String client = pr.lastClient() != null ? pr.lastClient() : "unknown";
        if (pr.clients().size() > 1) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Client: " + Colours.BRAND_ACCENT_SECONDARY + client
                    + " " + Colours.BRAND_ACCENT_SECONDARY + "(all: " + Colours.BRAND_ACCENT_SECONDARY + String.join(Colours.BRAND_ACCENT_SECONDARY + ", " + Colours.BRAND_ACCENT_SECONDARY, pr.clients()) + Colours.BRAND_ACCENT_SECONDARY + ")");
        } else {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Client: " + Colours.BRAND_ACCENT_SECONDARY + client);
        }

        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Referrer: " + Colours.BRAND_ACCENT_SECONDARY + (pr.referrer() != null ? pr.referrer() : "unknown"));

        String lastIp = pr.lastIp();
        if (lastIp != null) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Country: " + Colours.BRAND_ACCENT_SECONDARY + countryLabel(lastIp));
            if (sender.hasPermission("veritesauver.whois.ip")) {
                raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Last IP: " + Colours.BRAND_ACCENT_SECONDARY + lastIp
                        + " " + Colours.BRAND_ACCENT_SECONDARY + "(" + Colours.BRAND_ACCENT_SECONDARY + pr.ips().size() + " " + Colours.BRAND_ACCENT_SECONDARY + "on record)");
            }
        }

        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Playtime: " + Colours.BRAND_ACCENT_SECONDARY + (pr.playtimeMs() > 0
                ? SauverFormat.fancyTime(pr.playtimeMs()) : "none")
                + " " + Colours.BRAND_ACCENT_SECONDARY + "over " + Colours.BRAND_ACCENT_SECONDARY + pr.joinCount() + " " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.pluralize(pr.joinCount(), "join"));

        if (pr.firstJoin() > 0) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  First joined: " + Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(now - pr.firstJoin()) + " " + Colours.BRAND_ACCENT_SECONDARY + "ago");
        }
        if (pr.lastSeen() > 0) {
            raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Last seen: " + (pr.online() ? Colours.BRAND + "now"
                    : Colours.BRAND_ACCENT_SECONDARY + SauverFormat.fancyTime(now - pr.lastSeen()) + " " + Colours.BRAND_ACCENT_SECONDARY + "ago"));
        }

        String punishColor = pr.punishments() > 0 ? Colours.WARNING : Colours.BRAND_ACCENT_SECONDARY;
        raw(sender, Colours.BRAND_ACCENT_SECONDARY + "  Punishments: " + punishColor + pr.punishments()
                + (pr.punishments() > 0 ? " " + Colours.BRAND_ACCENT_SECONDARY + "(see " + Colours.BRAND_ACCENT_SECONDARY + "/history " + name + Colours.BRAND_ACCENT_SECONDARY + ")" : ""));
    }

    private String countryLabel(String ip) {
        if (!SauverGeoIp.available()) {
            return "geoip off " + Colours.BRAND_ACCENT_SECONDARY + "(no GeoLite2 database)";
        }
        if (SauverGeoIp.isPrivate(ip)) {
            return "unknown " + Colours.BRAND_ACCENT_SECONDARY + "(local/proxy IP, enable IP forwarding)";
        }
        String country = SauverGeoIp.country(ip);
        return country != null ? country : "unknown";
    }

    public void seen(CommandSender sender, String[] args) {
        if (args.length == 0) {
            usage(sender, "seen <player>", "show when a player was last online");
            return;
        }
        Player online = Bukkit.getPlayerExact(args[0]);
        if (online != null) {
            send(sender, Colours.BRAND_ACCENT_SECONDARY + online.getName() + " " + Colours.BRAND_ACCENT_SECONDARY + "is online now.");
            return;
        }
        OfflinePlayer off = Bukkit.getOfflinePlayer(args[0]);
        long lastSeen = lastSeenMillis(off);
        if (lastSeen <= 0 && !off.hasPlayedBefore()) {
            err(sender, Colours.WARNING + "That player has never joined the server.");
            return;
        }
        long ago = System.currentTimeMillis() - lastSeen;
        send(sender, Colours.BRAND_ACCENT_SECONDARY + args[0] + " " + Colours.BRAND_ACCENT_SECONDARY + "was last seen " + Colours.BRAND_ACCENT_SECONDARY
                + SauverFormat.fancyTime(ago) + " " + Colours.BRAND_ACCENT_SECONDARY + "ago.");
    }

    @SuppressWarnings("deprecation")
    private static long lastSeenMillis(OfflinePlayer off) {
        try {
            return (long) OfflinePlayer.class.getMethod("getLastSeen").invoke(off);
        } catch (Throwable notPaper) {
            return off.getLastPlayed();
        }
    }

    public List<String> tabWhois(CommandSender sender, String[] args) {
        if (!sender.hasPermission("veritesauver.whois") || args.length != 1) {
            return List.of();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>(matchOnline(args[0]));
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (String n : out) {
            seen.add(n.toLowerCase(Locale.ROOT));
        }
        for (String n : dao().knownNames()) {
            if (out.size() >= 50) {
                break;
            }
            if (n.startsWith(prefix) && seen.add(n)) {
                out.add(n);
            }
        }
        return out;
    }
}
