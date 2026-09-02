package teacommontea.util.chat;

import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import java.lang.reflect.Method;


public final class AdvancementMessage {

    private AdvancementMessage() {}

    private static volatile Method setter = null;
    private static volatile boolean resolved = false;

    public static void suppress(PlayerAdvancementDoneEvent event) {
        Method m = setter;
        if (!resolved) {
            m = resolve(event.getClass());
            setter = m;
            resolved = true;
        }
        if (m == null) return;
        try {
            m.invoke(event, new Object[] { null });
        } catch (Throwable ignored) {
            // a server that rejects a null message here keeps its default broadcast
        }
    }

    private static Method resolve(Class<?> eventClass) {
        Method stringSetter = null;
        Method messageSetter = null;
        for (Method m : eventClass.getMethods()) {
            if (m.getParameterCount() != 1) continue;
            String name = m.getName();
            Class<?> p = m.getParameterTypes()[0];
            if (name.equals("setMessage") && p == String.class) stringSetter = m;
            else if (name.equals("message")) messageSetter = m;
        }
        return stringSetter != null ? stringSetter : messageSetter;
    }
}
