package teacommontea.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import teacommontea.util.VeriteFlags;
import teacommontea.util.Messages;

public final class FlagCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "veritedoux.flag";

    private final Plugin plugin;
    private final Runnable onReload;
    private final Messages messages = new Messages();

    public FlagCommand(Plugin plugin, Runnable onReload) {
        this.plugin = plugin;
        this.onReload = onReload;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages m = messages;
        if (!sender.hasPermission(PERMISSION)) {
            sender.spigot().sendMessage(m.prefixed(Messages.DENY_PERMISSION));
            return true;
        }
        if (args.length == 0) {
            sender.spigot().sendMessage(m.prefixed(Colours.BRAND + "Editable flags " + Colours.BRAND_ACCENT + "(" + VeriteFlags.names().size() + "):"));
            for (String name : VeriteFlags.names()) {
                sender.spigot().sendMessage(m.parse("  " + Colours.BRAND_ACCENT + "- " + Colours.BRAND_ACCENT_SECONDARY + name));
            }
            sender.spigot().sendMessage(m.prefixed(Colours.BRAND_ACCENT_SECONDARY + "Usage: " + Colours.BRAND_ACCENT + "/veriteflag <flag> [value]"));
            return true;
        }
        VeriteFlags.Flag flag = VeriteFlags.flag(args[0]);
        if (flag == null) {
            sender.spigot().sendMessage(m.prefixed(Colours.WARNING + "Unknown flag " + Colours.BRAND_ACCENT_SECONDARY + args[0] + Colours.WARNING + "."));
            return true;
        }
        if (args.length == 1) {
            String current = VeriteFlags.get(plugin, flag);
            sender.spigot().sendMessage(m.prefixed(Colours.BRAND_ACCENT_SECONDARY + flag.name() + " " + Colours.BRAND_ACCENT + "= " + Colours.SUCCESS
                    + (current == null ? Colours.WARNING + "unset" : current)));
            sender.spigot().sendMessage(m.parse("  " + Colours.BRAND_ACCENT_SECONDARY + "accepts: " + Colours.BRAND_ACCENT_SECONDARY
                    + String.join(" " + Colours.BRAND_ACCENT + "| " + Colours.BRAND_ACCENT_SECONDARY, VeriteFlags.suggest(flag))));
            return true;
        }
        String value = args[1];
        boolean ok = VeriteFlags.set(plugin, flag, value);
        if (!ok) {
            sender.spigot().sendMessage(m.prefixed(Colours.WARNING + "Could not set " + Colours.BRAND_ACCENT_SECONDARY + flag.name()
                    + Colours.WARNING + " to " + Colours.BRAND_ACCENT_SECONDARY + value + Colours.WARNING + ". Accepted: " + Colours.BRAND_ACCENT_SECONDARY
                    + String.join(" " + Colours.BRAND_ACCENT + "| " + Colours.BRAND_ACCENT_SECONDARY, VeriteFlags.suggest(flag))));
            return true;
        }
        sender.spigot().sendMessage(m.prefixed(Colours.SUCCESS + "Set " + Colours.BRAND_ACCENT_SECONDARY + flag.name()
                + " " + Colours.BRAND_ACCENT + "= " + Colours.BRAND_ACCENT_SECONDARY + value + Colours.BRAND_ACCENT + "."));
        if (flag.isConfigGate()) {
            sender.spigot().sendMessage(m.parse("  " + Colours.BRAND_ACCENT_SECONDARY + "This change is instant and applies right away."));
        } else {
            onReload.run();
            sender.spigot().sendMessage(m.parse("  " + Colours.BRAND_ACCENT_SECONDARY + "Applied live; the change is active now."));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Complete.prefix(VeriteFlags.names(), args[0]);
        }
        if (args.length == 2) {
            VeriteFlags.Flag flag = VeriteFlags.flag(args[0]);
            if (flag != null) {
                return Complete.prefix(VeriteFlags.suggest(flag), args[1]);
            }
        }
        return new ArrayList<>();
    }
}
