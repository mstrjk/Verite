package teacommontea.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


public final class Complete {

    private Complete() {}

    public static List<String> prefix(Collection<String> options, String typed) {
        String p = typed.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o != null && o.toLowerCase(Locale.ROOT).startsWith(p)) {
                out.add(o);
            }
        }
        return out;
    }

    public static List<String> onlineNames(String typed) {
        return onlineNames(typed, null);
    }

    public static List<String> onlineNames(String typed, Predicate<Player> keep) {
        String p = typed.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (Player pl : Bukkit.getOnlinePlayers()) {
            if (keep != null && !keep.test(pl)) {
                continue;
            }
            if (pl.getName().toLowerCase(Locale.ROOT).startsWith(p)) {
                out.add(pl.getName());
            }
        }
        return out;
    }
}
