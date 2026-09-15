package teacommontea.veritechasse.vanilla.Potions;

public final class Glowing {

    public static final String KEY = "glowing";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean NEUTRAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;
    public static final boolean AFFECTS_SERVER_REALITY = false;

    private Glowing() {
    }

    public static boolean setsGlowing() {
        return true;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
