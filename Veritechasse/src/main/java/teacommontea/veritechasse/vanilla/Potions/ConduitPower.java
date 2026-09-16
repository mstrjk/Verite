package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Potions.Haste;
import teacommontea.veritechasse.Vanilla.Reality;

public final class ConduitPower {

    public static final String KEY = "conduit_power";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    private ConduitPower() {
    }

    public static boolean grantsWaterBreathing() {
        return true;
    }

    public static boolean grantsDigSpeed() {
        return true;
    }

    public static boolean permitsAirRefill() {
        return true;
    }

    public static float digSpeedMultiplier(int amplifier) {
        return Haste.digSpeedMultiplier(amplifier);
    }

    public static float applyDigSpeed(float speed, int amplifier) {
        return Haste.applyDigSpeed(speed, amplifier);
    }

    public static boolean negatesSubmergedMiningPenalty() {
        return false;
    }

    public static Reality digSpeedFrom(float speed, int amplifier) {
        return Reality.of(applyDigSpeed(speed, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
