package teacommontea.util.text;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public final class Kick {

    private Kick() {}

    private static volatile boolean resolved;
    private static volatile Method kicker;

    public static void disconnect(Player player, String legacy) {
        if (!resolved) resolve(player);
        Method m = kicker;
        if (m == null) return;
        try {
            m.invoke(player, legacy);
        } catch (Throwable ignored) {
            kicker = null;
        }
    }

    private static synchronized void resolve(Player player) {
        if (resolved) return;
        resolved = true;
        try {
            Method m = player.getClass().getMethod("kickPlayer", String.class);
            m.setAccessible(true);
            kicker = m;
        } catch (Throwable ignored) {
            kicker = null;
        }
    }
}
