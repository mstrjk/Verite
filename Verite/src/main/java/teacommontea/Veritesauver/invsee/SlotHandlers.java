package teacommontea.veritesauver.invsee;


final class SlotHandlers {

    private SlotHandlers() {}

    static final class Inaccessible implements SlotHandler {
        private final Object filler;
        private final Object empty;

        Inaccessible(Object filler, Object empty) {
            this.filler = filler;
            this.empty = empty;
        }

        @Override public Boolean mayPlace(Object slot, Object stack) { return false; }
        @Override public Boolean hasItem(Object slot) { return false; }
        @Override public boolean handleSet(Object slot, Object stack) { return true; }
        @Override public int maxStackSize(Object slot) { return 0; }
        @Override public Object remove(Object slot, int amount) { return empty; }
        @Override public Boolean allowModification(Object slot, Object nmsPlayer) { return false; }
        @Override public Boolean mayPickup(Object slot, Object nmsPlayer) { return false; }
        @Override public Object getItem(Object slot) { return filler; }
        @Override public Boolean isActive(Object slot) { return false; }
        @Override public Object noItemIcon(Object slot) { return null; }
    }

    static final class Personal implements SlotHandler {
        private final Object filler;
        private final Object empty;
        private final MainViewContainer view;
        private final int containerSlot;

        Personal(Object filler, Object empty, MainViewContainer view, int containerSlot) {
            this.filler = filler;
            this.empty = empty;
            this.view = view;
            this.containerSlot = containerSlot;
        }

        private boolean works() {
            return containerSlot >= 45 && containerSlot < 45 + view.personalSize();
        }

        @Override public Boolean mayPlace(Object slot, Object stack) { return works() ? null : false; }
        @Override public Boolean hasItem(Object slot) { return works() ? null : false; }
        @Override public boolean handleSet(Object slot, Object stack) { return !works(); }
        @Override public int maxStackSize(Object slot) { return works() ? -1 : 0; }
        @Override public Object remove(Object slot, int amount) { return works() ? null : empty; }
        @Override public Boolean allowModification(Object slot, Object nmsPlayer) { return works() ? null : false; }
        @Override public Boolean mayPickup(Object slot, Object nmsPlayer) { return works() ? null : false; }
        @Override public Object getItem(Object slot) { return works() ? null : filler; }
        @Override public Boolean isActive(Object slot) { return works(); }
        @Override public Object noItemIcon(Object slot) { return null; }
    }

    static final class Equipment implements SlotHandler {
        private final Object noItemIcon;

        Equipment(Object noItemIcon) {
            this.noItemIcon = noItemIcon;
        }

        @Override public Boolean mayPlace(Object slot, Object stack) { return null; }
        @Override public Boolean hasItem(Object slot) { return null; }
        @Override public boolean handleSet(Object slot, Object stack) { return false; }
        @Override public int maxStackSize(Object slot) { return -1; }
        @Override public Object remove(Object slot, int amount) { return null; }
        @Override public Boolean allowModification(Object slot, Object nmsPlayer) { return null; }
        @Override public Boolean mayPickup(Object slot, Object nmsPlayer) { return null; }
        @Override public Object getItem(Object slot) { return null; }
        @Override public Boolean isActive(Object slot) { return null; }
        @Override public Object noItemIcon(Object slot) { return noItemIcon; }
    }

    static final class Equip implements SlotHandler {
        private final Object filler;
        private final InvSeeAccess access;
        private final ContainerHandler container;
        private final int index;

        Equip(Object filler, InvSeeAccess access, ContainerHandler container, int index) {
            this.filler = filler;
            this.access = access;
            this.container = container;
            this.index = index;
        }

        boolean realEmpty() {
            try {
                return access.isEmptyStack(container.getItem(index));
            } catch (Throwable t) {
                return true;
            }
        }

        @Override public Boolean mayPlace(Object slot, Object stack) { return true; }
        @Override public Boolean hasItem(Object slot) { return realEmpty() ? false : null; }
        @Override public boolean handleSet(Object slot, Object stack) { return false; }
        @Override public int maxStackSize(Object slot) { return -1; }
        @Override public Object remove(Object slot, int amount) { return null; }
        @Override public Boolean allowModification(Object slot, Object nmsPlayer) { return null; }
        @Override public Boolean mayPickup(Object slot, Object nmsPlayer) {
            return realEmpty() ? false : null;
        }
        @Override public Object getItem(Object slot) { return realEmpty() ? filler : null; }
        @Override public Boolean isActive(Object slot) { return null; }
        @Override public Object noItemIcon(Object slot) { return null; }
    }

    static final class Plain implements SlotHandler {
        static final Plain INSTANCE = new Plain();

        @Override public Boolean mayPlace(Object slot, Object stack) { return null; }
        @Override public Boolean hasItem(Object slot) { return null; }
        @Override public boolean handleSet(Object slot, Object stack) { return false; }
        @Override public int maxStackSize(Object slot) { return -1; }
        @Override public Object remove(Object slot, int amount) { return null; }
        @Override public Boolean allowModification(Object slot, Object nmsPlayer) { return null; }
        @Override public Boolean mayPickup(Object slot, Object nmsPlayer) { return null; }
        @Override public Object getItem(Object slot) { return null; }
        @Override public Boolean isActive(Object slot) { return null; }
        @Override public Object noItemIcon(Object slot) { return null; }
    }
}
