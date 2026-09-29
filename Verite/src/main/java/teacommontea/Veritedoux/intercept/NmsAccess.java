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
    private final ComponentDecoder decoder;

    interface ComponentDecoder {
        String toPlain(Object nmsComponent) throws Throwable;
    }

    private NmsAccess(MethodHandle getHandle, Field connectionField, Field rawConnectionField,
                      Field channelField, Class<?> chatPacketClass, Field packetComponentField,
                      ComponentDecoder decoder) {
        this.getHandle = getHandle;
        this.connectionField = connectionField;
        this.rawConnectionField = rawConnectionField;
        this.channelField = channelField;
        this.chatPacketClass = chatPacketClass;
        this.packetComponentField = packetComponentField;
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

            Class<?> packetClass = Class.forName(
                    "net.minecraft.network.protocol.game.ClientboundSystemChatPacket");

            Field componentField = firstComponentField(packetClass);
            if (componentField == null) {
                throw new Unsupported("no Component content field on " + packetClass.getName());
            }

            Field connField = NmsFields.firstFieldOfAnyType(serverPlayer,
                    "net.minecraft.server.network.ServerGamePacketListenerImpl",
                    "net.minecraft.server.network.PlayerConnection",
                    "net.minecraft.server.network.ServerCommonPacketListenerImpl");
            if (connField == null) {
                throw new Unsupported("no packet-listener field on " + serverPlayer.getName());
            }
            connField.setAccessible(true);
            Class<?> listenerType = connField.getType();

            Field rawConn = NmsFields.firstFieldOfAnyType(listenerType,
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
                    decoder);
        } catch (Unsupported u) {
            throw u;
        } catch (Throwable t) {
            throw new Unsupported("NMS resolution failed: " + t, t);
        }
    }

    private static Field firstComponentField(Class<?> c) {
        Class<?> comp = classOrNull("net.minecraft.network.chat.Component");
        Class<?> compSpigot = classOrNull("net.minecraft.network.chat.IChatBaseComponent");
        for (Field f : c.getDeclaredFields()) {
            Class<?> t = f.getType();
            if ((comp != null && t == comp) || (compSpigot != null && t == compSpigot)) {
                f.setAccessible(true);
                return f;
            }
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
