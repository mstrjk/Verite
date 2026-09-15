package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Reality;

public final class DolphinsGrace {

    public static final String KEY = "dolphins_grace";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final float WATER_DRAG = 0.96F;

    private DolphinsGrace() {
    }

    public static float slowDown() {
        return WATER_DRAG;
    }

    public static boolean overridesDepthStrider() {
        return true;
    }

    public static Reality slowDownFrom() {
        return Reality.of(WATER_DRAG);
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
