package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class Weaving {

    public static final String KEY = "weaving";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final int MIN_COBWEBS = 2;
    public static final int MAX_COBWEBS = 3;

    public static final int SEARCH_ATTEMPTS = 15;
    public static final int SEARCH_RADIUS = 1;

    private Weaving() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 20, 6);
    }

    public static boolean triggersOnDeath() {
        return true;
    }

    public static boolean requiresMobGriefing(boolean isPlayer) {
        return !isPlayer;
    }

    public static boolean spawns(boolean isPlayer, boolean mobGriefing) {
        return isPlayer || mobGriefing;
    }

    public static Reality maxCobwebs() {
        return Reality.of(MAX_COBWEBS);
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
