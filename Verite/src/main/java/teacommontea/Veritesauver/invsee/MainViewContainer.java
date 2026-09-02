package teacommontea.veritesauver.invsee;

import java.util.ArrayList;
import java.util.List;


public final class MainViewContainer implements ContainerHandler {

    private final InvSeeAccess access;
    private final Object nmsTarget;
    private final Object targetInventory;
    private List<Object> personal;
    private int maxStack;
    private final int invSize;

    public MainViewContainer(InvSeeAccess access, Object nmsTarget) throws Throwable {
        this.access = access;
        this.nmsTarget = nmsTarget;
        this.targetInventory = access.inventoryOf(nmsTarget);
        this.personal = access.personalContentsOf(nmsTarget);
        this.invSize = access.inventorySize(targetInventory);
        this.maxStack = access.inventoryMaxStack(targetInventory);
    }

    public Object nmsTarget() {
        return nmsTarget;
    }

    public void setPersonal(List<Object> personal) {
        this.personal = personal;
    }

    public List<Object> personal() {
        return personal;
    }

    public int personalSize() {
        return personal.size();
    }

    private static final int POS_HELD = 38;
    private static final int POS_OFFHAND = 39;
    private static final int POS_HELMET = 41;
    private static final int POS_CHEST = 42;
    private static final int POS_LEGS = 43;
    private static final int POS_BOOTS = 44;

    private static final int NMS_BOOTS = 36;
    private static final int NMS_LEGGINGS = 37;
    private static final int NMS_CHESTPLATE = 38;
    private static final int NMS_HELMET = 39;
    private static final int NMS_OFFHAND = 40;

    private SlotRef decide(int slot) {
        if (slot >= 0 && slot < 36) {
            final int s = slot;
            return SlotRef.of(
                () -> safeGet(() -> access.inventoryGetItem(targetInventory, s)),
                item -> safeSet(() -> access.inventorySetItem(targetInventory, s, item)));
        }
        return switch (slot) {
            case POS_HELD -> flat(() -> access.selectedSlotOf(targetInventory));
            case POS_OFFHAND -> flat(() -> NMS_OFFHAND);
            case POS_HELMET -> flat(() -> NMS_HELMET);
            case POS_CHEST -> flat(() -> NMS_CHESTPLATE);
            case POS_LEGS -> flat(() -> NMS_LEGGINGS);
            case POS_BOOTS -> flat(() -> NMS_BOOTS);
            default -> null;
        };
    }

    private interface IntSup { int get(); }

    private SlotRef flat(IntSup index) {
        return SlotRef.of(
            () -> safeGet(() -> access.inventoryGetItem(targetInventory, index.get())),
            item -> safeSet(() -> access.inventorySetItem(targetInventory, index.get(), item)));
    }

    private Object menu() throws Throwable {
        return access.containerMenuOf(nmsTarget);
    }

    private interface Act { Object run() throws Throwable; }
    private interface Run { void run() throws Throwable; }

    private Object safeGet(Act a) {
        try {
            return a.run();
        } catch (Throwable t) {
            return null;
        }
    }

    private void safeSet(Run r) {
        try {
            r.run();
        } catch (Throwable ignored) {
        }
    }

    @Override
    public int getContainerSize() {
        return 45;
    }

    @Override
    public boolean isEmpty() {
        try {
            for (int i = 0; i < invSize; i++) {
                if (!access.isEmptyStack(access.inventoryGetItem(targetInventory, i))) {
                    return false;
                }
            }
            for (Object s : personal) {
                if (!access.isEmptyStack(s)) {
                    return false;
                }
            }
            return access.isEmptyStack(access.carriedOf(menu()));
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public Object getItem(int slot) {
        SlotRef ref = decide(slot);
        return ref == null ? emptyOrNull() : ref.get();
    }

    @Override
    public Object removeItem(int slot, int amount) {
        SlotRef ref = decide(slot);
        if (ref == null || amount <= 0) {
            return emptyOrNull();
        }
        try {
            Object current = ref.get();
            if (access.isEmptyStack(current)) {
                return emptyOrNull();
            }
            int have = access.stackCount(current);
            int take = Math.min(amount, have);
            Object taken = access.withStackCount(current, take);
            if (take >= have) {
                ref.set(emptyOrNull());
            } else {
                ref.set(access.withStackCount(current, have - take));
            }
            return taken;
        } catch (Throwable t) {
            return emptyOrNull();
        }
    }

    @Override
    public Object removeItemNoUpdate(int slot) {
        SlotRef ref = decide(slot);
        if (ref == null) {
            return emptyOrNull();
        }
        Object current = ref.get();
        if (access.isEmptyStack(current)) {
            return emptyOrNull();
        }
        ref.set(null);
        return current;
    }

    @Override
    public void setItem(int slot, Object stack) {
        SlotRef ref = decide(slot);
        if (ref == null) {
            return;
        }
        Object toStore = stack;
        try {
            if (!access.isEmptyStack(stack) && access.stackCount(stack) > maxStack) {
                toStore = access.withStackCount(stack, maxStack);
            }
        } catch (Throwable ignored) {
        }
        ref.set(toStore);
    }

    @Override
    public int getMaxStackSize() {
        return maxStack;
    }

    @Override
    public void setMaxStackSize(int size) {
        this.maxStack = size;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Object nmsPlayer) {
        return true;
    }

    @Override
    public List<Object> getContents() {
        List<Object> out = new ArrayList<>(45);
        for (int i = 0; i < 45; i++) {
            out.add(getItem(i));
        }
        return out;
    }

    private final List<org.bukkit.entity.HumanEntity> viewers = new ArrayList<>();

    @Override
    public List<org.bukkit.entity.HumanEntity> getViewers() {
        return viewers;
    }

    @Override
    public void onOpen(org.bukkit.entity.HumanEntity who) {
        viewers.add(who);
    }

    @Override
    public void onClose(org.bukkit.entity.HumanEntity who) {
        viewers.remove(who);
    }

    @Override
    public org.bukkit.inventory.InventoryHolder getOwner() {
        return null;
    }

    @Override
    public org.bukkit.Location getLocation() {
        return null;
    }

    private Object emptyOrNull() {
        try {
            return access.emptyStack();
        } catch (Throwable t) {
            return null;
        }
    }
}
