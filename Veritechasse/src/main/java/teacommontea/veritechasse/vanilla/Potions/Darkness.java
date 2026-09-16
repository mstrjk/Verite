package teacommontea.veritechasse.Vanilla.Potions;

public final class Darkness {

    public static final String KEY = "darkness";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;
    public static final boolean AFFECTS_SERVER_REALITY = false;

    public static final int BLEND_DURATION_TICKS = 22;

    private Darkness() {
    }

    public static int blendInTicks() {
        return BLEND_DURATION_TICKS;
    }

    public static int blendOutTicks() {
        return BLEND_DURATION_TICKS;
    }

    public static int blendOutAdvanceTicks() {
        return BLEND_DURATION_TICKS;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
