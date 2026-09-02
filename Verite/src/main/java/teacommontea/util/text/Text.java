package teacommontea.util.text;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;


public final class Text {

    private Text() {}

    public static BaseComponent[] parse(String miniMessage) {
        return MiniToBungee.parse(miniMessage);
    }

    public static void send(CommandSender to, String miniMessage) {
        to.spigot().sendMessage(parse(miniMessage));
    }

    public static void sendRaw(CommandSender to, String miniMessage) {
        to.spigot().sendMessage(parse(miniMessage));
    }

    public static void actionBar(Player to, String miniMessage) {
        to.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, parse(miniMessage));
    }

    @SuppressWarnings("deprecation")
    public static void itemName(ItemMeta meta, String miniMessage) {
        meta.setDisplayName(toLegacy(miniMessage));
    }

    @SuppressWarnings("deprecation")
    public static void itemLore(ItemMeta meta, List<String> miniMessageLines) {
        List<String> out = new ArrayList<>(miniMessageLines.size());
        for (String line : miniMessageLines) out.add(toLegacy(line));
        meta.setLore(out);
    }

    @SuppressWarnings("deprecation")
    public static void kick(Player player, String miniMessage) {
        player.kickPlayer(toLegacy(miniMessage));
    }

    public static BaseComponent[] screen(String miniMessage) {
        return parse(miniMessage);
    }

    public static void broadcast(BaseComponent[] components) {
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(components);
        }
        org.bukkit.Bukkit.getConsoleSender().sendMessage(TextComponent.toLegacyText(components));
    }

    public static String toLegacy(String miniMessage) {
        return TextComponent.toLegacyText(parse(miniMessage));
    }

    public static String legacyFrom(Object serverComponent) {
        if (serverComponent == null) return "";
        if (serverComponent instanceof BaseComponent[] arr) return TextComponent.toLegacyText(arr);
        if (serverComponent instanceof BaseComponent one) return TextComponent.toLegacyText(one);
        return String.valueOf(serverComponent);
    }

    public static String plainFrom(Object serverComponent) {
        return net.md_5.bungee.api.ChatColor.stripColor(legacyFrom(serverComponent));
    }
}
