package teacommontea.veritechasse.Reality;

import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY.VerticalDrag;

public final class PlayerState {

    public static final double IMPULSE_NEGLIGIBLE = 1.0E-3D;

    public static final long IMPULSE_EXPIRY_TICKS = 100L;

    private double impulseHorizontal;
    private double impulseVertical;
    private long impulseGrantedTick;
    private boolean impulseHeld;

    private int hangTicks;
    private int stillTicks;

    private String surfaceKey;
    private int surfaceTicks;
    private double surfaceEntryY;

    private final java.util.Map<String, Integer> streaks = new java.util.HashMap<>();
    private final java.util.Map<String, Boolean> flags = new java.util.HashMap<>();
    private final java.util.Map<String, Double> buffers = new java.util.HashMap<>();

    public void authoriseImpulse(double horizontal, double vertical, long tick) {
        this.impulseHorizontal = Math.abs(horizontal);
        this.impulseVertical = Math.abs(vertical);
        this.impulseGrantedTick = tick;
        this.impulseHeld = true;
    }

    public boolean impulseIsLive(long tick) {
        if (!this.impulseHeld) {
            return false;
        }
        if (tick - this.impulseGrantedTick > IMPULSE_EXPIRY_TICKS) {
            clearImpulse();
            return false;
        }
        return true;
    }

    public double impulseHorizontal(long tick) {
        return impulseIsLive(tick) ? this.impulseHorizontal : 0.0D;
    }

    public double impulseVertical(long tick) {
        return impulseIsLive(tick) ? this.impulseVertical : 0.0D;
    }

    public void decayImpulse(long tick, float horizontalDrag) {
        if (!impulseIsLive(tick)) {
            return;
        }
        this.impulseHorizontal = this.impulseHorizontal * (double) horizontalDrag;
        this.impulseVertical = this.impulseVertical
            * (double) VerticalDrag.BASE_VERTICAL_AIR_DRAG;
        if (this.impulseHorizontal < IMPULSE_NEGLIGIBLE
                && this.impulseVertical < IMPULSE_NEGLIGIBLE) {
            clearImpulse();
        }
    }

    public void clearImpulse() {
        this.impulseHorizontal = 0.0D;
        this.impulseVertical = 0.0D;
        this.impulseHeld = false;
    }

    public int recordHangTick() {
        this.hangTicks = this.hangTicks + 1;
        return this.hangTicks;
    }

    public void clearHangTicks() {
        this.hangTicks = 0;
    }

    public int hangTicks() {
        return this.hangTicks;
    }

    public int recordStillTick() {
        this.stillTicks = this.stillTicks + 1;
        return this.stillTicks;
    }

    public void clearStillTicks() {
        this.stillTicks = 0;
    }

    public int stillTicks() {
        return this.stillTicks;
    }

    public int recordSurfaceTick(String key, double y) {
        if (!key.equals(this.surfaceKey)) {
            this.surfaceKey = key;
            this.surfaceTicks = 0;
            this.surfaceEntryY = y;
        }
        this.surfaceTicks = this.surfaceTicks + 1;
        return this.surfaceTicks;
    }

    public void clearSurface() {
        this.surfaceKey = null;
        this.surfaceTicks = 0;
        this.surfaceEntryY = 0.0D;
    }

    public String surfaceKey() {
        return this.surfaceKey;
    }

    public int surfaceTicks() {
        return this.surfaceTicks;
    }

    public double surfaceEntryY() {
        return this.surfaceEntryY;
    }

    public double descentSince(double y) {
        return this.surfaceEntryY - y;
    }

    public int advanceStreak(String key) {
        Integer existing = this.streaks.get(key);
        int next = existing == null ? 1 : existing.intValue() + 1;
        this.streaks.put(key, Integer.valueOf(next));
        return next;
    }

    public void clearStreak(String key) {
        this.streaks.remove(key);
    }

    public int streak(String key) {
        Integer existing = this.streaks.get(key);
        return existing == null ? 0 : existing.intValue();
    }

    public double buffer(String key) {
        Double existing = this.buffers.get(key);
        return existing == null ? 0.0D : existing.doubleValue();
    }

    public double increaseBuffer(String key, double amount) {
        double raised = buffer(key) + amount;
        this.buffers.put(key, Double.valueOf(raised));
        return raised;
    }

    public void decayBuffer(String key, double decay) {
        double shed = buffer(key) - decay;
        if (shed <= 0.0D) {
            this.buffers.remove(key);
            return;
        }
        this.buffers.put(key, Double.valueOf(shed));
    }

    public void clearBuffer(String key) {
        this.buffers.remove(key);
    }

    public boolean flag(String key) {
        Boolean existing = this.flags.get(key);
        return existing != null && existing.booleanValue();
    }

    public void setFlag(String key, boolean value) {
        this.flags.put(key, Boolean.valueOf(value));
    }

    public void reset() {
        clearImpulse();
        clearHangTicks();
        clearStillTicks();
        clearSurface();
        this.streaks.clear();
        this.flags.clear();
        this.buffers.clear();
    }
}
