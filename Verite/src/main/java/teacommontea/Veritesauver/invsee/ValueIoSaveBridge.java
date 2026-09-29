package teacommontea.veritesauver.invsee;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.UUID;

final class ValueIoSaveBridge implements InvSeeAccess.SaveBridge {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final InvSeeAccess access;
    private final MethodHandle storageLoad;
    private final MethodHandle storageSave;
    private final MethodHandle tagValueInputCreate;
    private final Object problemReporter;
    private final Constructor<?> nameAndIdCtor;

    ValueIoSaveBridge(InvSeeAccess access) throws Throwable {
        this.access = access;
        Class<?> storageClass = InvSeeAccess.firstExisting(
                "net.minecraft.world.level.storage.PlayerDataStorage",
                "net.minecraft.world.level.storage.WorldNBTStorage");
        Class<?> problemReporterClass = InvSeeAccess.firstExisting("net.minecraft.util.ProblemReporter");
        Class<?> valueInputClass = InvSeeAccess.firstExisting("net.minecraft.world.level.storage.ValueInput");
        Class<?> tagValueInputClass = InvSeeAccess.firstExisting("net.minecraft.world.level.storage.TagValueInput");
        Class<?> compoundTagClass = InvSeeAccess.firstExisting(
                "net.minecraft.nbt.CompoundTag",
                "net.minecraft.nbt.NBTTagCompound");
        Class<?> registryClass = InvSeeAccess.firstExisting(
                "net.minecraft.core.HolderLookup$Provider",
                "net.minecraft.core.HolderLookup$a");
        if (storageClass == null || problemReporterClass == null || valueInputClass == null
                || tagValueInputClass == null || compoundTagClass == null || registryClass == null) {
            throw new InvSeeAccess.Unsupported("value-io save shapes missing");
        }
        Class<?> nameAndIdClass = InvSeeAccess.classOrNull("net.minecraft.server.players.NameAndId");
        Class<?> playerClass = access.playerClass();

        Method load = null;
        for (Method m : storageClass.getDeclaredMethods()) {
            if (m.isSynthetic() || m.isBridge() || m.getReturnType() != Optional.class) continue;
            Class<?>[] p = m.getParameterTypes();
            if (nameAndIdClass != null) {
                if (p.length == 1 && p[0] == nameAndIdClass) {
                    load = m;
                    break;
                }
            } else if (p.length == 2 && p[0] == playerClass && p[1] == problemReporterClass) {
                load = m;
                break;
            }
        }
        if (load == null) {
            throw new InvSeeAccess.Unsupported("no PlayerDataStorage.load on this server");
        }
        load.setAccessible(true);
        this.storageLoad = LOOKUP.unreflect(load);
        this.nameAndIdCtor = nameAndIdClass == null ? null
                : nameAndIdClass.getConstructor(UUID.class, String.class);

        Method save = null;
        for (Method m : storageClass.getDeclaredMethods()) {
            if (m.isSynthetic() || m.isBridge() || m.getReturnType() != void.class) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 1 && p[0] == playerClass) {
                save = m;
                break;
            }
        }
        if (save == null) {
            throw new InvSeeAccess.Unsupported("no PlayerDataStorage.save on this server");
        }
        save.setAccessible(true);
        this.storageSave = LOOKUP.unreflect(save);

        Method create = null;
        for (Method m : tagValueInputClass.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 3 && p[0] == problemReporterClass && p[1] == registryClass
                    && p[2] == compoundTagClass
                    && valueInputClass.isAssignableFrom(m.getReturnType())) {
                create = m;
                break;
            }
        }
        if (create == null) {
            throw new InvSeeAccess.Unsupported("no TagValueInput.create(ProblemReporter, _, CompoundTag)");
        }
        create.setAccessible(true);
        this.tagValueInputCreate = LOOKUP.unreflect(create);

        Field discarding = null;
        for (Field f : problemReporterClass.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && problemReporterClass.isAssignableFrom(f.getType())) {
                discarding = f;
                break;
            }
        }
        if (discarding == null) {
            throw new InvSeeAccess.Unsupported("no discarding ProblemReporter");
        }
        discarding.setAccessible(true);
        this.problemReporter = discarding.get(null);
    }

    @Override
    public Optional<Object> read(UUID uuid, String name, Object registry, Object entity) throws Throwable {
        Object storage = access.rawPlayerStorage();
        if (nameAndIdCtor == null) {
            Optional<?> input = (Optional<?>) storageLoad.invoke(storage, entity, problemReporter);
            return input == null || input.isEmpty() ? Optional.empty() : Optional.of(input.get());
        }
        Optional<?> tag = (Optional<?>) storageLoad.invoke(storage, nameAndIdCtor.newInstance(uuid, name));
        if (tag == null || tag.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tagValueInputCreate.invoke(problemReporter, registry, tag.get()));
    }

    @Override
    public void save(Object nmsServerPlayer) throws Throwable {
        storageSave.invoke(access.rawPlayerStorage(), nmsServerPlayer);
    }
}
