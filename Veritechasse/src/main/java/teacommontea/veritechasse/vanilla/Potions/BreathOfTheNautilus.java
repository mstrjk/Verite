package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Protocol;

public final class BreathOfTheNautilus {

    public static final String KEY = "breath_of_the_nautilus";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final int GRANTED_DURATION_TICKS = 60;

    private BreathOfTheNautilus() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 21, 11);
    }

    public static boolean preventsDrowning() {
        return true;
    }

    public static boolean permitsAirRefill(boolean waterBreathing, boolean conduitPower) {
        return waterBreathing || conduitPower;
    }

    public static boolean blocksAirRefillAlone() {
        return true;
    }

    public static int grantedDurationTicks() {
        return GRANTED_DURATION_TICKS;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
