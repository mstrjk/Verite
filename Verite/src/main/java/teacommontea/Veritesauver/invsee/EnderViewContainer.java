package teacommontea.veritesauver.invsee;

import java.util.ArrayList;
import java.util.List;


public final class EnderViewContainer implements ContainerHandler {

    private final InvSeeAccess access;
    private final List<Object> storage;
    private int maxStack;

    public EnderViewContainer(InvSeeAccess access, Object nmsTarget) throws Throwable {
        this.access = access;
        Object ender = access.enderChestOf(nmsTarget);
        this.storage = access.enderContentsOf(ender);
        this.maxStack = access.inventoryMaxStack(ender);
    }

    List<Object> storage() {
        return storage;
    }

    private boolean inRange(int slot) {
        return slot >= 0 && slot < storage.size();
    }

    @Override
    public int getContainerSize() {
        return storage.size();
    }

    @Override
    public boolean isEmpty() {
        for (Object s : storage) {
            if (!access.isEmptyStack(s)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object getItem(int slot) {
        return inRange(slot) ? storage.get(slot) : emptyOrNull();
    }

    @Override
    public Object removeItem(int slot, int amount) {
        if (!inRange(slot) || amount <= 0) {
            return emptyOrNull();
        }
        Object current = storage.get(slot);
        if (access.isEmptyStack(current)) {
            return emptyOrNull();
        }
        try {
            int have = access.stackCount(current);
            int take = Math.min(amount, have);
            Object taken = access.withStackCount(current, take);
            storage.set(slot, take >= have ? emptyOrNull() : access.withStackCount(current, have - take));
            return taken;
        } catch (Throwable t) {
            return emptyOrNull();
        }
    }

    @Override
    public Object removeItemNoUpdate(int slot) {
        if (!inRange(slot)) {
            return emptyOrNull();
        }
        Object current = storage.get(slot);
        if (access.isEmptyStack(current)) {
            return emptyOrNull();
        }
        storage.set(slot, emptyOrNull());
        return current;
    }

    @Override
    public void setItem(int slot, Object stack) {
        if (!inRange(slot)) {
            return;
        }
        Object toStore = stack;
        try {
            if (!access.isEmptyStack(stack) && access.stackCount(stack) > maxStack) {
                toStore = access.withStackCount(stack, maxStack);
            }
        } catch (Throwable ignored) {
        }
        storage.set(slot, toStore == null ? emptyOrNull() : toStore);
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
        return storage;
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
