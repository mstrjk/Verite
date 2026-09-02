package teacommontea.veritesauver.invsee;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import teacommontea.util.text.Text;

import java.util.UUID;

import teacommontea.util.Colours;

public final class InvSeeCommands {


    private final InvSee core;

    public InvSeeCommands(InvSee core) {
        this.core = core;
    }

    public void invsee(CommandSender sender, String[] args) {
        run(sender, args, false);
    }

    public void endersee(CommandSender sender, String[] args) {
        run(sender, args, true);
    }

    private void run(CommandSender sender, String[] args, boolean ender) {
        if (!(sender instanceof Player spectator)) {
            Text.send(sender, (Colours.WARNING + "This command can only be used by players!"));
            return;
        }
        if (args.length == 0 || args[0].isEmpty()) {
            Text.send(spectator, (
                    Colours.WARNING + "Usage: /" + (ender ? "endersee" : "invsee") + " <username|uuid>"));
            return;
        }
        String raw = args[0];
        UUID uuid = null;
        String name = null;
        try {
            uuid = UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            name = raw;
        }

        boolean bypass = spectator.hasPermission(
                ender ? "drigz.staff.endersee.bypass-exempt" : "drigz.staff.invsee.bypass-exempt");

        var future = ender
                ? core.openEnder(spectator, raw, uuid, name, bypass)
                : core.openMain(spectator, raw, uuid, name, bypass);

        future.whenComplete((result, error) -> {
            if (error != null) {
                Text.send(spectator, (Colours.WARNING + "An error occurred while trying to open "
                        + escape(raw) + "'s " + (ender ? "ender chest." : "inventory.")));
                core.plugin().getLogger().log(java.util.logging.Level.SEVERE, "InvSee open failed", error);
                return;
            }
            if (result.isSuccess()) {
                return;
            }
            Text.send(spectator, (Colours.WARNING + escape(message(result.reason(), raw, ender))));
        });
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String message(SpectateResult.Reason reason, String raw, boolean ender) {
        String what = ender ? "ender chest" : "inventory";
        return switch (reason) {
            case TARGET_DOES_NOT_EXIST -> "Player " + raw + " does not exist.";
            case UNKNOWN_TARGET -> "Player " + raw + " has not logged onto the server yet.";
            case TARGET_EXEMPT -> "Player " + raw + " is exempted from being spectated.";
            case OFFLINE_SUPPORT_DISABLED -> "Spectating offline players' " + what + "s is disabled.";
            case OPEN_CANCELLED -> "Another plugin prevented you from spectating " + raw + "'s " + what + ".";
            default -> "Could not open " + raw + "'s " + what + " for an unknown reason.";
        };
    }
}
