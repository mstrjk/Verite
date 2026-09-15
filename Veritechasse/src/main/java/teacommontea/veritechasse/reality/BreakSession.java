package teacommontea.veritechasse.reality;

public final class BreakSession {

    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final String blockName;
    private final long startTick;
    private final float perTickProgress;
    private final boolean instaBreak;

    public BreakSession(
            int blockX,
            int blockY,
            int blockZ,
            String blockName,
            long startTick,
            float perTickProgress,
            boolean instaBreak) {
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.blockName = blockName;
        this.startTick = startTick;
        this.perTickProgress = perTickProgress;
        this.instaBreak = instaBreak;
    }

    public int blockX() {
        return this.blockX;
    }

    public int blockY() {
        return this.blockY;
    }

    public int blockZ() {
        return this.blockZ;
    }

    public String blockName() {
        return this.blockName;
    }

    public long startTick() {
        return this.startTick;
    }

    public float perTickProgress() {
        return this.perTickProgress;
    }

    public boolean instaBreak() {
        return this.instaBreak;
    }

    public boolean matches(int x, int y, int z) {
        return this.blockX == x && this.blockY == y && this.blockZ == z;
    }

    public int elapsedTicks(long currentTick) {
        long elapsed = currentTick - this.startTick;
        if (elapsed < 0L) {
            return 0;
        }
        return elapsed > (long) Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) elapsed;
    }
}
