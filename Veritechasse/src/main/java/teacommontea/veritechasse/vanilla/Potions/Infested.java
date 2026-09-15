package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class Infested {

    public static final String KEY = "infested";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final float CHANCE_TO_SPAWN = 0.1F;
    public static final int MIN_SILVERFISH = 1;
    public static final int MAX_SILVERFISH = 2;

    public static final float LAUNCH_SCALE = 0.3F;
    public static final float LAUNCH_VERTICAL_SCALE = 1.5F;

    private Infested() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 20, 6);
    }

    public static boolean triggersOnHurt() {
        return true;
    }

    public static boolean spawns(float roll) {
        return roll <= CHANCE_TO_SPAWN;
    }

    public static Reality maxSilverfish() {
        return Reality.of(MAX_SILVERFISH);
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
