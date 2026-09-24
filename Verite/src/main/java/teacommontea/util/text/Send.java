package teacommontea.util.text;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public final class Send {

    private Send() {}

    private static volatile boolean resolved;
    private static volatile Method getHandle;
    private static volatile Field connectionField;
    private static volatile Method sendPacket;
    private static volatile Method fromJson;
    private static volatile Constructor<?> packetCtor;
    private static volatile Object packetFlag;
    private static volatile Constructor<?> actionBarCtor;

    public static void to(CommandSender target, List<Span> spans) {
        if (target instanceof Player player && deliver(player, Json.of(spans))) {
            return;
        }
        target.sendMessage(Legacy.of(spans));
    }

    public static boolean actionBar(Player player, List<Span> spans) {
        if (!resolved) resolve(player);
        Constructor<?> ctor = actionBarCtor;
        Method decode = fromJson;
        Method handle = getHandle;
        Field conn = connectionField;
        Method send = sendPacket;
        if (ctor == null || decode == null || handle == null || conn == null || send == null) {
            return false;
        }
        try {
            Object component = decode.invoke(null, Json.of(spans));
            if (component == null) return false;
            send.invoke(conn.get(handle.invoke(player)), ctor.newInstance(component));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void broadcast(List<Span> spans) {
        String json = Json.of(spans);
        String legacy = Legacy.of(spans);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!deliver(p, json)) {
                p.sendMessage(legacy);
            }
        }
        Bukkit.getConsoleSender().sendMessage(legacy);
    }

    private static boolean deliver(Player player, String json) {
        if (!resolved) resolve(player);
        Method handle = getHandle;
        Field conn = connectionField;
        Method send = sendPacket;
        Method decode = fromJson;
        Constructor<?> ctor = packetCtor;
        if (handle == null || conn == null || send == null || decode == null || ctor == null) {
            return false;
        }
        try {
            Object component = decode.invoke(null, json);
            if (component == null) return false;
            Object packet = ctor.newInstance(component, packetFlag);
            send.invoke(conn.get(handle.invoke(player)), packet);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static synchronized void resolve(Player player) {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> component = componentClass();
            if (component == null) return;

            fromJson = craftChatMessageFromJson(component);
            if (fromJson == null) return;

            packetCtor = systemChatCtor(component);
            if (packetCtor == null) return;
            Class<?> flagType = packetCtor.getParameterTypes()[1];
            packetFlag = flagType == boolean.class ? Boolean.FALSE : Integer.valueOf(1);

            Method handle = player.getClass().getMethod("getHandle");
            handle.setAccessible(true);
            getHandle = handle;

            Field conn = teacommontea.util.NmsFields.firstFieldOfAnyType(handle.getReturnType(),
                    "net.minecraft.server.network.ServerGamePacketListenerImpl",
                    "net.minecraft.server.network.PlayerConnection",
                    "net.minecraft.server.network.ServerCommonPacketListenerImpl");
            if (conn == null) return;
            conn.setAccessible(true);
            connectionField = conn;

            sendPacket = packetSender(conn.getType());
            actionBarCtor = singleComponentCtor(
                    "net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket", component);
        } catch (Throwable ignored) {
            getHandle = null;
        }
    }

    private static Constructor<?> singleComponentCtor(String packetName, Class<?> component) {
        try {
            Class<?> packet = Class.forName(packetName);
            for (Constructor<?> c : packet.getConstructors()) {
                if (c.getParameterCount() != 1) continue;
                if (c.getParameterTypes()[0] != component) continue;
                c.setAccessible(true);
                return c;
            }
        } catch (Throwable ignored) {
            return null;
        }
        return null;
    }

    private static Method packetSender(Class<?> listener) {
        Class<?> packet;
        try {
            packet = Class.forName("net.minecraft.network.protocol.Packet");
        } catch (Throwable t) {
            return null;
        }
        Method named = null;
        Method fallback = null;
        for (Class<?> c = listener; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 1) continue;
                if (m.getParameterTypes()[0] != packet) continue;
                if (m.getReturnType() != void.class) continue;
                if (m.getName().equals("sendPacket")) {
                    named = m;
                } else if (fallback == null) {
                    fallback = m;
                }
            }
            if (named != null) break;
        }
        Method chosen = named != null ? named : fallback;
        if (chosen != null) chosen.setAccessible(true);
        return chosen;
    }

    private static Constructor<?> systemChatCtor(Class<?> component) {
        Class<?> packet;
        try {
            packet = Class.forName("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
        } catch (Throwable t) {
            return null;
        }
        for (Constructor<?> c : packet.getConstructors()) {
            if (c.getParameterCount() != 2) continue;
            if (c.getParameterTypes()[0] != component) continue;
            Class<?> second = c.getParameterTypes()[1];
            if (second != boolean.class && second != int.class) continue;
            c.setAccessible(true);
            return c;
        }
        return null;
    }

    private static Method craftChatMessageFromJson(Class<?> component) {
        Class<?> ccm = craftClass("util.CraftChatMessage");
        if (ccm == null) return null;
        for (Method m : ccm.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) continue;
            if (!m.getName().equals("fromJSON")) continue;
            if (m.getParameterCount() != 1) continue;
            if (m.getParameterTypes()[0] != String.class) continue;
            if (!component.isAssignableFrom(m.getReturnType())) continue;
            m.setAccessible(true);
            return m;
        }
        return null;
    }

    private static Class<?> componentClass() {
        String[] names = {
                "net.minecraft.network.chat.IChatBaseComponent",
                "net.minecraft.network.chat.Component"
        };
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (Throwable ignored) {
                continue;
            }
        }
        return null;
    }

    private static Class<?> craftClass(String suffix) {
        String base = Bukkit.getServer().getClass().getPackage().getName();
        try {
            return Class.forName(base + "." + suffix);
        } catch (Throwable ignored) {
            try {
                return Class.forName("org.bukkit.craftbukkit." + suffix);
            } catch (Throwable ignored2) {
                return null;
            }
        }
    }
}
