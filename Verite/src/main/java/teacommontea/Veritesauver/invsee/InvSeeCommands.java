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
            Text.send(sender, teacommontea.util.Lang.of("invsee.players.only"));
            return;
        }
        if (args.length == 0 || args[0].isEmpty()) {
            Text.send(spectator, (
                    teacommontea.util.Lang.of("invsee.usage", "command", ender ? "endersee" : "invsee")));
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
                Text.send(spectator, teacommontea.util.Lang.of("invsee.open.error", "name", escape(raw),
                        "what", teacommontea.util.Lang.of(ender ? "invsee.what.enderchest" : "invsee.what.inventory")));
                core.plugin().getLogger().log(java.util.logging.Level.SEVERE, teacommontea.util.Lang.of("invsee.open.failed"), error);
                return;
            }
            if (result.isSuccess()) {
                return;
            }
            Text.send(spectator, message(result.reason(), escape(raw), ender));
        });
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String message(SpectateResult.Reason reason, String raw, boolean ender) {
        String what = teacommontea.util.Lang.of(ender ? "invsee.what.enderchest" : "invsee.what.inventory");
        return switch (reason) {
            case TARGET_DOES_NOT_EXIST -> teacommontea.util.Lang.of("invsee.player.unknown", "name", raw);
            case UNKNOWN_TARGET -> teacommontea.util.Lang.of("invsee.player.never.joined", "name", raw);
            case TARGET_EXEMPT -> teacommontea.util.Lang.of("invsee.player.exempt", "name", raw);
            case OFFLINE_SUPPORT_DISABLED -> teacommontea.util.Lang.of(ender
                    ? "invsee.offline.disabled.enderchest" : "invsee.offline.disabled.inventory");
            case OPEN_CANCELLED -> teacommontea.util.Lang.of("invsee.blocked.by.plugin", "name", raw, "what", what);
            default -> teacommontea.util.Lang.of("invsee.open.unknown", "name", raw, "what", what);
        };
    }
}
