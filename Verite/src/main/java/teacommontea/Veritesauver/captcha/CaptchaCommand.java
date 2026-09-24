package teacommontea.veritesauver.captcha;

import teacommontea.util.Colours;
import teacommontea.util.Complete;
import teacommontea.util.Lang;
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
            teacommontea.util.text.Send.to(sender, m.prefixed(Lang.of("deny.permission")));
            return true;
        }
        if (args.length < 2) {
            teacommontea.util.text.Send.to(sender, m.prefixed(Lang.of("captcha.usage")));
            return true;
        }
        String mode = args[0].toLowerCase();
        if (!mode.equals("standard") && !mode.equals("detailed")) {
            teacommontea.util.text.Send.to(sender, m.prefixed(Lang.of("captcha.mode.invalid")));
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
            teacommontea.util.text.Send.to(sender, m.prefixed(
                    Lang.counted("captcha.sent.bulk", sent, "mode", mode, "count", sent)));
            return true;
        }

        Player target = Bukkit.getPlayerExact(targetArg);
        if (target == null || !target.isOnline()) {
            teacommontea.util.text.Send.to(sender, m.prefixed(Lang.of("player.offline")));
            return true;
        }
        if (!captcha.challenge(target, kind, source)) {
            teacommontea.util.text.Send.to(sender, m.prefixed(
                    Lang.of("captcha.challenge.refused", "name", target.getName())));
            return true;
        }
        teacommontea.util.text.Send.to(sender, m.prefixed(
                Lang.of("captcha.sent.player", "mode", mode, "name", target.getName())));
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
