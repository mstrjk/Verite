package teacommontea.veritesauver.lockdown;

import teacommontea.util.Colours;
import org.bukkit.command.CommandSender;

import java.util.List;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.command.CommandBase;

public final class LockdownCommands extends CommandBase {

    public LockdownCommands(Sauver sauver) {
        super(sauver);
    }

    public void lockdown(CommandSender sender, String[] args) {
        if (args.length == 0) {
            send(sender, SauverLockdown.active()
                    ? teacommontea.util.Lang.of("lockdown.state.active", "reason", SauverLockdown.reason())
                    : teacommontea.util.Lang.of("lockdown.state.off"));
            usage(sender, "lockdown <reason>", "seal the server");
            usage(sender, "lockdown end", "lift the lockdown");
            return;
        }
        if (args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("off")) {
            if (!SauverLockdown.active()) {
                err(sender, teacommontea.util.Lang.of("lockdown.not.active"));
                return;
            }
            SauverLockdown.end();
            send(sender, teacommontea.util.Lang.of("lockdown.lifted"));
            return;
        }
        String reason = String.join(" ", args);
        SauverLockdown.begin(reason);
        send(sender, teacommontea.util.Lang.of("lockdown.started", "reason", reason));
    }

    public List<String> lockdownTab(CommandSender sender, String[] args) {
        if (!sender.hasPermission("veritesauver.lockdown")) {
            return List.of();
        }
        return args.length == 1 ? prefixed(List.of("end"), args[0]) : List.of();
    }
}
