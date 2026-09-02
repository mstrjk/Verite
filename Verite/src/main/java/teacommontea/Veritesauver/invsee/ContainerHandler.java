package teacommontea.veritesauver.invsee;

import java.util.List;


public interface ContainerHandler {

    int getContainerSize();

    boolean isEmpty();

    Object getItem(int slot);

    Object removeItem(int slot, int amount);

    Object removeItemNoUpdate(int slot);

    void setItem(int slot, Object stack);

    int getMaxStackSize();

    void setMaxStackSize(int size);

    void setChanged();

    boolean stillValid(Object nmsPlayer);

    List<Object> getContents();

    List<org.bukkit.entity.HumanEntity> getViewers();

    void onOpen(org.bukkit.entity.HumanEntity who);

    void onClose(org.bukkit.entity.HumanEntity who);

    org.bukkit.inventory.InventoryHolder getOwner();

    org.bukkit.Location getLocation();
}
