package teacommontea.veritevoiler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public final class VanishEvents {

    public interface Listener {
        default void vanished(UUID player) {
            onVanishChange(player, true);
        }
        default void unvanished(UUID player) {
            onVanishChange(player, false);
        }
        default void onVanishChange(UUID player, boolean nowVanished) {}
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private VanishEvents() {}

    public static void register(Listener l) {
        if (l != null) {
            LISTENERS.add(l);
        }
    }

    public static void unregister(Listener l) {
        LISTENERS.remove(l);
    }

    static void fireVanished(UUID player) {
        for (Listener l : LISTENERS) {
            try {
                l.vanished(player);
            } catch (Throwable t) {
                warn("vanished", t);
            }
        }
        teacommontea.api.internal.ApiBridge.fireVanished(player);
    }

    static void fireUnvanished(UUID player) {
        for (Listener l : LISTENERS) {
            try {
                l.unvanished(player);
            } catch (Throwable t) {
                warn("unvanished", t);
            }
        }
        teacommontea.api.internal.ApiBridge.fireUnvanished(player);
    }

    private static void warn(String which, Throwable t) {
        Vanish v = Vanish.instance();
        if (v != null && v.plugin() != null) {
            v.plugin().getLogger().warning("A vanish " + which + " listener threw: " + t);
        }
    }
}
