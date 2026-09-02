package teacommontea.veritesauver.invsee;


public interface SlotHandler {

    Boolean mayPlace(Object slot, Object stack);

    Boolean hasItem(Object slot);

    boolean handleSet(Object slot, Object stack);

    int maxStackSize(Object slot);

    Object remove(Object slot, int amount);

    Boolean allowModification(Object slot, Object nmsPlayer);

    Boolean mayPickup(Object slot, Object nmsPlayer);

    Object getItem(Object slot);

    Boolean isActive(Object slot);

    Object noItemIcon(Object slot);
}
