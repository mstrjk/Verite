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

    public void reset() {
        clearImpulse();
        clearHangTicks();
        clearStillTicks();
    }
}
