package teacommontea.veritesauver.punish;

import teacommontea.util.Colours;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

import teacommontea.veritesauver.Sauver;
import teacommontea.veritesauver.command.CommandBase;

public final class PunishTreeCommand extends CommandBase {

    public PunishTreeCommand(Sauver sauver) {
        super(sauver);
    }

    public void open(CommandSender sender, String[] args) {
        if (!(sender instanceof Player staff)) {
            err(sender, Colours.WARNING + "The punishment tree can only be opened by a player.");
            return;
        }
        if (!sauver.tree().canOpen(staff)) {
            err(sender, Colours.WARNING + "You don't have permission to do that.");
            return;
        }
        if (args.length == 0) {
            sauver.tree().open(staff, null, null);
            return;
        }
        UUID target = resolve(args[0]);
        if (target == null) {
            unknownPlayer(sender, args[0]);
            return;
        }
        if (target.equals(staff.getUniqueId())) {
            err(sender, Colours.WARNING + "You cannot punish yourself.");
            return;
        }
        sauver.tree().open(staff, target, bestName(target, args[0]));
    }

    public List<String> tab(CommandSender sender, String[] args) {
        if (args.length != 1 || !(sender instanceof Player staff) || !sauver.tree().canOpen(staff)) {
            return List.of();
        }
        return matchOnline(args[0]);
    }
}
