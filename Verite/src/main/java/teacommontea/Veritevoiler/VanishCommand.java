package teacommontea.veritevoiler;

import teacommontea.util.Colours;
import teacommontea.util.Lang;
import teacommontea.util.Complete;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import teacommontea.util.Messages;

import java.util.ArrayList;
import java.util.List;

public final class VanishCommand implements CommandExecutor, TabCompleter {

    private final Vanish vanish;
    private final Messages messages = new Messages();

    public VanishCommand(Vanish vanish) {
        this.vanish = vanish;
    }

    private void msg(CommandSender to, String tagged) {
        teacommontea.util.text.Send.to(to, messages.prefixed(tagged));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!vanish.enabled()) {
            msg(sender, Lang.of("vanish.disabled"));
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                msg(sender, Lang.of("console.name.player", "command", "vanish"));
                return true;
            }
            if (!p.hasPermission("verite.vanish")) {
                msg(sender, Lang.of("vanish.deny.self"));
                return true;
            }
            msg(sender, Lang.of(vanish.toggle(p) ? "vanish.now.hidden" : "vanish.now.visible"));
            return true;
        }

        if (!sender.hasPermission("verite.vanish.others")) {
            msg(sender, Lang.of("vanish.deny.others"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            msg(sender, Lang.of("player.offline.named", "name", args[0]));
            return true;
        }
        msg(sender, Lang.of(vanish.toggle(target) ? "vanish.other.hidden" : "vanish.other.visible",
                "name", target.getName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("verite.vanish.others")) {
            boolean canSeeVanished = Vanish.canSee(sender instanceof Player sp ? sp : null);
            return Complete.onlineNames(args[0],
                    p -> canSeeVanished || !vanish.isVanished(p.getUniqueId()));
        }
        return new ArrayList<>();
    }
}
