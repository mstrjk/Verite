package teacommontea.veritevoiler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.netty.channel.Channel;
import teacommontea.util.NmsFields;

final class VanishPacketAccess {

    static final class Unsupported extends Exception {
        private static final long serialVersionUID = 1L;
        Unsupported(String m) { super(m); }
    }

    private final Method getHandle;
    private final Field connectionField;
    private final Field rawConnectionField;
    private final Field channelField;

    private final Positioned sound;
    private final Positioned particle;

    private VanishPacketAccess(Method getHandle, Field connectionField, Field rawConnectionField,
                               Field channelField, Positioned sound, Positioned particle) {
        this.getHandle = getHandle;
        this.connectionField = connectionField;
        this.rawConnectionField = rawConnectionField;
        this.channelField = channelField;
        this.sound = sound;
        this.particle = particle;
    }

    private static final class Positioned {
        final Class<?> type;
        final Field x;
        final Field y;
        final Field z;
        final boolean fixedPoint;
        Positioned(Class<?> type, Field x, Field y, Field z, boolean fixedPoint) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.fixedPoint = fixedPoint;
        }
        double[] read(Object packet) throws IllegalAccessException {
            if (fixedPoint) {
                return new double[] { x.getInt(packet) / 8.0D, y.getInt(packet) / 8.0D, z.getInt(packet) / 8.0D };
            }
            return new double[] { x.getDouble(packet), y.getDouble(packet), z.getDouble(packet) };
        }
    }

    Channel channelOf(Player p) {
        try {
            Object handle = getHandle.invoke(p);
            Object listener = connectionField.get(handle);
            Object connection = rawConnectionField.get(listener);
            return (Channel) channelField.get(connection);
        } catch (Throwable t) {
            return null;
        }
    }

    boolean isSound(Object packet) {
        return packet != null && sound.type.isInstance(packet);
    }

    boolean isParticle(Object packet) {
        return packet != null && particle.type.isInstance(packet);
    }

    double[] positionOf(Object packet) {
        try {
            if (sound.type.isInstance(packet)) return sound.read(packet);
            if (particle.type.isInstance(packet)) return particle.read(packet);
        } catch (Throwable ignored) {
        }
        return null;
    }

    static VanishPacketAccess resolve() throws Unsupported {
        try {
            Class<?> craftPlayer = Class.forName(
                    Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer");
            Method getHandle = craftPlayer.getMethod("getHandle");
            Class<?> serverPlayer = getHandle.getReturnType();

            Field connField = NmsFields.firstFieldOfAnyType(serverPlayer,
                    "net.minecraft.server.network.ServerGamePacketListenerImpl",
                    "net.minecraft.server.network.PlayerConnection",
                    "net.minecraft.server.network.ServerCommonPacketListenerImpl");
            if (connField == null) throw new Unsupported("no packet-listener field on " + serverPlayer.getName());
            connField.setAccessible(true);

            Field rawConn = NmsFields.firstFieldOfAnyType(connField.getType(),
                    "net.minecraft.network.Connection",
                    "net.minecraft.network.NetworkManager");
            if (rawConn == null) throw new Unsupported("no Connection field on " + connField.getType().getName());
            rawConn.setAccessible(true);

            Field chan = NmsFields.firstFieldAssignableTo(rawConn.getType(), Channel.class);
            if (chan == null) throw new Unsupported("no netty Channel field on " + rawConn.getType().getName());
            chan.setAccessible(true);

            Positioned sound = firstPositioned(int.class, true,
                    "net.minecraft.network.protocol.game.ClientboundSoundPacket",
                    "net.minecraft.network.protocol.game.PacketPlayOutNamedSoundEffect");
            if (sound == null) throw new Unsupported("no positioned sound packet class on this server");

            Positioned particle = firstPositioned(double.class, false,
                    "net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket",
                    "net.minecraft.network.protocol.game.PacketPlayOutWorldParticles");
            if (particle == null) throw new Unsupported("no positioned particle packet class on this server");

            return new VanishPacketAccess(getHandle, connField, rawConn, chan,
                    sound, particle);
        } catch (Unsupported u) {
            throw u;
        } catch (Throwable t) {
            throw new Unsupported("packet-access resolution failed: " + t);
        }
    }

    private static Positioned firstPositioned(Class<?> primitive, boolean fixedPoint, String... classNames) {
        for (String n : classNames) {
            Class<?> c = classOrNull(n);
            if (c == null) continue;
            Field[] xyz = firstTriple(c, primitive);
            if (xyz != null) return new Positioned(c, xyz[0], xyz[1], xyz[2], fixedPoint);
        }
        return null;
    }

    private static Field[] firstTriple(Class<?> c, Class<?> primitive) {
        Field[] run = new Field[3];
        int n = 0;
        for (Field f : c.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                n = 0;
                continue;
            }
            if (f.getType() == primitive) {
                run[n++] = f;
                if (n == 3) {
                    for (Field g : run) g.setAccessible(true);
                    return run;
                }
            } else {
                n = 0;
            }
        }
        return null;
    }

    private static Class<?> classOrNull(String name) {
        try {
            return Class.forName(name, false, VanishPacketAccess.class.getClassLoader());
        } catch (Throwable t) {
            return null;
        }
    }
}
