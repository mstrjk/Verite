package teacommontea.veritechasse.Vanilla.Potions;

public final class NightVision {

    public static final String KEY = "night_vision";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;
    public static final boolean AFFECTS_SERVER_REALITY = false;

    public static final int FLICKER_THRESHOLD_TICKS = 200;
    public static final float FULL_SCALE = 1.0F;
    public static final float FLICKER_BASE = 0.7F;
    public static final float FLICKER_RANGE = 0.3F;

    private NightVision() {
    }

    public static boolean flickering(int durationTicks) {
        return durationTicks <= FLICKER_THRESHOLD_TICKS;
    }

    public static float scale(int durationTicks, float partialTick) {
        if (!flickering(durationTicks)) {
            return FULL_SCALE;
        }
        double phase = ((double) durationTicks - (double) partialTick) * Math.PI * 0.2D;
        return FLICKER_BASE + (float) Math.sin(phase) * FLICKER_RANGE;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
