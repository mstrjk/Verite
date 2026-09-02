package teacommontea.veritesauver.invsee;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;


final class CompoundTagSaveBridge implements InvSeeAccess.SaveBridge {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final InvSeeAccess access;
    private final MethodHandle storageLoad;
    private final MethodHandle storageSave;
    private final int loadArgKind;

    private static final int ARG_PROFILE = 0;
    private static final int ARG_HUMAN = 1;
    private static final int ARG_STRING = 2;

    CompoundTagSaveBridge(InvSeeAccess access) throws Throwable {
        this.access = access;

        Class<?> storageClass = InvSeeAccess.firstExisting(
                "net.minecraft.world.level.storage.PlayerDataStorage",
                "net.minecraft.world.level.storage.WorldNBTStorage");
        Class<?> compoundTag = InvSeeAccess.firstExisting(
                "net.minecraft.nbt.CompoundTag",
                "net.minecraft.nbt.NBTTagCompound");
        Class<?> serverPlayer = InvSeeAccess.firstExisting(
                "net.minecraft.server.level.ServerPlayer",
                "net.minecraft.server.level.EntityPlayer");
        Class<?> profileClass = InvSeeAccess.firstExisting("com.mojang.authlib.GameProfile");
        Class<?> humanClass = access.playerClass();
        if (storageClass == null || compoundTag == null || serverPlayer == null) {
            throw new InvSeeAccess.Unsupported("legacy save shapes missing");
        }

        Method load = null;
        int kind = -1;
        for (Method m : storageClass.getDeclaredMethods()) {
            if (m.isSynthetic() || m.isBridge()) continue;
            if (m.getReturnType() != compoundTag && m.getReturnType() != Optional.class) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length != 1) continue;
            if (humanClass != null && p[0].isAssignableFrom(humanClass) && p[0] != Object.class) {
                load = m; kind = ARG_HUMAN; break;
            }
            if (p[0] == profileClass && load == null) { load = m; kind = ARG_PROFILE; }
            else if (p[0] == String.class && load == null) { load = m; kind = ARG_STRING; }
        }
        if (load == null) {
            throw new InvSeeAccess.Unsupported("no PlayerDataStorage.load on this server");
        }
        load.setAccessible(true);
        this.storageLoad = LOOKUP.unreflect(load);
        this.loadArgKind = kind;

        Method save = null;
        for (Method m : storageClass.getDeclaredMethods()) {
            if (m.isSynthetic() || m.isBridge()) continue;
            if (m.getReturnType() != void.class) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 1 && p[0].isAssignableFrom(serverPlayer) && p[0] != Object.class) { save = m; break; }
        }
        if (save == null) {
            throw new InvSeeAccess.Unsupported("no PlayerDataStorage.save on this server");
        }
        save.setAccessible(true);
        this.storageSave = LOOKUP.unreflect(save);
    }

    @Override
    public Optional<Object> read(UUID uuid, String name, Object registry, Object entity) throws Throwable {
        Object arg;
        switch (loadArgKind) {
            case ARG_HUMAN -> arg = entity;
            case ARG_STRING -> arg = name;
            default -> arg = InvSeeProfiles.newProfile(uuid, name);
        }
        Object loaded = storageLoad.invoke(access.rawPlayerStorage(), arg);
        @SuppressWarnings("unchecked")
        Optional<Object> tag = loaded instanceof Optional
                ? (Optional<Object>) loaded
                : Optional.ofNullable(loaded);
        return tag == null ? Optional.empty() : tag;
    }

    @Override
    public void save(Object nmsServerPlayer) throws Throwable {
        storageSave.invoke(access.rawPlayerStorage(), nmsServerPlayer);
    }
}
