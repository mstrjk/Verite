package teacommontea.veritesauver.client;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import io.netty.channel.Channel;
import teacommontea.util.NmsFields;

final class ProbeNms {

    static final class Unsupported extends Exception {
        private static final long serialVersionUID = 1L;
        Unsupported(String m) { super(m); }
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final MethodHandle getHandle;
    private final Field connectionField;
    private final Field rawConnectionField;
    private final Field channelField;
    private final MethodHandle sendPacket;

    private final Class<?> signUpdateClass;
    private final Field signLinesField;
    private final Field signPosField;

    private final Constructor<?> blockPosCtor;
    private final Constructor<?> openSignCtor;
    private final Constructor<?> signEntityCtor;
    private final Constructor<?> signTextCtor;
    private final Class<?> componentClass;
    private final MethodHandle fromJson;
    private final MethodHandle setText;
    private final MethodHandle updatePacket;
    private final MethodHandle setLevel;
    private final MethodHandle worldHandle;
    private final Object blackDye;
    private final Object signBlockState;
    private final Class<?> blockChangeClass;
    private final Constructor<?> blockChangeCtor;
    private final Constructor<?> closeContainerCtor;

    private ProbeNms(Builder b) {
        this.getHandle = b.getHandle;
        this.connectionField = b.connectionField;
        this.rawConnectionField = b.rawConnectionField;
        this.channelField = b.channelField;
        this.sendPacket = b.sendPacket;
        this.signUpdateClass = b.signUpdateClass;
        this.signLinesField = b.signLinesField;
        this.signPosField = b.signPosField;
        this.blockPosCtor = b.blockPosCtor;
        this.openSignCtor = b.openSignCtor;
        this.signEntityCtor = b.signEntityCtor;
        this.signTextCtor = b.signTextCtor;
        this.componentClass = b.componentClass;
        this.fromJson = b.fromJson;
        this.setText = b.setText;
        this.updatePacket = b.updatePacket;
        this.setLevel = b.setLevel;
        this.worldHandle = b.worldHandle;
        this.blackDye = b.blackDye;
        this.signBlockState = b.signBlockState;
        this.blockChangeClass = b.blockChangeClass;
        this.blockChangeCtor = b.blockChangeCtor;
        this.closeContainerCtor = b.closeContainerCtor;
    }

    private static final class Builder {
        MethodHandle getHandle;
        Field connectionField;
        Field rawConnectionField;
        Field channelField;
        MethodHandle sendPacket;
        Class<?> signUpdateClass;
        Field signLinesField;
        Field signPosField;
        Constructor<?> blockPosCtor;
        Constructor<?> openSignCtor;
        Constructor<?> signEntityCtor;
        Constructor<?> signTextCtor;
        Class<?> componentClass;
        MethodHandle fromJson;
        MethodHandle setText;
        MethodHandle updatePacket;
        MethodHandle setLevel;
        MethodHandle worldHandle;
        Object blackDye;
        Object signBlockState;
        Class<?> blockChangeClass;
        Constructor<?> blockChangeCtor;
        Constructor<?> closeContainerCtor;
    }

    static ProbeNms resolve() throws Unsupported {
        try {
            Builder b = new Builder();
            Class<?> craftPlayer = obc("entity.CraftPlayer");
            Method gh = craftPlayer.getMethod("getHandle");
            b.getHandle = LOOKUP.unreflect(gh);
            Class<?> serverPlayer = gh.getReturnType();

            b.connectionField = require(NmsFields.firstFieldOfAnyType(serverPlayer,
                    "net.minecraft.server.network.ServerGamePacketListenerImpl",
                    "net.minecraft.server.network.PlayerConnection",
                    "net.minecraft.server.network.ServerCommonPacketListenerImpl"),
                    "packet-listener field on " + serverPlayer.getName());
            b.rawConnectionField = require(NmsFields.firstFieldOfAnyType(b.connectionField.getType(),
                    "net.minecraft.network.Connection",
                    "net.minecraft.network.NetworkManager"),
                    "Connection field on " + b.connectionField.getType().getName());
            b.channelField = require(NmsFields.firstFieldAssignableTo(b.rawConnectionField.getType(), Channel.class),
                    "netty Channel field");

            Class<?> packet = requireClass("net.minecraft.network.protocol.Packet");
            b.sendPacket = LOOKUP.unreflect(packetSender(b.connectionField.getType(), packet));

            Class<?> blockPos = requireClass("net.minecraft.core.BlockPos", "net.minecraft.core.BlockPosition");
            b.blockPosCtor = blockPos.getConstructor(int.class, int.class, int.class);

            b.signUpdateClass = requireClass(
                    "net.minecraft.network.protocol.game.ServerboundSignUpdatePacket",
                    "net.minecraft.network.protocol.game.PacketPlayInUpdateSign");
            b.signLinesField = require(NmsFields.firstFieldOfAnyType(b.signUpdateClass, "[Ljava.lang.String;"),
                    "String[] field on " + b.signUpdateClass.getName());
            b.signPosField = require(NmsFields.firstFieldAssignableTo(b.signUpdateClass, blockPos),
                    "BlockPos field on " + b.signUpdateClass.getName());

            Class<?> openSign = requireClass(
                    "net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket",
                    "net.minecraft.network.protocol.game.PacketPlayOutOpenSignEditor");
            b.openSignCtor = openSign.getConstructor(blockPos, boolean.class);

            b.componentClass = requireClass("net.minecraft.network.chat.Component",
                    "net.minecraft.network.chat.IChatBaseComponent");
            Class<?> craftChat = obc("util.CraftChatMessage");
            b.fromJson = LOOKUP.unreflect(craftChat.getMethod("fromJSON", String.class));

            Class<?> signEntity = requireClass("net.minecraft.world.level.block.entity.SignBlockEntity",
                    "net.minecraft.world.level.block.entity.TileEntitySign");
            Class<?> blockState = requireClass("net.minecraft.world.level.block.state.BlockState",
                    "net.minecraft.world.level.block.state.IBlockData");
            b.signEntityCtor = signEntity.getConstructor(blockPos, blockState);

            Class<?> signText = requireClass("net.minecraft.world.level.block.entity.SignText");
            Class<?> dyeColour = requireClass("net.minecraft.world.item.DyeColor",
                    "net.minecraft.world.item.EnumColor");
            Class<?> components = Array.newInstance(b.componentClass, 0).getClass();
            b.signTextCtor = signText.getConstructor(components, components, dyeColour, boolean.class);
            b.blackDye = firstEnum(dyeColour);
            b.setText = LOOKUP.unreflect(methodTaking(signEntity, boolean.class, signText, boolean.class));
            b.updatePacket = LOOKUP.unreflect(zeroArgReturning(signEntity,
                    requireClass("net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket",
                            "net.minecraft.network.protocol.game.PacketPlayOutTileEntityData")));
            Class<?> levelClass = requireClass("net.minecraft.world.level.Level",
                    "net.minecraft.world.level.World");
            b.setLevel = LOOKUP.unreflect(methodTaking(signEntity, void.class, levelClass));
            b.worldHandle = LOOKUP.unreflect(obc("CraftWorld").getMethod("getHandle"));

            b.signBlockState = blockStateOf(requireClass(
                    "net.minecraft.world.level.block.StandingSignBlock",
                    "net.minecraft.world.level.block.BlockFloorSign"));

            b.blockChangeClass = requireClass("net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket",
                    "net.minecraft.network.protocol.game.PacketPlayOutBlockChange");
            b.blockChangeCtor = b.blockChangeClass.getConstructor(blockPos, blockState);

            b.closeContainerCtor = requireClass(
                    "net.minecraft.network.protocol.game.ClientboundContainerClosePacket",
                    "net.minecraft.network.protocol.game.PacketPlayOutCloseWindow")
                    .getConstructor(int.class);

            return new ProbeNms(b);
        } catch (Unsupported u) {
            throw u;
        } catch (Throwable t) {
            throw new Unsupported("client probe resolution failed: " + t);
        }
    }

    private static Object blockStateOf(Class<?> wanted) throws Throwable {
        Class<?> blocks = requireClass("net.minecraft.world.level.block.Blocks");
        Class<?> block = requireClass("net.minecraft.world.level.block.Block");
        Class<?> blockState = requireClass("net.minecraft.world.level.block.state.BlockState",
                "net.minecraft.world.level.block.state.IBlockData");
        for (Field f : blocks.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) || !block.isAssignableFrom(f.getType())) continue;
            f.setAccessible(true);
            Object candidate = f.get(null);
            if (candidate == null || !wanted.isInstance(candidate)) continue;
            return zeroArgReturning(block, blockState).invoke(candidate);
        }
        throw new Unsupported("no block of type " + wanted.getName() + " in the block registry");
    }

    private static Object firstEnum(Class<?> type) throws Unsupported {
        Object[] constants = type.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new Unsupported("no constants on " + type.getName());
        }
        return constants[0];
    }

    private static Method methodTaking(Class<?> owner, Class<?> ret, Class<?>... params) throws Unsupported {
        for (Method m : owner.getMethods()) {
            if (m.getReturnType() != ret) continue;
            if (!java.util.Arrays.equals(m.getParameterTypes(), params)) continue;
            m.setAccessible(true);
            return m;
        }
        throw new Unsupported("no method on " + owner.getName() + " taking " + java.util.Arrays.toString(params));
    }

    private static Method zeroArgReturning(Class<?> owner, Class<?> ret) throws Unsupported {
        for (Method m : owner.getMethods()) {
            if (m.getParameterCount() != 0 || Modifier.isStatic(m.getModifiers())) continue;
            if (!ret.isAssignableFrom(m.getReturnType())) continue;
            m.setAccessible(true);
            return m;
        }
        throw new Unsupported("no zero-arg method on " + owner.getName() + " returning " + ret.getName());
    }

    private static Method packetSender(Class<?> listener, Class<?> packet) throws Unsupported {
        Method named = null;
        Method fallback = null;
        for (Class<?> c = listener; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getParameterCount() != 1 || m.getReturnType() != void.class) continue;
                if (m.getParameterTypes()[0] != packet) continue;
                if (m.getName().equals("send") || m.getName().equals("sendPacket")) {
                    named = m;
                } else if (fallback == null) {
                    fallback = m;
                }
            }
            if (named != null) break;
        }
        Method chosen = named != null ? named : fallback;
        if (chosen == null) {
            throw new Unsupported("no packet send method on " + listener.getName());
        }
        chosen.setAccessible(true);
        return chosen;
    }

    private static Field require(Field f, String what) throws Unsupported {
        if (f == null) {
            throw new Unsupported("no " + what);
        }
        f.setAccessible(true);
        return f;
    }

    private static Class<?> requireClass(String... names) throws Unsupported {
        for (String n : names) {
            try {
                return Class.forName(n);
            } catch (Throwable ignored) {
            }
        }
        throw new Unsupported("no class among " + java.util.Arrays.toString(names));
    }

    Channel channelOf(Player p) {
        try {
            Object listener = listenerOf(p);
            Object connection = rawConnectionField.get(listener);
            return (Channel) channelField.get(connection);
        } catch (Throwable t) {
            return null;
        }
    }

    private Object listenerOf(Player p) throws Throwable {
        return connectionField.get(getHandle.invoke(p));
    }

    void showProbeSign(Player p, int x, int y, int z, List<String> lines) throws Throwable {
        Object listener = listenerOf(p);
        Object pos = blockPosCtor.newInstance(x, y, z);

        sendPacket.invoke(listener, blockChangeCtor.newInstance(pos, signBlockState));

        Object messages = Array.newInstance(componentClass, 4);
        for (int i = 0; i < 4; i++) {
            String json = i < lines.size() ? lines.get(i) : "{\"text\":\"\"}";
            Array.set(messages, i, fromJson.invoke(json));
        }
        Object text = signTextCtor.newInstance(messages, messages, blackDye, false);
        Object entity = signEntityCtor.newInstance(pos, signBlockState);
        setLevel.invoke(entity, worldHandle.invoke(p.getWorld()));
        setText.invoke(entity, text, true);
        sendPacket.invoke(listener, updatePacket.invoke(entity));
        sendPacket.invoke(listener, openSignCtor.newInstance(pos, true));
        sendPacket.invoke(listener, closeContainerCtor.newInstance(0));
    }

    void clearProbeSign(Player p, int x, int y, int z) {
        org.bukkit.Location at = new org.bukkit.Location(p.getWorld(), x, y, z);
        p.sendBlockChange(at, at.getBlock().getBlockData());
    }

    boolean isSignUpdate(Object packet) {
        return packet != null && signUpdateClass.isInstance(packet);
    }

    String[] linesOf(Object packet) {
        try {
            Object v = signLinesField.get(packet);
            return v instanceof String[] arr ? arr : null;
        } catch (Throwable t) {
            return null;
        }
    }

    int[] positionOf(Object packet) {
        try {
            Object pos = signPosField.get(packet);
            return pos == null ? null : coordsOf(pos);
        } catch (Throwable t) {
            return null;
        }
    }

    private static int[] coordsOf(Object blockPos) throws Throwable {
        int[] xyz = new int[3];
        int found = 0;
        for (Class<?> c = blockPos.getClass(); c != null && c != Object.class && found < 3; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (found == 3) break;
                if (Modifier.isStatic(f.getModifiers()) || f.getType() != int.class) continue;
                f.setAccessible(true);
                xyz[found++] = f.getInt(blockPos);
            }
        }
        return found == 3 ? xyz : null;
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
