package teacommontea.veritesauver.client;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class ClientInfoProbe {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final MethodHandle getHandle;
    private final MethodHandle clientInformation;
    private final List<Method> components;

    private ClientInfoProbe(MethodHandle getHandle, MethodHandle clientInformation, List<Method> components) {
        this.getHandle = getHandle;
        this.clientInformation = clientInformation;
        this.components = components;
    }

    static ClientInfoProbe resolve() {
        try {
            Class<?> craftPlayer = obc("entity.CraftPlayer");
            Method handle = craftPlayer.getMethod("getHandle");
            Class<?> serverPlayer = handle.getReturnType();
            Class<?> infoClass = Class.forName("net.minecraft.server.level.ClientInformation");

            Method accessor = null;
            for (Method m : serverPlayer.getMethods()) {
                if (m.getParameterCount() != 0 || Modifier.isStatic(m.getModifiers())) continue;
                if (m.getReturnType() != infoClass) continue;
                accessor = m;
                break;
            }
            if (accessor == null) {
                return null;
            }
            accessor.setAccessible(true);

            RecordComponent[] parts = infoClass.getRecordComponents();
            if (parts == null || parts.length == 0) {
                return null;
            }
            List<Method> components = new ArrayList<>(parts.length);
            for (RecordComponent rc : parts) {
                Method m = rc.getAccessor();
                m.setAccessible(true);
                components.add(m);
            }
            return new ClientInfoProbe(LOOKUP.unreflect(handle), LOOKUP.unreflect(accessor), components);
        } catch (Throwable t) {
            return null;
        }
    }

    Map<String, String> read(Player p) {
        Map<String, String> out = new LinkedHashMap<>();
        try {
            Object info = clientInformation.invoke(getHandle.invoke(p));
            if (info == null) {
                return out;
            }
            for (Method m : components) {
                Object value = m.invoke(info);
                out.put(m.getName(), String.valueOf(value));
            }
        } catch (Throwable ignored) {
        }
        return out;
    }

    static List<Signal> evaluate(Player p, Map<String, String> info) {
        if (info.isEmpty()) {
            return List.of();
        }
        List<Signal> out = new ArrayList<>();

        String language = info.get("language");
        String bukkitLocale = localeOf(p);
        if (language != null && bukkitLocale != null
                && !language.equalsIgnoreCase(bukkitLocale)
                && !bukkitLocale.equalsIgnoreCase("unknown")) {
            out.add(Signal.of(Signal.Source.CLIENT_INFO, "Locale mismatch",
                    language + " against " + bukkitLocale, 30));
        }

        String skinParts = info.get("modelCustomisation");
        if ("0".equals(skinParts)) {
            out.add(Signal.of(Signal.Source.CLIENT_INFO, "All skin layers disabled",
                    "modelCustomisation=0", 20));
        }

        if ("false".equals(info.get("allowsListing"))) {
            out.add(Signal.of(Signal.Source.CLIENT_INFO, "Player list opt-out",
                    "allowsListing=false", 15));
        }

        return out;
    }

    static String fingerprint(Map<String, String> info) {
        if (info.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : info.entrySet()) {
            if (e.getKey().equals("language")) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('/');
            }
            sb.append(e.getValue());
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    private static String localeOf(Player p) {
        try {
            Object l = Player.class.getMethod("locale").invoke(p);
            if (l != null) {
                return String.valueOf(l);
            }
        } catch (Throwable ignored) {
        }
        try {
            Object l = Player.class.getMethod("getLocale").invoke(p);
            return l == null ? null : String.valueOf(l);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Class<?> obc(String suffix) throws ClassNotFoundException {
        String base = Bukkit.getServer().getClass().getPackage().getName();
        try {
            return Class.forName(base + "." + suffix);
        } catch (ClassNotFoundException e) {
            return Class.forName("org.bukkit.craftbukkit." + suffix);
        }
    }
}
