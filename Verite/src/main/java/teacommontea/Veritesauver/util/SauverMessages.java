package teacommontea.veritesauver.util;

import java.util.function.Predicate;

import net.md_5.bungee.api.chat.BaseComponent;
import teacommontea.util.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import teacommontea.util.Messages;

public final class SauverMessages {

    private final Messages messages = new Messages();

    public SauverMessages() {}

    public void send(CommandSender to, String tagged) {
        to.spigot().sendMessage(messages.prefixed(tagged));
    }

    public void info(CommandSender to, String tagged) {
        send(to, tagged);
    }

    public void err(CommandSender to, String tagged) {
        send(to, tagged);
    }

    public void raw(CommandSender to, String tagged) {
        Text.sendRaw(to, tagged);
    }

    public void notify(String permission, String tagged) {
        notify(permission, null, tagged);
    }

    public void notify(String permission, Predicate<Player> skip, String tagged) {
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (skip != null && skip.test(staff)) {
                continue;
            }
            if (staff.hasPermission(permission)) {
                send(staff, tagged);
            }
        }
    }

    public static BaseComponent[] screen(String tagged) {
        return Text.screen(tagged);
    }

    public Messages messages() {
        return messages;
    }
}
