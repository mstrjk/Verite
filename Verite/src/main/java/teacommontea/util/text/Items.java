package teacommontea.util.text;

import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.List;

public final class Items {

    private Items() {}

    private static volatile boolean resolved;
    private static volatile Method nameSetter;
    private static volatile Method loreSetter;

    public static void name(ItemMeta meta, String legacy) {
        if (!resolved) resolve(meta);
        Method m = nameSetter;
        if (m == null) return;
        try {
            m.invoke(meta, legacy);
        } catch (Throwable ignored) {
            nameSetter = null;
        }
    }

    public static void lore(ItemMeta meta, List<String> legacyLines) {
        if (!resolved) resolve(meta);
        Method m = loreSetter;
        if (m == null) return;
        try {
            m.invoke(meta, legacyLines);
        } catch (Throwable ignored) {
            loreSetter = null;
        }
    }

    private static synchronized void resolve(ItemMeta meta) {
        if (resolved) return;
        resolved = true;
        nameSetter = stringMethod(meta.getClass(), "setDisplayName", String.class);
        loreSetter = stringMethod(meta.getClass(), "setLore", List.class);
    }

    private static Method stringMethod(Class<?> owner, String name, Class<?> param) {
        try {
            Method m = owner.getMethod(name, param);
            m.setAccessible(true);
            return m;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
