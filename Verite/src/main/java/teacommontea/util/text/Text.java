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

    public static String capitalise(String s) {
        if (s == null || s.isEmpty()) {
            return s == null ? "" : s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static BaseComponent[] parse(String tagged) {
        return TagParser.parse(tagged);
    }

    public static void send(CommandSender to, String tagged) {
        to.spigot().sendMessage(parse(tagged));
    }

    public static void sendRaw(CommandSender to, String tagged) {
        to.spigot().sendMessage(parse(tagged));
    }

    public static void actionBar(Player to, String tagged) {
        to.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, parse(tagged));
    }

    @SuppressWarnings("deprecation")
    public static void itemName(ItemMeta meta, String tagged) {
        meta.setDisplayName(toLegacy(tagged));
    }

    @SuppressWarnings("deprecation")
    public static void itemLore(ItemMeta meta, List<String> taggedLines) {
        List<String> out = new ArrayList<>(taggedLines.size());
        for (String line : taggedLines) out.add(toLegacy(line));
        meta.setLore(out);
    }

    @SuppressWarnings("deprecation")
    public static void kick(Player player, String tagged) {
        player.kickPlayer(toLegacy(tagged));
    }

    public static BaseComponent[] screen(String tagged) {
        return parse(tagged);
    }

    public static void broadcast(BaseComponent[] components) {
        for (Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(components);
        }
        org.bukkit.Bukkit.getConsoleSender().sendMessage(TextComponent.toLegacyText(components));
    }

    public static String toLegacy(String tagged) {
        return TextComponent.toLegacyText(parse(tagged));
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
