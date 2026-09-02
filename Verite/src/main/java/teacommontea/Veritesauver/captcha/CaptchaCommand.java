package teacommontea.veritesauver.captcha;

import teacommontea.util.Colours;
import teacommontea.util.Complete;
import teacommontea.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class CaptchaCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "veritesauver.captcha";

    private final CaptchaManager captcha;
    private final Messages messages;

    public CaptchaCommand(CaptchaManager captcha, Messages messages) {
        this.captcha = captcha;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages m = messages;
        if (!sender.hasPermission(PERMISSION)) {
            sender.spigot().sendMessage(m.prefixed(Messages.DENY_PERMISSION));
            return true;
        }
        if (args.length < 2) {
            sender.spigot().sendMessage(m.prefixed(Colours.BRAND_ACCENT_SECONDARY + "Usage: " + Colours.BRAND_ACCENT_SECONDARY + "/captcha <standard|detailed> <player|*>"));
            return true;
        }
        String mode = args[0].toLowerCase();
        if (!mode.equals("standard") && !mode.equals("detailed")) {
            sender.spigot().sendMessage(m.prefixed(Colours.WARNING + "Invalid mode. Use " + Colours.BRAND_ACCENT_SECONDARY + "standard " + Colours.WARNING + "or " + Colours.BRAND_ACCENT_SECONDARY + "detailed" + Colours.WARNING + "."));
            return true;
        }
        CaptchaKind kind = mode.equals("detailed") ? CaptchaKind.DETAILED : CaptchaKind.STANDARD;
        String targetArg = args[1];
        String source = "staff: " + sender.getName();

        if (targetArg.equals("*")) {
            int sent = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (captcha.challenge(p, kind, source)) sent++;
            }
            sender.spigot().sendMessage(m.prefixed(Colours.BRAND + "Sent a " + mode + " captcha to " + Colours.BRAND_ACCENT_SECONDARY + sent + " " + Colours.BRAND + "eligible player(s)."));
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetArg);
        if (target == null || !target.isOnline()) {
            sender.spigot().sendMessage(m.prefixed(Colours.WARNING + "That player is not online."));
            return true;
        }
        if (!captcha.challenge(target, kind, source)) {
            sender.spigot().sendMessage(m.prefixed(Colours.BRAND_ACCENT_SECONDARY + target.getName()
                    + " " + Colours.WARNING + "could not be challenged (exempt or already has one open)."));
            return true;
        }
        sender.spigot().sendMessage(m.prefixed(Colours.BRAND + "Sent a " + mode + " captcha to " + Colours.BRAND_ACCENT_SECONDARY + target.getName() + Colours.BRAND + "."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Complete.prefix(List.of("standard", "detailed"), args[0]);
        }
        if (args.length == 2) {
            List<String> out = new ArrayList<>();
            out.add("*");
            out.addAll(Complete.onlineNames(args[1]));
            return out;
        }
        return List.of();
    }
}
