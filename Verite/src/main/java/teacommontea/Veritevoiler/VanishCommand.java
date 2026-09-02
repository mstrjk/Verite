package teacommontea.veritevoiler;

import teacommontea.util.Colours;
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

    private void msg(CommandSender to, String mini) {
        to.spigot().sendMessage(messages.prefixed(mini));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!vanish.enabled()) {
            msg(sender, Colours.WARNING + "Vanish is currently disabled.");
            return true;
        }
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                msg(sender, Colours.WARNING + "Console must name a player: " + Colours.BRAND_ACCENT_SECONDARY + "/vanish <player>");
                return true;
            }
            if (!p.hasPermission("verite.vanish")) {
                msg(sender, Colours.WARNING + "You may not vanish.");
                return true;
            }
            boolean nowVanished = vanish.toggle(p);
            msg(sender, nowVanished
                    ? Colours.BRAND + "You are now " + Colours.BRAND_ACCENT_SECONDARY + "vanished" + Colours.BRAND + "."
                    : Colours.BRAND + "You are now " + Colours.BRAND_ACCENT_SECONDARY + "visible" + Colours.BRAND + ".");
            return true;
        }

        if (!sender.hasPermission("verite.vanish.others")) {
            msg(sender, Colours.WARNING + "You may not vanish other players.");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            msg(sender, Colours.BRAND_ACCENT_SECONDARY + args[0] + " " + Colours.WARNING + "is not online.");
            return true;
        }
        boolean nowVanished = vanish.toggle(target);
        msg(sender, Colours.BRAND_ACCENT_SECONDARY + target.getName() + " " + Colours.BRAND + "is now " + Colours.BRAND_ACCENT_SECONDARY
                + (nowVanished ? "vanished" : "visible") + Colours.BRAND + ".");
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
