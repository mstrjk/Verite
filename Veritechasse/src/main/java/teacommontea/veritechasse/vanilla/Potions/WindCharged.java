package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class WindCharged {

    public static final String KEY = "wind_charged";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final float MIN_GUST_STRENGTH = 3.0F;
    public static final float MAX_GUST_STRENGTH = 5.0F;
    public static final float GUST_STRENGTH_RANGE = 2.0F;

    private WindCharged() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 20, 5);
    }

    public static boolean triggersOnDeath() {
        return true;
    }

    public static float gustStrength(float roll) {
        return MIN_GUST_STRENGTH + roll * GUST_STRENGTH_RANGE;
    }

    public static boolean damagesBlocks() {
        return false;
    }

    public static Reality maxGustStrength() {
        return Reality.of(MAX_GUST_STRENGTH);
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
