package teacommontea.veritesauver.invsee;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;


final class InvSeePlayers {

    private final InvSeeAccess access;

    private final Constructor<?> playerCtor;
    private final int playerShape;
    private final Constructor<?> serverPlayerCtor;
    private final int serverShape;

    private final Object originBlockPos;
    private final Object defaultClientInfo;
    private final Class<?> profilePublicKeyClass;

    private static final int P_TWO = 0;
    private static final int P_FOUR = 1;
    private static final int P_FIVE = 2;

    private static final int S_THREE = 0;
    private static final int S_KEY = 1;
    private static final int S_CLIENT = 2;

    InvSeePlayers(InvSeeAccess access) throws Throwable {
        this.access = access;

        Class<?> playerClass = access.playerClass();
        Class<?> levelClass = InvSeeAccess.firstExisting(
                "net.minecraft.world.level.Level", "net.minecraft.world.level.World");
        Class<?> blockPosClass = InvSeeAccess.firstExisting(
                "net.minecraft.core.BlockPos", "net.minecraft.core.BlockPosition");
        Class<?> profileClass = InvSeeAccess.firstExisting("com.mojang.authlib.GameProfile");
        if (profileClass == null) {
            throw new InvSeeAccess.Unsupported("no com.mojang.authlib.GameProfile on this server");
        }
        this.profilePublicKeyClass = InvSeeAccess.classOrNull(
                "net.minecraft.world.entity.player.ProfilePublicKey");

        this.originBlockPos = blockPosClass == null ? null : firstStaticOfType(blockPosClass, blockPosClass);

        Constructor<?> pc = null;
        int ps = -1;
        for (Constructor<?> c : playerClass.getDeclaredConstructors()) {
            Class<?>[] p = c.getParameterTypes();
            if (p.length == 2 && p[0] == levelClass && p[1] == profileClass) {
                pc = c; ps = P_TWO; break;
            }
            if (p.length == 4 && p[0] == levelClass && p[1] == blockPosClass
                    && p[2] == float.class && p[3] == profileClass) {
                pc = c; ps = P_FOUR;
            } else if (p.length == 5 && p[0] == levelClass && p[1] == blockPosClass
                    && p[2] == float.class && p[3] == profileClass
                    && p[4] == profilePublicKeyClass) {
                if (pc == null) { pc = c; ps = P_FIVE; }
            }
        }
        if (pc == null) {
            throw new InvSeeAccess.Unsupported("no known Player constructor on " + playerClass.getName());
        }
        pc.setAccessible(true);
        this.playerCtor = pc;
        this.playerShape = ps;

        Class<?> serverPlayerClass = access.serverPlayerClass();
        Class<?> serverClass = InvSeeAccess.firstExisting("net.minecraft.server.MinecraftServer");
        Class<?> serverLevelClass = InvSeeAccess.firstExisting(
                "net.minecraft.server.level.ServerLevel", "net.minecraft.server.level.WorldServer");
        Class<?> clientInfoClass = InvSeeAccess.classOrNull(
                "net.minecraft.server.level.ClientInformation");

        Constructor<?> sc = null;
        int ss = -1;
        for (Constructor<?> c : serverPlayerClass.getDeclaredConstructors()) {
            Class<?>[] p = c.getParameterTypes();
            if (p.length < 3 || p[0] != serverClass || p[1] != serverLevelClass
                    || p[2] != profileClass) {
                continue;
            }
            if (p.length == 3) { sc = c; ss = S_THREE; }
            else if (p.length == 4 && p[3] == profilePublicKeyClass) { sc = c; ss = S_KEY; }
            else if (p.length == 4 && clientInfoClass != null && p[3] == clientInfoClass) {
                sc = c; ss = S_CLIENT; break;
            }
        }
        if (sc == null) {
            throw new InvSeeAccess.Unsupported(
                    "no known ServerPlayer constructor on " + serverPlayerClass.getName());
        }
        sc.setAccessible(true);
        this.serverPlayerCtor = sc;
        this.serverShape = ss;

        this.defaultClientInfo = ss == S_CLIENT ? buildClientInfo(clientInfoClass) : null;
    }

    Object newHuman(Object level, UUID uuid, String name) throws Throwable {
        Object profile = InvSeeProfiles.newProfile(uuid, name);
        return switch (playerShape) {
            case P_TWO -> playerCtor.newInstance(level, profile);
            case P_FOUR -> playerCtor.newInstance(level, originBlockPos, 0.0F, profile);
            default -> playerCtor.newInstance(level, originBlockPos, 0.0F, profile, null);
        };
    }

    Object newServerPlayer(Object server, Object serverLevel, UUID uuid, String name) throws Throwable {
        Object profile = InvSeeProfiles.newProfile(uuid, name);
        return switch (serverShape) {
            case S_THREE -> serverPlayerCtor.newInstance(server, serverLevel, profile);
            case S_KEY -> serverPlayerCtor.newInstance(server, serverLevel, profile, (Object) null);
            default -> serverPlayerCtor.newInstance(server, serverLevel, profile, defaultClientInfo);
        };
    }

    private static Object buildClientInfo(Class<?> clientInfoClass) throws Throwable {
        for (Method m : clientInfoClass.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0
                    && m.getName().equals("createDefault")
                    && clientInfoClass.isAssignableFrom(m.getReturnType())) {
                m.setAccessible(true);
                return m.invoke(null);
            }
        }
        Constructor<?> eight = null;
        for (Constructor<?> c : clientInfoClass.getDeclaredConstructors()) {
            if (c.getParameterCount() >= 8 && c.getParameterTypes()[0] == String.class) {
                eight = c; break;
            }
        }
        if (eight == null) {
            throw new InvSeeAccess.Unsupported("no ClientInformation createDefault or ctor");
        }
        eight.setAccessible(true);
        Class<?>[] p = eight.getParameterTypes();
        Object[] args = new Object[p.length];
        for (int i = 0; i < p.length; i++) {
            args[i] = defaultFor(p[i]);
        }
        args[0] = "en_us";
        args[1] = 2;
        return eight.newInstance(args);
    }

    private static Object defaultFor(Class<?> t) {
        if (t == boolean.class) return true;
        if (t == int.class) return 0;
        if (t == String.class) return "";
        if (t.isEnum()) {
            Object[] constants = t.getEnumConstants();
            return constants != null && constants.length > 0 ? constants[constants.length - 1] : null;
        }
        return null;
    }

    private static Object firstStaticOfType(Class<?> owner, Class<?> type) throws IllegalAccessException {
        for (Field f : owner.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == type) {
                f.setAccessible(true);
                Object v = f.get(null);
                if (v != null) return v;
            }
        }
        return null;
    }
}
