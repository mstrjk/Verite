package teacommontea.veritedoux.intercept;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.netty.channel.Channel;
import teacommontea.util.NmsFields;

final class NmsAccess {

    static final class Unsupported extends Exception {
        private static final long serialVersionUID = 1L;
        Unsupported(String m) { super(m); }
        Unsupported(String m, Throwable c) { super(m, c); }
    }

    private final MethodHandle getHandle;
    private final Field connectionField;
    private final Field rawConnectionField;
    private final Field channelField;
    private final Class<?> chatPacketClass;
    private final Field packetComponentField;
    private final boolean componentIsString;
    private final ComponentDecoder decoder;

    interface ComponentDecoder {
        String toPlain(Object nmsComponent) throws Throwable;
    }

    private NmsAccess(MethodHandle getHandle, Field connectionField, Field rawConnectionField,
                      Field channelField, Class<?> chatPacketClass, Field packetComponentField,
                      boolean componentIsString, ComponentDecoder decoder) {
        this.getHandle = getHandle;
        this.connectionField = connectionField;
        this.rawConnectionField = rawConnectionField;
        this.channelField = channelField;
        this.chatPacketClass = chatPacketClass;
        this.packetComponentField = packetComponentField;
        this.componentIsString = componentIsString;
        this.decoder = decoder;
    }

    Class<?> chatPacketClass() { return chatPacketClass; }

    boolean isChatPacket(Object packet) {
        return packet != null && chatPacketClass.isInstance(packet);
    }

    String plainTextOf(Object packet) {
        try {
            Object value = packetComponentField.get(packet);
            if (value == null) return null;
            if (componentIsString) return (String) value;
            return decoder.toPlain(value);
        } catch (Throwable t) {
            return null;
        }
    }

    Channel channelOf(Player p) throws Throwable {
        Object handle = getHandle.invoke(p);
        Object listener = connectionField.get(handle);
        Object connection = rawConnectionField.get(listener);
        return (Channel) channelField.get(connection);
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static NmsAccess resolve() throws Unsupported {
        try {
            Class<?> craftPlayer = obc("entity.CraftPlayer");
            Method gh = craftPlayer.getMethod("getHandle");
            MethodHandle getHandle = LOOKUP.unreflect(gh);
            Class<?> serverPlayer = gh.getReturnType();

            Class<?> packetClass = firstExisting(
                    "net.minecraft.network.protocol.game.ClientboundSystemChatPacket",
                    "net.minecraft.network.protocol.game.ClientboundChatPacket");
            if (packetClass == null) {
                throw new Unsupported("no known outbound chat packet class on this server");
            }

            boolean componentIsString = false;
            Field componentField = firstComponentField(packetClass);
            if (componentField == null) {
                componentField = firstStringField(packetClass);
                if (componentField == null) {
                    throw new Unsupported("no Component or String content field on " + packetClass.getName());
                }
                componentIsString = true;
            }
            componentField.setAccessible(true);

            Field connField = NmsFields.firstFieldOfAnyType(serverPlayer,
                    "net.minecraft.server.network.ServerGamePacketListenerImpl",
                    "net.minecraft.server.network.PlayerConnection",
                    "net.minecraft.server.network.ServerCommonPacketListenerImpl");
            if (connField == null) {
                throw new Unsupported("no packet-listener field on " + serverPlayer.getName());
            }
            connField.setAccessible(true);
            Class<?> listenerType = connField.getType();

            Field rawConn = NmsFields.firstFieldOfAnyType(deepestListener(listenerType),
                    "net.minecraft.network.Connection",
                    "net.minecraft.network.NetworkManager");
            if (rawConn == null) {
                throw new Unsupported("no Connection field on " + listenerType.getName());
            }
            rawConn.setAccessible(true);

            Field chan = NmsFields.firstFieldAssignableTo(rawConn.getType(), Channel.class);
            if (chan == null) {
                throw new Unsupported("no netty Channel field on " + rawConn.getType().getName());
            }
            chan.setAccessible(true);

            ComponentDecoder decoder = ComponentDecoders.resolve();

            return new NmsAccess(getHandle, connField, rawConn, chan, packetClass, componentField,
                    componentIsString, decoder);
        } catch (Unsupported u) {
            throw u;
        } catch (Throwable t) {
            throw new Unsupported("NMS resolution failed: " + t, t);
        }
    }

    private static Class<?> deepestListener(Class<?> listenerType) {
        return listenerType;
    }

    private static Field firstStringField(Class<?> c) {
        Field firstAny = null;
        for (Field f : c.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            if (f.getType() != String.class) continue;
            if (f.getName().equals("content")) { f.setAccessible(true); return f; }
            if (firstAny == null && !f.getName().startsWith("adventure")) firstAny = f;
        }
        if (firstAny != null) firstAny.setAccessible(true);
        return firstAny;
    }

    private static Field firstComponentField(Class<?> c) {
        Class<?> comp = classOrNull("net.minecraft.network.chat.Component");
        Class<?> compLegacy = classOrNull("net.minecraft.network.chat.IChatBaseComponent");
        for (Field f : c.getDeclaredFields()) {
            Class<?> t = f.getType();
            if ((comp != null && t == comp) || (compLegacy != null && t == compLegacy)) {
                f.setAccessible(true);
                return f;
            }
        }
        return null;
    }

    private static Class<?> firstExisting(String... names) {
        for (String n : names) {
            Class<?> c = classOrNull(n);
            if (c != null) return c;
        }
        return null;
    }

    private static Class<?> classOrNull(String n) {
        try { return Class.forName(n); } catch (Throwable t) { return null; }
    }

    private static Class<?> obc(String sub) throws ClassNotFoundException {
        String base = Bukkit.getServer().getClass().getPackage().getName();
        try {
            return Class.forName(base + "." + sub);
        } catch (ClassNotFoundException e) {
            return Class.forName("org.bukkit.craftbukkit." + sub);
        }
    }

}
