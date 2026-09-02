package teacommontea.veritesauver.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.Sauver;

public final class SauverEvents {

    public interface Listener {
        default void entryAdded(Entry entry) {}
        default void entryRemoved(Entry entry) {}
        default void broadcastSent(String message, String target) {}
    }

    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private SauverEvents() {}

    public static void register(Listener l) {
        if (l != null) {
            LISTENERS.add(l);
        }
    }

    public static void unregister(Listener l) {
        LISTENERS.remove(l);
    }

    public static void fireAdded(Entry e) {
        for (Listener l : LISTENERS) {
            try {
                l.entryAdded(e);
            } catch (Throwable t) {
                warn("entryAdded", t);
            }
        }
        teacommontea.api.internal.ApiBridge.firePunishmentAdded(e);
    }

    public static void fireRemoved(Entry e) {
        for (Listener l : LISTENERS) {
            try {
                l.entryRemoved(e);
            } catch (Throwable t) {
                warn("entryRemoved", t);
            }
        }
        teacommontea.api.internal.ApiBridge.firePunishmentRemoved(e);
    }

    public static void fireBroadcast(String message, String target) {
        for (Listener l : LISTENERS) {
            try {
                l.broadcastSent(message, target);
            } catch (Throwable t) {
                warn("broadcastSent", t);
            }
        }
        teacommontea.api.internal.ApiBridge.fireBroadcast(message, target);
    }

    private static void warn(String which, Throwable t) {
        Sauver s = Sauver.instance();
        if (s != null && s.plugin() != null) {
            s.plugin().getLogger().warning("A punishment " + which + " listener threw: " + t);
        }
    }
}
