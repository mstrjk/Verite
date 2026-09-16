package teacommontea.veritechasse.Vanilla.Potions.Support;

import teacommontea.veritechasse.Vanilla.Potions.Haste;
import teacommontea.veritechasse.Vanilla.Potions.MiningFatigue;

public final class DigSpeed {

    public static final int ABSENT = -1;

    public static final float AIRBORNE_DIVISOR = 5.0F;

    private DigSpeed() {
    }

    public static int amplification(int hasteAmplifier, int conduitPowerAmplifier) {
        int haste = hasteAmplifier == ABSENT ? 0 : hasteAmplifier;
        int conduit = conduitPowerAmplifier == ABSENT ? 0 : conduitPowerAmplifier;
        return Math.max(haste, conduit);
    }

    public static boolean present(int hasteAmplifier, int conduitPowerAmplifier) {
        return hasteAmplifier != ABSENT || conduitPowerAmplifier != ABSENT;
    }

    public static float apply(float speed, int hasteAmplifier, int conduitPowerAmplifier, int miningFatigueAmplifier) {
        float result = speed;

        if (present(hasteAmplifier, conduitPowerAmplifier)) {
            result = Haste.applyDigSpeed(result, amplification(hasteAmplifier, conduitPowerAmplifier));
        }

        if (miningFatigueAmplifier != ABSENT) {
            result = MiningFatigue.applyDigSpeed(result, miningFatigueAmplifier);
        }

        return result;
    }

    public static float applyAirborne(float speed) {
        return speed / AIRBORNE_DIVISOR;
    }
}
