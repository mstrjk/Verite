package teacommontea.util.text;

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

    public static List<Span> parse(String tagged) {
        return Tags.parse(tagged);
    }

    public static void send(CommandSender to, String tagged) {
        Send.to(to, parse(tagged));
    }

    public static void sendRaw(CommandSender to, String tagged) {
        Send.to(to, parse(tagged));
    }

    public static void actionBar(Player to, String tagged) {
        List<Span> spans = parse(tagged);
        if (!Send.actionBar(to, spans)) {
            Send.to(to, spans);
        }
    }

    public static void itemName(ItemMeta meta, String tagged) {
        Items.name(meta, toLegacy(tagged));
    }

    public static void itemLore(ItemMeta meta, List<String> taggedLines) {
        List<String> out = new ArrayList<>(taggedLines.size());
        for (String line : taggedLines) out.add(toLegacy(line));
        Items.lore(meta, out);
    }

    public static void kick(Player player, String tagged) {
        Kick.disconnect(player, toLegacy(tagged));
    }

    public static List<Span> screen(String tagged) {
        return parse(tagged);
    }

    public static void broadcast(List<Span> spans) {
        Send.broadcast(spans);
    }

    public static String toLegacy(String tagged) {
        return Legacy.of(parse(tagged));
    }

    public static String legacyFrom(Object serverComponent) {
        if (serverComponent == null) return "";
        if (serverComponent instanceof List<?> list) {
            List<Span> spans = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Span s) spans.add(s);
            }
            if (!spans.isEmpty()) return Legacy.of(spans);
        }
        return String.valueOf(serverComponent);
    }

    public static String plainFrom(Object serverComponent) {
        return Legacy.strip(legacyFrom(serverComponent));
    }
}
