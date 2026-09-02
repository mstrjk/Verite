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
            String state = SauverLockdown.active()
                    ? Colours.WARNING + "ACTIVE " + Colours.BRAND_ACCENT_SECONDARY + "- " + SauverLockdown.reason()
                    : Colours.BRAND + "off";
            send(sender, Colours.BRAND_ACCENT_SECONDARY + "Lockdown is " + state + Colours.BRAND_ACCENT_SECONDARY + ".");
            usage(sender, "lockdown <reason>", "seal the server");
            usage(sender, "lockdown end", "lift the lockdown");
            return;
        }
        if (args[0].equalsIgnoreCase("end") || args[0].equalsIgnoreCase("off")) {
            if (!SauverLockdown.active()) {
                err(sender, Colours.WARNING + "The server is not in lockdown.");
                return;
            }
            SauverLockdown.end();
            send(sender, Colours.BRAND + "Lockdown lifted. Players may join again.");
            return;
        }
        String reason = String.join(" ", args);
        SauverLockdown.begin(reason);
        send(sender, Colours.WARNING + "The server is now in lockdown. " + Colours.BRAND_ACCENT_SECONDARY + "Reason: " + Colours.BRAND_ACCENT_SECONDARY + reason);
    }

    public List<String> lockdownTab(CommandSender sender, String[] args) {
        if (!sender.hasPermission("veritesauver.lockdown")) {
            return List.of();
        }
        return args.length == 1 ? prefixed(List.of("end"), args[0]) : List.of();
    }
}
