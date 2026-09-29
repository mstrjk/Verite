package teacommontea.veritesauver.invsee;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.UUID;


final class InvSeePlayers {

    private final Constructor<?> playerCtor;
    private final Constructor<?> serverPlayerCtor;
    private final Object defaultClientInfo;

    InvSeePlayers(InvSeeAccess access) throws Throwable {
        Class<?> playerClass = access.playerClass();
        Class<?> levelClass = InvSeeAccess.firstExisting(
                "net.minecraft.world.level.Level", "net.minecraft.world.level.World");
        Class<?> profileClass = InvSeeAccess.firstExisting("com.mojang.authlib.GameProfile");
        if (profileClass == null) {
            throw new InvSeeAccess.Unsupported("no com.mojang.authlib.GameProfile on this server");
        }

        Constructor<?> pc = null;
        for (Constructor<?> c : playerClass.getDeclaredConstructors()) {
            Class<?>[] p = c.getParameterTypes();
            if (p.length == 2 && p[0] == levelClass && p[1] == profileClass) {
                pc = c;
                break;
            }
        }
        if (pc == null) {
            throw new InvSeeAccess.Unsupported("no known Player constructor on " + playerClass.getName());
        }
        pc.setAccessible(true);
        this.playerCtor = pc;

        Class<?> serverPlayerClass = access.serverPlayerClass();
        Class<?> serverClass = InvSeeAccess.firstExisting("net.minecraft.server.MinecraftServer");
        Class<?> serverLevelClass = InvSeeAccess.firstExisting(
                "net.minecraft.server.level.ServerLevel", "net.minecraft.server.level.WorldServer");
        Class<?> clientInfoClass = InvSeeAccess.firstExisting(
                "net.minecraft.server.level.ClientInformation");
        if (clientInfoClass == null) {
            throw new InvSeeAccess.Unsupported("no net.minecraft.server.level.ClientInformation on this server");
        }

        Constructor<?> sc = null;
        for (Constructor<?> c : serverPlayerClass.getDeclaredConstructors()) {
            Class<?>[] p = c.getParameterTypes();
            if (p.length == 4 && p[0] == serverClass && p[1] == serverLevelClass
                    && p[2] == profileClass && p[3] == clientInfoClass) {
                sc = c;
                break;
            }
        }
        if (sc == null) {
            throw new InvSeeAccess.Unsupported(
                    "no known ServerPlayer constructor on " + serverPlayerClass.getName());
        }
        sc.setAccessible(true);
        this.serverPlayerCtor = sc;
        this.defaultClientInfo = defaultClientInfo(clientInfoClass);
    }

    Object newHuman(Object level, UUID uuid, String name) throws Throwable {
        return playerCtor.newInstance(level, InvSeeProfiles.newProfile(uuid, name));
    }

    Object newServerPlayer(Object server, Object serverLevel, UUID uuid, String name) throws Throwable {
        return serverPlayerCtor.newInstance(server, serverLevel,
                InvSeeProfiles.newProfile(uuid, name), defaultClientInfo);
    }

    private static Object defaultClientInfo(Class<?> clientInfoClass) throws Throwable {
        for (Method m : clientInfoClass.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 0
                    && m.getReturnType() == clientInfoClass) {
                m.setAccessible(true);
                return m.invoke(null);
            }
        }
        throw new InvSeeAccess.Unsupported("no ClientInformation default factory on " + clientInfoClass.getName());
    }
}
