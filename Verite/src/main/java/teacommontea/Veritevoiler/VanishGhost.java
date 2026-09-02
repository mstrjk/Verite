package teacommontea.veritevoiler;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import teacommontea.util.NmsFields;

final class VanishGhost {

    private static boolean resolved;
    private static boolean available;

    private static Method craftGetHandle;
    private static Field connectionField;
    private static Field rawConnectionField;
    private static Method sendMethod;
    private static Constructor<?> infoCtor;
    private static Object updateGameModeAction;
    private static boolean varargsCtor;
    private static Field gameModeField;
    private static Field gameTypeField;
    private static Object spectatorGameType;

    private VanishGhost() {}

    static void ghostFor(Player viewer, Player vanished) {
        if (!ensureResolved()) return;
        try {
            Object serverPlayer = craftGetHandle.invoke(vanished);
            Object gameModeObj = gameModeField.get(serverPlayer);
            Object realType = gameTypeField.get(gameModeObj);
            gameTypeField.set(gameModeObj, spectatorGameType);
            Object packet;
            try {
                packet = varargsCtor
                        ? infoCtor.newInstance(updateGameModeAction, arrayOf(serverPlayer))
                        : infoCtor.newInstance(updateGameModeAction, serverPlayer);
            } finally {
                gameTypeField.set(gameModeObj, realType);
            }
            send(viewer, packet);
        } catch (Throwable ignored) {

        }
    }

    private static Object[] arrayOf(Object serverPlayer) {

        Object arr = java.lang.reflect.Array.newInstance(serverPlayer.getClass(), 1);
        java.lang.reflect.Array.set(arr, 0, serverPlayer);
        return new Object[]{arr};
    }

    private static void send(Player viewer, Object packet) throws Exception {
        Object sp = craftGetHandle.invoke(viewer);
        Object listener = connectionField.get(sp);
        Object connection = rawConnectionField.get(listener);
        sendMethod.invoke(connection, packet);
    }

    private static synchronized boolean ensureResolved() {
        if (resolved) return available;
        resolved = true;
        try {
            Class<?> craftPlayer = Class.forName(
                    Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftPlayer");
            craftGetHandle = craftPlayer.getMethod("getHandle");
            Class<?> serverPlayer = craftGetHandle.getReturnType();

            Class<?> infoClass = firstClass(
                    "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket",
                    "net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket",
                    "net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo");
            if (infoClass == null) return fail();

            Class<?> actionClass = firstClass(
                    "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action",
                    "net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket$Action",
                    "net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
            updateGameModeAction = actionClass == null ? null
                    : enumConst(actionClass, "UPDATE_GAME_MODE", "UPDATE_GAMEMODE");
            if (updateGameModeAction == null) {
                Object[] found = findActionByConstant(infoClass, "UPDATE_GAME_MODE", "UPDATE_GAMEMODE");
                if (found != null) {
                    actionClass = (Class<?>) found[0];
                    updateGameModeAction = found[1];
                }
            }
            if (actionClass == null || updateGameModeAction == null) return fail();

            try {
                infoCtor = infoClass.getConstructor(actionClass, serverPlayer);
                varargsCtor = false;
            } catch (NoSuchMethodException e) {
                Class<?> arr = java.lang.reflect.Array.newInstance(serverPlayer, 0).getClass();
                infoCtor = infoClass.getConstructor(actionClass, arr);
                varargsCtor = true;
            }

            connectionField = NmsFields.firstFieldOfAnyType(serverPlayer,
                    "net.minecraft.server.network.ServerGamePacketListenerImpl",
                    "net.minecraft.server.network.PlayerConnection",
                    "net.minecraft.server.network.ServerCommonPacketListenerImpl");
            if (connectionField == null) return fail();
            connectionField.setAccessible(true);

            rawConnectionField = NmsFields.firstFieldOfAnyType(connectionField.getType(),
                    "net.minecraft.network.Connection", "net.minecraft.network.NetworkManager");
            if (rawConnectionField == null) return fail();
            rawConnectionField.setAccessible(true);

            Class<?> packetClass = firstClass("net.minecraft.network.protocol.Packet");
            if (packetClass == null) return fail();
            Method[] connMethods = rawConnectionField.getType().getMethods();
            for (Method m : connMethods) {
                if (m.getParameterCount() == 1
                        && m.getParameterTypes()[0].isAssignableFrom(packetClass)
                        && (m.getName().equals("send") || m.getName().equals("sendPacket"))) {
                    sendMethod = m;
                    break;
                }
            }
            if (sendMethod == null) {
                for (Method m : connMethods) {
                    if (m.getParameterCount() == 1
                            && m.getParameterTypes()[0].isAssignableFrom(packetClass)
                            && m.getReturnType() == void.class) {
                        sendMethod = m;
                        break;
                    }
                }
            }
            if (sendMethod == null) return fail();

            Class<?> gameTypeClass = firstClass(
                    "net.minecraft.world.level.GameType", "net.minecraft.world.level.EnumGamemode");
            if (gameTypeClass == null) return fail();
            spectatorGameType = enumConst(gameTypeClass, "SPECTATOR");
            if (spectatorGameType == null) return fail();

            Class<?> gameModeClass = firstClass(
                    "net.minecraft.server.level.ServerPlayerGameMode",
                    "net.minecraft.server.level.PlayerInteractManager");
            if (gameModeClass == null) return fail();

            gameModeField = firstFieldOfExactType(serverPlayer, gameModeClass);
            if (gameModeField == null) return fail();
            gameModeField.setAccessible(true);

            gameTypeField = firstFieldOfExactType(gameModeClass, gameTypeClass);
            if (gameTypeField == null) return fail();
            gameTypeField.setAccessible(true);

            available = true;
            return true;
        } catch (Throwable t) {
            return fail();
        }
    }

    private static boolean fail() {
        available = false;
        return false;
    }

    private static Class<?> firstClass(String... names) {
        for (String n : names) {
            try { return Class.forName(n); } catch (Throwable ignored) {}
        }
        return null;
    }

    private static Object enumConst(Class<?> enumClass, String... names) {
        Object[] consts = enumClass.getEnumConstants();
        if (consts == null) return null;
        for (Object c : consts) {
            String nm = c instanceof Enum<?> ? ((Enum<?>) c).name() : c.toString();
            for (String n : names) {
                if (nm.equalsIgnoreCase(n)) return c;
            }
        }
        return null;
    }

    private static Object[] findActionByConstant(Class<?> packetClass, String... names) {
        for (Class<?> nested : packetClass.getDeclaredClasses()) {
            if (!nested.isEnum()) continue;
            Object c = enumConst(nested, names);
            if (c != null) return new Object[] { nested, c };
        }
        return null;
    }

    private static Field firstFieldOfExactType(Class<?> start, Class<?> type) {
        for (Class<?> c = start; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == type) return f;
            }
        }
        return null;
    }
}
