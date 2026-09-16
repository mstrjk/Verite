package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class TrialOmen {

    public static final String KEY = "trial_omen";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean NEUTRAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final int TICKS_PER_BAD_OMEN_LEVEL = 18000;
    public static final int APPLIED_AMPLIFIER = 0;

    private TrialOmen() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 20, 5);
    }

    public static int durationFromBadOmen(int badOmenAmplifier) {
        return TICKS_PER_BAD_OMEN_LEVEL * (badOmenAmplifier + 1);
    }

    public static int appliedAmplifier() {
        return APPLIED_AMPLIFIER;
    }

    public static boolean makesSpawnerOminous() {
        return true;
    }

    public static Reality durationFrom(int badOmenAmplifier) {
        return Reality.of(durationFromBadOmen(badOmenAmplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
