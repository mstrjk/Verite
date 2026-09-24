package teacommontea.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import teacommontea.util.VeriteFlags;
import teacommontea.util.Lang;
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
            teacommontea.util.text.Send.to(sender, m.prefixed(Lang.of("deny.permission")));
            return true;
        }
        if (args.length == 0) {
            teacommontea.util.text.Send.to(sender, m.prefixed(
                    Lang.of("flag.list.header", "count", VeriteFlags.names().size())));
            for (String name : VeriteFlags.names()) {
                teacommontea.util.text.Send.to(sender, m.parse(Lang.of("flag.list.entry", "name", name)));
            }
            teacommontea.util.text.Send.to(sender, m.prefixed(Lang.of("flag.usage")));
            return true;
        }
        VeriteFlags.Flag flag = VeriteFlags.flag(args[0]);
        if (flag == null) {
            teacommontea.util.text.Send.to(sender, m.prefixed(Lang.of("flag.unknown", "name", args[0])));
            return true;
        }
        if (args.length == 1) {
            String current = VeriteFlags.get(plugin, flag);
            teacommontea.util.text.Send.to(sender, m.prefixed(current == null
                    ? Lang.of("flag.value.unset", "name", flag.name())
                    : Lang.of("flag.value", "name", flag.name(), "value", current)));
            teacommontea.util.text.Send.to(sender, m.parse(Lang.of("flag.accepts", "options", accepted(flag))));
            return true;
        }
        String value = args[1];
        boolean ok = VeriteFlags.set(plugin, flag, value);
        if (!ok) {
            teacommontea.util.text.Send.to(sender, m.prefixed(Lang.of("flag.set.failed",
                    "name", flag.name(), "value", value, "options", accepted(flag))));
            return true;
        }
        teacommontea.util.text.Send.to(sender, m.prefixed(
                Lang.of("flag.set.ok", "name", flag.name(), "value", value)));
        if (flag.isConfigGate()) {
            teacommontea.util.text.Send.to(sender, m.parse(Lang.of("flag.applied.instant")));
        } else {
            onReload.run();
            teacommontea.util.text.Send.to(sender, m.parse(Lang.of("flag.applied.live")));
        }
        return true;
    }

    private static String accepted(VeriteFlags.Flag flag) {
        return String.join(" " + Colours.BRAND_ACCENT + "| " + Colours.BRAND_ACCENT_SECONDARY,
                VeriteFlags.suggest(flag));
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
