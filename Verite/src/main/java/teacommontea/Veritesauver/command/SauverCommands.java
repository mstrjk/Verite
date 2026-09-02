package teacommontea.veritesauver.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.util.SauverConfig;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.geoip.GeoCommands;
import teacommontea.veritesauver.lockdown.LockdownCommands;
import teacommontea.veritesauver.lookup.LookupCommands;
import teacommontea.veritesauver.punish.PunishCommands;
import teacommontea.veritesauver.punish.PunishTreeCommand;
import teacommontea.veritesauver.warn.WarnCommands;

public final class SauverCommands implements CommandExecutor, TabCompleter {

    private final Sauver sauver;
    private final PunishCommands punish;
    private final WarnCommands warn;
    private final LookupCommands lookup;
    private final LockdownCommands lockdown;
    private final GeoCommands geo;
    private final PunishTreeCommand tree;

    public SauverCommands(Sauver sauver) {
        this.sauver = sauver;
        this.punish = new PunishCommands(sauver);
        this.warn = new WarnCommands(sauver);
        this.lookup = new LookupCommands(sauver);
        this.lockdown = new LockdownCommands(sauver);
        this.geo = new GeoCommands(sauver);
        this.tree = new PunishTreeCommand(sauver);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!SauverConfig.moderationEnabled()) {
            sauver.messages().err(sender, "Moderation is currently disabled.");
            return true;
        }
        String cmd = command.getName().toLowerCase(Locale.ROOT);
        switch (cmd) {
            case "ban"          -> punish.issueBanOrMute(sender, args, Entry.Type.BAN, false);
            case "tempban"      -> punish.issueBanOrMute(sender, args, Entry.Type.BAN, true);
            case "mute"         -> punish.issueBanOrMute(sender, args, Entry.Type.MUTE, false);
            case "tempmute"     -> punish.issueBanOrMute(sender, args, Entry.Type.MUTE, true);
            case "ipban"        -> punish.issueIp(sender, args, Entry.Type.BAN);
            case "ipmute"       -> punish.issueIp(sender, args, Entry.Type.MUTE);
            case "kick"         -> punish.kick(sender, args);
            case "unban"        -> punish.pardon(sender, args, Entry.Type.BAN);
            case "unmute"       -> punish.pardon(sender, args, Entry.Type.MUTE);
            case "checkban"     -> punish.check(sender, args, Entry.Type.BAN);
            case "checkmute"    -> punish.check(sender, args, Entry.Type.MUTE);
            case "warn"         -> warn.warn(sender, args);
            case "unwarn"       -> warn.unwarn(sender, args);
            case "warnings"     -> warn.warnings(sender, args);
            case "checkwarn"    -> warn.checkwarn(sender, args);
            case "warnlist"     -> warn.warnlist(sender, args);
            case "dupeip"       -> lookup.dupeip(sender, args);
            case "iphistory"    -> lookup.iphistory(sender, args);
            case "namehistory"  -> lookup.namehistory(sender, args);
            case "lastuuid"     -> lookup.lastuuid(sender, args);
            case "banlist"      -> lookup.listActive(sender, args, Entry.Type.BAN);
            case "mutelist"     -> lookup.listActive(sender, args, Entry.Type.MUTE);
            case "history"      -> lookup.history(sender, args);
            case "staffhistory" -> lookup.staffhistory(sender, args);
            case "staffrollback"-> lookup.staffrollback(sender, args);
            case "prunehistory" -> lookup.prunehistory(sender, args);
            case "whois"        -> lookup.whois(sender, args);
            case "seen"         -> lookup.seen(sender, args);
            case "lockdown"     -> lockdown.lockdown(sender, args);
            case "geoip"        -> geo.geoip(sender, args);
            case "bc", "broadcast" -> sauver.chat().broadcast(sender, args);
            case "chatclear", "cc" -> sauver.chat().chatClear(sender);
            case "chatmute"     -> sauver.chat().chatMute(sender);
            case "slowmode"     -> sauver.chat().slowmode(sender, args);
            case "punish"       -> tree.open(sender, args);
            default -> { return false; }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase(Locale.ROOT);
        return switch (cmd) {
            case "ban", "mute"      -> punish.tabIssue(sender, "veritesauver." + cmd, args, false);
            case "tempban"          -> punish.tabIssue(sender, "veritesauver.ban", args, true);
            case "tempmute"         -> punish.tabIssue(sender, "veritesauver.mute", args, true);
            case "ipban"            -> punish.tabTargetAll(sender, "veritesauver.ipban", args);
            case "ipmute"           -> punish.tabTargetAll(sender, "veritesauver.ipmute", args);
            case "kick"             -> punish.tabTargetAll(sender, "veritesauver.kick", args);
            case "warn"             -> punish.tabTargetAll(sender, "veritesauver.warn", args);
            case "unban"            -> punish.tabPardon(sender, "veritesauver.unban", args, Entry.Type.BAN);
            case "unmute"           -> punish.tabPardon(sender, "veritesauver.unmute", args, Entry.Type.MUTE);
            case "unwarn"           -> warn.tabUnwarn(sender, "veritesauver.unwarn", args);
            case "whois"            -> lookup.tabWhois(sender, args);
            case "warnings", "checkwarn", "checkban", "checkmute", "dupeip",
                 "iphistory", "namehistory", "lastuuid", "history", "staffhistory",
                 "staffrollback", "prunehistory", "seen"
                                    -> punish.tabTarget(sender, "veritesauver." + cmd, args);
            case "lockdown"         -> lockdown.lockdownTab(sender, args);
            case "slowmode"         -> sender.hasPermission("veritesauver.slowmode")
                                            ? sauver.chat().slowmodeTab(args.length) : List.of();
            case "punish"           -> tree.tab(sender, args);
            default -> List.of();
        };
    }
}
