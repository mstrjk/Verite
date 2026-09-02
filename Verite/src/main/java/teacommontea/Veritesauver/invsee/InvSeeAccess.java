package teacommontea.veritesauver.invsee;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import teacommontea.util.NmsFields;

public final class InvSeeAccess {

    public static final class Unsupported extends Exception {
        private static final long serialVersionUID = 1L;
        Unsupported(String m) { super(m); }
        Unsupported(String m, Throwable c) { super(m, c); }
    }

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private final boolean mojangMapped;
    private final boolean valueIoSaves;

    private final Class<?> serverPlayerClass;
    private final Class<?> playerClass;
    private final Class<?> containerClass;
    private final Class<?> menuClass;
    private final Class<?> slotClass;
    private final Class<?> itemStackClass;
    private final Class<?> inventoryClass;
    private final Class<?> enderContainerClass;

    private final MethodHandle craftPlayerGetHandle;
    private final MethodHandle getInventory;
    private final MethodHandle getEnderChest;
    private final MethodHandle inventoryGetItem;
    private final MethodHandle inventorySetItem;
    private final MethodHandle inventorySize;
    private final MethodHandle inventoryMaxStack;
    private final MethodHandle containerGetCarried;
    private final MethodHandle containerSetCarried;
    private final MethodHandle personalContents;
    private final MethodHandle enderContents;
    private final MethodHandle asNmsCopy;
    private final MethodHandle asBukkitCopy;
    private final MethodHandle fromStringOrNull;

    private final MethodHandle stackToBukkit;
    private final MethodHandle stackFromBukkit;
    private final Field containerMenuField;

    private final Field emptyItemStack;

    private final Class<?> craftServerClass;
    private final MethodHandle craftServerGetHandle;
    private final Field playerIoField;
    private final MethodHandle playerDataDir;

    private SaveBridge saveBridgeRef;

    private InvSeeAccess(Builder b) {
        this.mojangMapped = b.mojangMapped;
        this.valueIoSaves = b.valueIoSaves;
        this.serverPlayerClass = b.serverPlayerClass;
        this.playerClass = b.playerClass;
        this.containerClass = b.containerClass;
        this.menuClass = b.menuClass;
        this.slotClass = b.slotClass;
        this.itemStackClass = b.itemStackClass;
        this.inventoryClass = b.inventoryClass;
        this.enderContainerClass = b.enderContainerClass;
        this.craftPlayerGetHandle = b.craftPlayerGetHandle;
        this.getInventory = b.getInventory;
        this.getEnderChest = b.getEnderChest;
        this.inventoryGetItem = b.inventoryGetItem;
        this.inventorySetItem = b.inventorySetItem;
        this.inventorySize = b.inventorySize;
        this.inventoryMaxStack = b.inventoryMaxStack;
        this.containerGetCarried = b.containerGetCarried;
        this.containerSetCarried = b.containerSetCarried;
        this.personalContents = b.personalContents;
        this.enderContents = b.enderContents;
        this.asNmsCopy = b.asNmsCopy;
        this.asBukkitCopy = b.asBukkitCopy;
        this.fromStringOrNull = b.fromStringOrNull;
        this.stackToBukkit = b.stackToBukkit;
        this.stackFromBukkit = b.stackFromBukkit;
        this.containerMenuField = b.containerMenuField;
        this.emptyItemStack = b.emptyItemStack;
        this.craftServerClass = b.craftServerClass;
        this.craftServerGetHandle = b.craftServerGetHandle;
        this.playerIoField = b.playerIoField;
        this.playerDataDir = b.playerDataDir;
    }

    public boolean mojangMapped() { return mojangMapped; }
    public boolean valueIoSaves() { return valueIoSaves; }

    public Class<?> serverPlayerClass() { return serverPlayerClass; }
    public Class<?> playerClass() { return playerClass; }
    public Class<?> containerClass() { return containerClass; }
    public Class<?> menuClass() { return menuClass; }
    public Class<?> slotClass() { return slotClass; }
    public Class<?> itemStackClass() { return itemStackClass; }

    public Object handleOf(Player p) throws Throwable {
        return craftPlayerGetHandle.invoke(p);
    }

    public Object inventoryOf(Object nmsPlayer) throws Throwable {
        return getInventory.invoke(nmsPlayer);
    }

    private volatile Object selectedSlotAccess = "\0";

    public int selectedSlotOf(Object inventory) {
        try {
            Object a = selectedSlotAccess;
            if ("\0".equals(a)) {
                a = resolveSelectedAccess(inventory.getClass());
                selectedSlotAccess = a;
            }
            if (a instanceof java.lang.reflect.Method m) {
                return (int) m.invoke(inventory);
            }
            if (a instanceof Field f) {
                return f.getInt(inventory);
            }
        } catch (Throwable ignored) {
        }
        return 0;
    }

    private static Object resolveSelectedAccess(Class<?> invClass) {
        for (Method m : invClass.getMethods()) {
            if (m.getParameterCount() == 0 && m.getReturnType() == int.class
                    && (m.getName().equals("getSelectedSlot") || m.getName().equals("getSelected"))) {
                m.setAccessible(true);
                return m;
            }
        }
        for (Class<?> c = invClass; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getType() == int.class
                        && (f.getName().equals("selected") || f.getName().equals("selectedSlot"))) {
                    f.setAccessible(true);
                    return f;
                }
            }
        }
        return null;
    }

    public Object enderChestOf(Object nmsPlayer) throws Throwable {
        return getEnderChest.invoke(nmsPlayer);
    }

    public Object inventoryGetItem(Object inventory, int slot) throws Throwable {
        return inventoryGetItem.invoke(inventory, slot);
    }

    public void inventorySetItem(Object inventory, int slot, Object stack) throws Throwable {
        inventorySetItem.invoke(inventory, slot, stack);
    }

    public int inventorySize(Object inventory) throws Throwable {
        return (int) inventorySize.invoke(inventory);
    }

    public int inventoryMaxStack(Object inventory) throws Throwable {
        return (int) inventoryMaxStack.invoke(inventory);
    }

    public Object carriedOf(Object menu) throws Throwable {
        return containerGetCarried.invoke(menu);
    }

    public void setCarried(Object menu, Object stack) throws Throwable {
        containerSetCarried.invoke(menu, stack);
    }

    @SuppressWarnings("unchecked")
    public List<Object> personalContentsOf(Object nmsPlayer) throws Throwable {
        return (List<Object>) personalContents.invoke(nmsPlayer);
    }

    @SuppressWarnings("unchecked")
    public List<Object> enderContentsOf(Object enderContainer) throws Throwable {
        return (List<Object>) enderContents.invoke(enderContainer);
    }

    public Object emptyStack() throws Throwable {
        return emptyItemStack.get(null);
    }

    public Object containerMenuOf(Object nmsPlayer) throws Throwable {
        return containerMenuField.get(nmsPlayer);
    }

    private org.bukkit.inventory.ItemStack toBukkit(Object nmsStack) throws Throwable {
        if (nmsStack == null) {
            return null;
        }
        if (stackToBukkit != null) {
            return (org.bukkit.inventory.ItemStack) stackToBukkit.invoke(nmsStack);
        }
        return (org.bukkit.inventory.ItemStack) asBukkitCopy.invoke(nmsStack);
    }

    private Object fromBukkit(org.bukkit.inventory.ItemStack bukkit) throws Throwable {
        if (bukkit == null) {
            return emptyStack();
        }
        if (stackFromBukkit != null) {
            return stackFromBukkit.invoke(bukkit);
        }
        return asNmsCopy.invoke(bukkit);
    }

    public boolean isEmptyStack(Object stack) {
        if (stack == null) {
            return true;
        }
        try {
            org.bukkit.inventory.ItemStack b = toBukkit(stack);
            return b == null || b.getType() == org.bukkit.Material.AIR || b.getAmount() <= 0;
        } catch (Throwable t) {
            return true;
        }
    }

    public int stackCount(Object stack) throws Throwable {
        org.bukkit.inventory.ItemStack b = toBukkit(stack);
        return b == null ? 0 : b.getAmount();
    }

    public Object withStackCount(Object stack, int count) throws Throwable {
        org.bukkit.inventory.ItemStack b = toBukkit(stack);
        if (b == null) {
            return stack;
        }
        b.setAmount(count);
        return fromBukkit(b);
    }


    public Object asNmsCopy(org.bukkit.inventory.ItemStack bukkit) throws Throwable {
        return asNmsCopy.invoke(bukkit);
    }

    public org.bukkit.inventory.ItemStack asBukkitCopy(Object nms) throws Throwable {
        return (org.bukkit.inventory.ItemStack) asBukkitCopy.invoke(nms);
    }

    public Object titleComponent(String title) throws Throwable {
        String legacy = teacommontea.util.text.Text.toLegacy(title);
        return fromStringOrNull.invoke(legacy);
    }

    public Object menuTypeForRows(int rows) throws Throwable {
        Class<?> menuType = firstExisting(
                "net.minecraft.world.inventory.MenuType",
                "net.minecraft.world.inventory.Containers");
        String constant = switch (rows) {
            case 1 -> "GENERIC_9x1";
            case 2 -> "GENERIC_9x2";
            case 3 -> "GENERIC_9x3";
            case 4 -> "GENERIC_9x4";
            case 5 -> "GENERIC_9x5";
            default -> "GENERIC_9x6";
        };
        for (Field f : menuType.getFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                    && menuType.isAssignableFrom(f.getType())
                    && f.getName().equals(constant)) {
                return f.get(null);
            }
        }
        java.util.List<Field> typed = new java.util.ArrayList<>();
        for (Field f : menuType.getFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())
                    && menuType.isAssignableFrom(f.getType())) {
                typed.add(f);
            }
        }
        int idx = Math.min(rows - 1, typed.size() - 1);
        return idx >= 0 ? typed.get(idx).get(null) : null;
    }

    private volatile Method openMenuMethod;

    public java.util.OptionalInt openMenu(Object nmsSpectator, Object menuProvider) throws Throwable {
        if (openMenuMethod == null) {
            for (Method m : serverPlayerClass.getMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (m.getName().equals("openMenu") && p.length == 1
                        && m.getReturnType() == java.util.OptionalInt.class) {
                    m.setAccessible(true);
                    openMenuMethod = m;
                    break;
                }
            }
            if (openMenuMethod == null) {
                for (Method m : serverPlayerClass.getMethods()) {
                    Class<?>[] p = m.getParameterTypes();
                    if (p.length == 1 && m.getReturnType() == java.util.OptionalInt.class
                            && p[0].getName().endsWith("MenuProvider")) {
                        m.setAccessible(true);
                        openMenuMethod = m;
                        break;
                    }
                }
            }
            if (openMenuMethod == null) {
                throw new Unsupported("no openMenu on " + serverPlayerClass.getName());
            }
        }
        Object r = openMenuMethod.invoke(nmsSpectator, menuProvider);
        return r instanceof java.util.OptionalInt oi ? oi : java.util.OptionalInt.empty();
    }

    public Class<?> menuProviderClass() {
        return firstExisting("net.minecraft.world.MenuProvider");
    }

    private volatile String checkReachableField = "\0";

    public String checkReachableFieldName() {
        if (!"\0".equals(checkReachableField)) {
            return checkReachableField;
        }
        String found = null;
        try {
            Field f = menuClass.getField("checkReachable");
            if (f.getType() == boolean.class) {
                found = "checkReachable";
            }
        } catch (NoSuchFieldException ignored) {
            for (Field f : menuClass.getDeclaredFields()) {
                if (f.getType() == boolean.class
                        && java.lang.reflect.Modifier.isPublic(f.getModifiers())
                        && !java.lang.reflect.Modifier.isStatic(f.getModifiers())
                        && !java.lang.reflect.Modifier.isFinal(f.getModifiers())) {
                    found = f.getName();
                    break;
                }
            }
        }
        checkReachableField = found;
        return found;
    }

    private volatile Method addSlotMethod;

    public void menuAddSlot(Object menu, Object slot) throws Throwable {
        if (addSlotMethod == null) {
            Method found = null;
            for (Method m : menuClass.getDeclaredMethods()) {
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 1 && slotClass.isAssignableFrom(p[0])
                        && slotClass.isAssignableFrom(m.getReturnType())
                        && java.lang.reflect.Modifier.isProtected(m.getModifiers())) {
                    m.setAccessible(true);
                    found = m;
                    break;
                }
            }
            if (found == null) {
                throw new Unsupported("no addSlot on " + menuClass.getName());
            }
            addSlotMethod = found;
        }
        addSlotMethod.invoke(menu, slot);
    }

    public java.io.File playerDataDir() throws Throwable {
        return (java.io.File) playerDataDir.invoke(rawPlayerStorage());
    }

    public Object rawPlayerStorage() throws Throwable {
        Object craftServer = craftServerClass.cast(Bukkit.getServer());
        Object dedicated = craftServerGetHandle.invoke(craftServer);
        return playerIoField.get(dedicated);
    }

    public String lastKnownNameOf(java.io.File datFile) {
        try {
            Class<?> nbtIo = firstExisting("net.minecraft.nbt.NbtIo",
                    "net.minecraft.nbt.NBTCompressedStreamTools");
            Class<?> accounter = firstExisting("net.minecraft.nbt.NbtAccounter");
            Class<?> compoundTag = firstExisting("net.minecraft.nbt.CompoundTag",
                    "net.minecraft.nbt.NBTTagCompound");
            if (nbtIo == null || compoundTag == null) {
                return null;
            }
            Object tag = readCompressed(nbtIo, accounter, datFile);
            if (tag == null) {
                return null;
            }
            Object bukkit = getCompound(compoundTag, tag, "bukkit");
            Object holder = bukkit != null ? bukkit : tag;
            return getString(compoundTag, holder, "lastKnownName");
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object readCompressed(Class<?> nbtIo, Class<?> accounter, java.io.File f)
            throws Throwable {
        if (accounter != null) {
            java.lang.reflect.Method unlimited = null;
            for (Method m : accounter.getMethods()) {
                if (java.lang.reflect.Modifier.isStatic(m.getModifiers())
                        && m.getParameterCount() == 0 && accounter.isAssignableFrom(m.getReturnType())) {
                    unlimited = m;
                    break;
                }
            }
            for (Method m : nbtIo.getMethods()) {
                if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                Class<?>[] p = m.getParameterTypes();
                if (p.length == 2 && p[1] == accounter
                        && (p[0] == java.nio.file.Path.class || p[0] == java.io.File.class
                            || java.io.InputStream.class.isAssignableFrom(p[0]))) {
                    Object acc = unlimited != null ? unlimited.invoke(null) : null;
                    Object arg0 = p[0] == java.nio.file.Path.class ? f.toPath()
                            : p[0] == java.io.File.class ? f
                            : new java.io.FileInputStream(f);
                    return m.invoke(null, arg0, acc);
                }
            }
        }
        for (Method m : nbtIo.getMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length == 1 && (p[0] == java.io.File.class
                    || java.io.InputStream.class.isAssignableFrom(p[0]))) {
                Object arg0 = p[0] == java.io.File.class ? f : new java.io.FileInputStream(f);
                return m.invoke(null, arg0);
            }
        }
        return null;
    }

    private static Object getCompound(Class<?> compoundTag, Object tag, String key) {
        try {
            try {
                Method m = compoundTag.getMethod("getCompoundOrEmpty", String.class);
                return m.invoke(tag, key);
            } catch (NoSuchMethodException e) {
                Method m = compoundTag.getMethod("getCompound", String.class);
                return m.invoke(tag, key);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static String getString(Class<?> compoundTag, Object tag, String key) {
        try {
            Method m = compoundTag.getMethod("getString", String.class);
            Object r = m.invoke(tag, key);
            if (r instanceof Optional<?> opt) {
                return (String) opt.orElse(null);
            }
            return (String) r;
        } catch (Throwable t) {
            return null;
        }
    }

    public Object rawServer() throws Throwable {
        Object craftServer = craftServerClass.cast(Bukkit.getServer());
        return craftServerGetHandle.invoke(craftServer);
    }

    public Object mainWorldHandle() throws Throwable {
        Object craftWorld = Bukkit.getWorlds().get(0);
        Class<?> cw = obc("CraftWorld");
        java.lang.reflect.Method getHandle = cw.getMethod("getHandle");
        getHandle.setAccessible(true);
        return getHandle.invoke(cw.cast(craftWorld));
    }

    public Object worldHandle(org.bukkit.World world) throws Throwable {
        Class<?> cw = obc("CraftWorld");
        java.lang.reflect.Method getHandle = cw.getMethod("getHandle");
        getHandle.setAccessible(true);
        return getHandle.invoke(cw.cast(world));
    }

    public Object registryAccessOf(Object entity) throws Throwable {
        for (Method m : entity.getClass().getMethods()) {
            if (m.getParameterCount() == 0
                    && m.getReturnType().getName().equals("net.minecraft.core.HolderLookup$Provider")) {
                m.setAccessible(true);
                return m.invoke(entity);
            }
        }
        for (Method m : entity.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && m.getName().equals("registryAccess")) {
                m.setAccessible(true);
                return m.invoke(entity);
            }
        }
        return null;
    }

    public void readSaveData(Object entity, Object valueInput) throws Throwable {
        for (Method m : entity.getClass().getMethods()) {
            if (m.getName().equals("readAdditionalSaveData") && m.getParameterCount() == 1) {
                m.setAccessible(true);
                m.invoke(entity, valueInput);
                return;
            }
        }
        for (Method m : entity.getClass().getMethods()) {
            if (m.getName().equals("load") && m.getParameterCount() == 1
                    && !m.getParameterTypes()[0].isPrimitive()) {
                m.setAccessible(true);
                m.invoke(entity, valueInput);
                return;
            }
        }
        throw new Unsupported("no readAdditionalSaveData on " + entity.getClass().getName());
    }

    public SaveBridge saves() { return saveBridgeRef; }

    public interface SaveBridge {
        Optional<Object> read(UUID uuid, String name, Object registry, Object entity) throws Throwable;
        void save(Object nmsServerPlayer) throws Throwable;
    }

    public static InvSeeAccess resolve() throws Unsupported {
        try {
            return new Builder().build();
        } catch (Unsupported u) {
            throw u;
        } catch (Throwable t) {
            throw new Unsupported("InvSee NMS resolution failed: " + t, t);
        }
    }

    private static final class Builder {
        boolean mojangMapped;
        boolean valueIoSaves;

        Class<?> serverPlayerClass;
        Class<?> playerClass;
        Class<?> containerClass;
        Class<?> menuClass;
        Class<?> slotClass;
        Class<?> itemStackClass;
        Class<?> inventoryClass;
        Class<?> enderContainerClass;

        MethodHandle craftPlayerGetHandle;
        MethodHandle getInventory;
        MethodHandle getEnderChest;
        MethodHandle inventoryGetItem;
        MethodHandle inventorySetItem;
        MethodHandle inventorySize;
        MethodHandle inventoryMaxStack;
        MethodHandle containerGetCarried;
        MethodHandle containerSetCarried;
        MethodHandle personalContents;
        MethodHandle enderContents;
        MethodHandle asNmsCopy;
        MethodHandle asBukkitCopy;
        MethodHandle fromStringOrNull;

        MethodHandle stackToBukkit;
        MethodHandle stackFromBukkit;
        Field containerMenuField;

        Field emptyItemStack;

        Class<?> craftServerClass;
        MethodHandle craftServerGetHandle;
        Field playerIoField;
        MethodHandle playerDataDir;

        InvSeeAccess build() throws Unsupported, Throwable {
            Class<?> craftPlayer = obc("entity.CraftPlayer");
            Method gh = craftPlayer.getMethod("getHandle");
            craftPlayerGetHandle = LOOKUP.unreflect(gh);
            serverPlayerClass = gh.getReturnType();

            playerClass = require(
                    "net.minecraft.world.entity.player.Player",
                    "net.minecraft.world.entity.player.EntityHuman");
            containerClass = require(
                    "net.minecraft.world.Container",
                    "net.minecraft.world.IInventory");
            menuClass = require(
                    "net.minecraft.world.inventory.AbstractContainerMenu",
                    "net.minecraft.world.inventory.Container");
            slotClass = require(
                    "net.minecraft.world.inventory.Slot");
            itemStackClass = require(
                    "net.minecraft.world.item.ItemStack");
            enderContainerClass = require(
                    "net.minecraft.world.inventory.PlayerEnderChestContainer",
                    "net.minecraft.world.inventory.InventoryEnderChest");

            mojangMapped = classOrNull("net.minecraft.world.entity.player.Inventory") != null;
            inventoryClass = require(
                    "net.minecraft.world.entity.player.Inventory",
                    "net.minecraft.world.entity.player.PlayerInventory");

            valueIoSaves = classOrNull("net.minecraft.world.level.storage.ValueInput") != null;

            getInventory = handle(method(playerClass, containerClass == null,
                    "getInventory", "fq", "gk", "getInventory"));
            getEnderChest = handle(firstMethodReturning(playerClass, enderContainerClass));

            inventoryGetItem = handle(methodByParams(containerClass, itemStackClass,
                    new Class<?>[]{ int.class }, "getItem", "a"));
            inventorySetItem = handle(methodByParams(containerClass, void.class,
                    new Class<?>[]{ int.class, itemStackClass }, "setItem", "a"));
            inventorySize = handle(methodByParams(containerClass, int.class,
                    new Class<?>[]{}, "getContainerSize", "getSize", "b"));
            inventoryMaxStack = handle(methodByParams(containerClass, int.class,
                    new Class<?>[]{}, "getMaxStackSize", "getMaxStackSize"));

            containerGetCarried = handle(methodByParams(menuClass, itemStackClass,
                    new Class<?>[]{}, "getCarried", "getCarried"));
            containerSetCarried = handle(methodByParams(menuClass, void.class,
                    new Class<?>[]{ itemStackClass }, "setCarried", "setCarried"));

            personalContents = resolvePersonalContents();
            enderContents = handle(firstMethodReturning(enderContainerClass, List.class));

            Class<?> craftItemStack = obc("inventory.CraftItemStack");
            asNmsCopy = handle(craftItemStack.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class));
            asBukkitCopy = handle(craftItemStack.getMethod("asBukkitCopy", itemStackClass));

            Class<?> craftChat = obc("util.CraftChatMessage");
            fromStringOrNull = handle(craftChat.getMethod("fromStringOrNull", String.class));

            Field emptyField;
            try {
                emptyField = itemStackClass.getField("EMPTY");
            } catch (NoSuchFieldException nsf) {
                emptyField = NmsFields.firstStaticFieldOfExactType(itemStackClass, itemStackClass);
                if (emptyField == null) {
                    throw new Unsupported("no empty ItemStack constant on " + itemStackClass.getName());
                }
                emptyField.setAccessible(true);
            }
            emptyItemStack = emptyField;

            // Paper adds no-arg convenience methods on the NMS ItemStack (asBukkitCopy / fromBukkitCopy);
            // plain CraftBukkit has neither, so fall back to the universal CraftItemStack statics that
            // every server has (asBukkitCopy(nms) and asNMSCopy(bukkit)) already resolved above.
            MethodHandle toBukkit;
            try {
                toBukkit = handle(itemStackClass.getMethod("asBukkitCopy"));
            } catch (NoSuchMethodException nsme) {
                toBukkit = null;
            }
            stackToBukkit = toBukkit;
            MethodHandle fromBukkit;
            try {
                fromBukkit = handle(itemStackClass.getMethod("fromBukkitCopy",
                        org.bukkit.inventory.ItemStack.class));
            } catch (NoSuchMethodException nsme) {
                fromBukkit = null;
            }
            stackFromBukkit = fromBukkit;

            containerMenuField = NmsFields.firstFieldOfAnyType(serverPlayerClass,
                    "net.minecraft.world.inventory.AbstractContainerMenu",
                    "net.minecraft.world.inventory.Container");
            if (containerMenuField == null) {
                containerMenuField = NmsFields.firstFieldAssignableTo(serverPlayerClass, menuClass);
            }
            if (containerMenuField == null) {
                throw new Unsupported("no containerMenu field on " + serverPlayerClass.getName());
            }
            containerMenuField.setAccessible(true);

            craftServerClass = obc("CraftServer");
            craftServerGetHandle = handle(craftServerClass.getMethod("getServer"));

            Class<?> dedicatedServer = craftServerGetHandle.type().returnType();
            Class<?> storageClass = require(
                    "net.minecraft.world.level.storage.PlayerDataStorage",
                    "net.minecraft.world.level.storage.WorldNBTStorage");
            playerIoField = NmsFields.firstFieldOfAnyType(deepest(dedicatedServer),
                    "net.minecraft.world.level.storage.PlayerDataStorage",
                    "net.minecraft.world.level.storage.WorldNBTStorage");
            if (playerIoField == null) {
                throw new Unsupported("no PlayerDataStorage field on " + dedicatedServer.getName());
            }
            playerIoField.setAccessible(true);
            playerDataDir = handle(firstMethodReturning(storageClass, java.io.File.class));

            InvSeeAccess access = new InvSeeAccess(this);
            if (valueIoSaves) {
                access.saveBridgeRef = new ValueIoSaveBridge(access);
            } else {
                access.saveBridgeRef = new CompoundTagSaveBridge(access);
            }
            return access;
        }

        private MethodHandle resolvePersonalContents() throws Throwable {
            Field invMenu = NmsFields.firstFieldOfAnyType(serverPlayerClass,
                    "net.minecraft.world.inventory.InventoryMenu");
            if (invMenu == null) {
                invMenu = NmsFields.firstFieldAssignableTo(serverPlayerClass,
                        classOrNull("net.minecraft.world.inventory.InventoryMenu"));
            }
            if (invMenu == null) {
                invMenu = NmsFields.firstFinalFieldProperSubtypeOf(serverPlayerClass, menuClass);
            }
            if (invMenu == null) {
                throw new Unsupported("no InventoryMenu field on " + serverPlayerClass.getName());
            }
            invMenu.setAccessible(true);
            final Field menuField = invMenu;
            Class<?> menuType = invMenu.getType();
            final Method craftSlots = firstMethodReturningRaw(menuType,
                    classOrNull("net.minecraft.world.inventory.CraftingContainer"),
                    classOrNull("net.minecraft.world.inventory.TransientCraftingContainer"),
                    classOrNull("net.minecraft.world.inventory.InventoryCrafting"),
                    classOrNull("net.minecraft.world.Container"));
            if (craftSlots == null) {
                throw new Unsupported("no craft-slots accessor on " + menuType.getName());
            }
            craftSlots.setAccessible(true);
            final Method contents = firstMethodReturning0(craftSlots.getReturnType(), List.class);
            if (contents == null) {
                throw new Unsupported("no contents accessor on " + craftSlots.getReturnType().getName());
            }
            contents.setAccessible(true);
            MethodHandle mf = LOOKUP.unreflectGetter(menuField);
            MethodHandle cs = LOOKUP.unreflect(craftSlots);
            MethodHandle ct = LOOKUP.unreflect(contents);
            return MethodHandles.filterReturnValue(
                    MethodHandles.filterReturnValue(mf, cs), ct);
        }

        Class<?> require(String... names) throws Unsupported {
            Class<?> c = firstExisting(names);
            if (c == null) {
                throw new Unsupported("no class among " + java.util.Arrays.toString(names));
            }
            return c;
        }
    }

    static MethodHandle handle(Method m) throws IllegalAccessException {
        m.setAccessible(true);
        return LOOKUP.unreflect(m);
    }

    static Method method(Class<?> c, boolean ignore, String... names) throws Unsupported {
        for (String n : names) {
            try {
                return c.getMethod(n);
            } catch (NoSuchMethodException ignored) {
            }
            try {
                return c.getDeclaredMethod(n);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new Unsupported("no method among " + java.util.Arrays.toString(names) + " on " + c.getName());
    }

    static Method methodByParams(Class<?> c, Class<?> ret, Class<?>[] params, String... names)
            throws Unsupported {
        for (String n : names) {
            try {
                Method m = c.getDeclaredMethod(n, params);
                if (ret == null || m.getReturnType() == ret) {
                    return m;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }
        for (Method m : c.getMethods()) {
            if (ret != null && m.getReturnType() != ret) continue;
            if (!java.util.Arrays.equals(m.getParameterTypes(), params)) continue;
            return m;
        }
        for (Method m : c.getDeclaredMethods()) {
            if (ret != null && m.getReturnType() != ret) continue;
            if (!java.util.Arrays.equals(m.getParameterTypes(), params)) continue;
            return m;
        }
        throw new Unsupported("no " + java.util.Arrays.toString(names) + java.util.Arrays.toString(params)
                + " on " + c.getName());
    }

    static Method firstMethodReturning(Class<?> c, Class<?> ret) throws Unsupported {
        Method m = firstMethodReturning0(c, ret);
        if (m == null) {
            throw new Unsupported("no zero-arg method returning " + ret.getName() + " on " + c.getName());
        }
        return m;
    }

    static Method firstMethodReturning0(Class<?> c, Class<?> ret) {
        for (Class<?> k = c; k != null && k != Object.class; k = k.getSuperclass()) {
            for (Method m : k.getDeclaredMethods()) {
                if (m.getParameterCount() == 0 && ret.isAssignableFrom(m.getReturnType())
                        && !java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
                    return m;
                }
            }
        }
        return null;
    }

    static Method firstMethodReturningRaw(Class<?> c, Class<?>... rets) {
        for (Class<?> ret : rets) {
            if (ret == null) continue;
            Method m = firstMethodReturning0(c, ret);
            if (m != null) return m;
        }
        return null;
    }

    static Class<?> deepest(Class<?> c) {
        return c;
    }

    static Class<?> firstExisting(String... names) {
        for (String n : names) {
            Class<?> c = classOrNull(n);
            if (c != null) return c;
        }
        return null;
    }

    static Class<?> classOrNull(String n) {
        try {
            return Class.forName(n, false, InvSeeAccess.class.getClassLoader());
        } catch (Throwable t) {
            return null;
        }
    }

    public static Class<?> obc(String sub) throws ClassNotFoundException {
        String base = Bukkit.getServer().getClass().getPackage().getName();
        try {
            return Class.forName(base + "." + sub);
        } catch (ClassNotFoundException e) {
            return Class.forName("org.bukkit.craftbukkit." + sub);
        }
    }
}
