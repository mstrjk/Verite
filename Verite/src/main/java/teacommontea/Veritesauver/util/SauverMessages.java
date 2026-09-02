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

    public void send(CommandSender to, String miniMessage) {
        to.spigot().sendMessage(messages.prefixed(miniMessage));
    }

    public void info(CommandSender to, String miniMessage) {
        send(to, miniMessage);
    }

    public void err(CommandSender to, String miniMessage) {
        send(to, miniMessage);
    }

    public void raw(CommandSender to, String miniMessage) {
        Text.sendRaw(to, miniMessage);
    }

    public void notify(String permission, String miniMessage) {
        notify(permission, null, miniMessage);
    }

    public void notify(String permission, Predicate<Player> skip, String miniMessage) {
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (skip != null && skip.test(staff)) {
                continue;
            }
            if (staff.hasPermission(permission)) {
                send(staff, miniMessage);
            }
        }
    }

    public static BaseComponent[] screen(String miniMessage) {
        return Text.screen(miniMessage);
    }

    public Messages messages() {
        return messages;
    }
}
