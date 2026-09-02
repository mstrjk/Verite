package teacommontea.veritesauver.invsee;

import org.bukkit.Bukkit;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import teacommontea.util.NmsFields;

final class ValueIoSaveBridge implements InvSeeAccess.SaveBridge {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final int KEY_NAMEANDID = 0;
    private static final int KEY_STRING_PAIR = 1;
    private static final int KEY_PLAYER = 2;
    private static final int KEY_STRING_PAIR_PR = 3;
    private static final int KEY_PLAYER_PR = 4;

    private final MethodHandle storageLoad;
    private final MethodHandle storageSave;
    private final MethodHandle tagValueInputCreate;
    private final Object problemReporter;
    private final Class<?> nameAndIdClass;
    private final java.lang.reflect.Constructor<?> nameAndIdCtor;
    private final int loadKeyKind;
    private final boolean loadYieldsValueInput;
    private final InvSeeAccess access;

    ValueIoSaveBridge(InvSeeAccess access) throws Throwable {
        this.access = access;
        Class<?> storageClass = InvSeeAccess.firstExisting(
                "net.minecraft.world.level.storage.PlayerDataStorage",
                "net.minecraft.world.level.storage.WorldNBTStorage");
        if (storageClass == null) {
            throw new InvSeeAccess.Unsupported("no PlayerDataStorage class");
        }

        Class<?> nameAndId = InvSeeAccess.classOrNull("net.minecraft.server.players.NameAndId");
        Class<?> playerClass = access.playerClass();
        Class<?> problemReporterType = InvSeeAccess.classOrNull("net.minecraft.util.ProblemReporter");
        Class<?> compoundTagType = InvSeeAccess.firstExisting(
                "net.minecraft.nbt.CompoundTag",
                "net.minecraft.nbt.NBTTagCompound");
        Class<?> valueInputType = InvSeeAccess.classOrNull("net.minecraft.world.level.storage.ValueInput");

        // Resolve the player-data load by SHAPE, never by method name: plain CraftBukkit is
        // Spigot-mapped at every version so the method name is obfuscated to a single letter, while
        // Paper keeps the Mojang name. The parameter and generic-return shapes fully identify each
        // load variant across the whole range. Preference order matches capability: NameAndId
        // (1.21.9+), Player+ProblemReporter yielding a ValueInput directly (1.21.6+), String pair +
        // ProblemReporter yielding a CompoundTag (1.21.6+), the plain String pair (1.21.5), then the
        // single Player argument (1.21.5). A variant that returns a ValueInput is used as-is; one that
        // returns a CompoundTag is wrapped through TagValueInput.create.
        Method load = null;
        int keyKind = -1;
        boolean yieldsValueInput = false;
        for (Method m : storageClass.getDeclaredMethods()) {
            if (m.isSynthetic() || m.isBridge()) continue;
            if (m.getReturnType() != Optional.class) continue;
            Class<?>[] p = m.getParameterTypes();
            boolean retValueInput = valueInputType != null
                    && m.getGenericReturnType().getTypeName().contains(valueInputType.getName());
            boolean retCompound = compoundTagType != null
                    && m.getGenericReturnType().getTypeName().contains(compoundTagType.getName());
            if (nameAndId != null && p.length == 1 && p[0] == nameAndId) {
                load = m; keyKind = KEY_NAMEANDID; yieldsValueInput = retValueInput; break;
            }
            if (problemReporterType != null && p.length == 2
                    && playerClass.isAssignableFrom(p[0]) && p[0] != Object.class
                    && p[1] == problemReporterType && retValueInput) {
                if (keyKind != KEY_NAMEANDID) { load = m; keyKind = KEY_PLAYER_PR; yieldsValueInput = true; }
            } else if (problemReporterType != null && p.length == 3
                    && p[0] == String.class && p[1] == String.class && p[2] == problemReporterType
                    && retCompound) {
                if (keyKind != KEY_NAMEANDID && keyKind != KEY_PLAYER_PR) {
                    load = m; keyKind = KEY_STRING_PAIR_PR; yieldsValueInput = false;
                }
            } else if (p.length == 2 && p[0] == String.class && p[1] == String.class && retCompound) {
                if (keyKind == -1 || keyKind == KEY_STRING_PAIR || keyKind == KEY_PLAYER) {
                    load = m; keyKind = KEY_STRING_PAIR; yieldsValueInput = false;
                }
            } else if (p.length == 1 && playerClass.isAssignableFrom(p[0]) && p[0] != Object.class) {
                if (keyKind == -1 || keyKind == KEY_PLAYER) {
                    load = m; keyKind = KEY_PLAYER; yieldsValueInput = retValueInput;
                }
            }
        }
        if (load == null) {
            throw new InvSeeAccess.Unsupported("no PlayerDataStorage.load on this server");
        }
        this.loadKeyKind = keyKind;
        this.loadYieldsValueInput = yieldsValueInput;
        this.nameAndIdClass = nameAndId;
        this.nameAndIdCtor = nameAndId != null
                ? nameAndId.getConstructor(UUID.class, String.class)
                : null;
        load.setAccessible(true);
        this.storageLoad = LOOKUP.unreflect(load);

        // save is the sole void method taking a single player-assignable argument; resolve by that
        // shape rather than the name, which CraftBukkit obfuscates.
        Method save = null;
        for (Method m : storageClass.getDeclaredMethods()) {
            if (m.isSynthetic() || m.isBridge()) continue;
            if (m.getReturnType() != void.class) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 1 && playerClass.isAssignableFrom(p[0]) && p[0] != Object.class) {
                save = m; break;
            }
        }
        if (save == null) {
            throw new InvSeeAccess.Unsupported("no PlayerDataStorage.save on this server");
        }
        save.setAccessible(true);
        this.storageSave = LOOKUP.unreflect(save);

        // These value-io shape classes drift by mapping: TagValueInput and ProblemReporter keep their
        // name, but CompoundTag is NBTTagCompound and HolderLookup.Provider is HolderLookup.a on
        // Spigot-mapped servers (all CraftBukkit versions), so list both names.
        Class<?> tagValueInput = InvSeeAccess.firstExisting(
                "net.minecraft.world.level.storage.TagValueInput");
        Class<?> problemReporterClass = InvSeeAccess.firstExisting(
                "net.minecraft.util.ProblemReporter");
        Class<?> registryClass = InvSeeAccess.firstExisting(
                "net.minecraft.core.HolderLookup$Provider",
                "net.minecraft.core.HolderLookup$a");
        Class<?> compoundTag = InvSeeAccess.firstExisting(
                "net.minecraft.nbt.CompoundTag",
                "net.minecraft.nbt.NBTTagCompound");
        if (tagValueInput == null || problemReporterClass == null
                || registryClass == null || compoundTag == null) {
            throw new InvSeeAccess.Unsupported("value-io save shapes missing");
        }
        // TagValueInput.create is the static factory taking (ProblemReporter, registry, CompoundTag);
        // resolve by that shape rather than the name. When the load variant already yields a ValueInput
        // there is nothing to wrap, so the factory is optional in that case.
        Method create = null;
        for (Method m : tagValueInput.getDeclaredMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 3 && p[2] == compoundTag && valueInputType != null
                    && valueInputType.isAssignableFrom(m.getReturnType())) {
                create = m; break;
            }
        }
        if (create == null && !loadYieldsValueInput) {
            throw new InvSeeAccess.Unsupported("no TagValueInput.create(_,_,CompoundTag)");
        }
        if (create != null) {
            create.setAccessible(true);
            this.tagValueInputCreate = LOOKUP.unreflect(create);
        } else {
            this.tagValueInputCreate = null;
        }

        Field discarding = null;
        for (Field f : problemReporterClass.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                    && problemReporterClass.isAssignableFrom(f.getType())) {
                discarding = f; break;
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
        Object loaded;
        switch (loadKeyKind) {
            case KEY_NAMEANDID -> loaded = storageLoad.invoke(storageArg(), nameAndIdCtor.newInstance(uuid, name));
            case KEY_STRING_PAIR -> loaded = storageLoad.invoke(storageArg(), uuid.toString(), name);
            case KEY_STRING_PAIR_PR -> loaded = storageLoad.invoke(storageArg(), uuid.toString(), name, problemReporter);
            case KEY_PLAYER_PR -> loaded = storageLoad.invoke(storageArg(), entity, problemReporter);
            case KEY_PLAYER -> loaded = storageLoad.invoke(storageArg(), entity);
            default -> { return Optional.empty(); }
        }
        @SuppressWarnings("unchecked")
        Optional<Object> tag = (Optional<Object>) loaded;
        if (tag == null || tag.isEmpty()) {
            return Optional.empty();
        }
        if (loadYieldsValueInput) {
            // the load already produced a ValueInput; hand it straight back
            return Optional.of(tag.get());
        }
        Object input = tagValueInputCreate.invoke(problemReporter, registry, tag.get());
        return Optional.of(input);
    }

    @Override
    public void save(Object nmsServerPlayer) throws Throwable {
        storageSave.invoke(storageArg(), nmsServerPlayer);
    }

    private Object storageArg() throws Throwable {
        return access.rawPlayerStorage();
    }
}
