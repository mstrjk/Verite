package teacommontea.veritesauver.invsee;


public final class Mirror {

    private final MainSlot[] positionToSlot;
    private final int[] slotToPosition;
    private final int[] enderPositionToSlot;
    private final boolean ender;

    private Mirror(MainSlot[] positionToSlot, int[] slotToPosition) {
        this.positionToSlot = positionToSlot;
        this.slotToPosition = slotToPosition;
        this.enderPositionToSlot = null;
        this.ender = false;
    }

    private Mirror(int[] enderPositionToSlot) {
        this.positionToSlot = null;
        this.slotToPosition = null;
        this.enderPositionToSlot = enderPositionToSlot;
        this.ender = true;
    }

    public boolean isEnder() {
        return ender;
    }

    public int gridSize() {
        return ender ? enderPositionToSlot.length : positionToSlot.length;
    }

    public MainSlot getSlot(int position) {
        if (ender || position < 0 || position >= positionToSlot.length) {
            return null;
        }
        return positionToSlot[position];
    }

    public int getIndex(MainSlot slot) {
        if (ender || slot == null) {
            return -1;
        }
        return slotToPosition[slot.ordinal()];
    }

    public int getEnderSlot(int position) {
        if (!ender || position < 0 || position >= enderPositionToSlot.length) {
            return -1;
        }
        return enderPositionToSlot[position];
    }

    public static Mirror defaultMain() {
        MainSlot[] pos = new MainSlot[MainSlot.GRID_SIZE];
        int[] slotPos = new int[MainSlot.values().length];
        java.util.Arrays.fill(slotPos, -1);
        for (int i = 0; i < pos.length; i++) {
            MainSlot slot = MainSlot.byDefaultIndex(i);
            pos[i] = slot;
            if (slot != null) {
                slotPos[slot.ordinal()] = i;
            }
        }
        return new Mirror(pos, slotPos);
    }

    public static Mirror defaultEnder(int size) {
        int[] pos = new int[size];
        for (int i = 0; i < size; i++) {
            pos[i] = i;
        }
        return new Mirror(pos);
    }

}
