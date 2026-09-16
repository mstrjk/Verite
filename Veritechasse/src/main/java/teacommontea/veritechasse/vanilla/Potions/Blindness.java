package teacommontea.veritechasse.Vanilla.Potions;

public final class Blindness {

    public static final String KEY = "blindness";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final int BLEND_DURATION_TICKS = 0;

    private Blindness() {
    }

    public static boolean restrictsMobility() {
        return true;
    }

    public static boolean preventsCriticalAttack() {
        return true;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
