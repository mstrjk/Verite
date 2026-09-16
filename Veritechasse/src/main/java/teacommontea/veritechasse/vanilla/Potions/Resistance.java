package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.Vanilla.Potions.Tags.BypassesEffects;
import teacommontea.veritechasse.Vanilla.Potions.Tags.BypassesResistance;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Resistance {

    public static final String KEY = "resistance";
    public static final int VANILLA_MAX_AMPLIFIER = 3;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final int SCALE = 25;
    public static final int REDUCTION_PER_AMPLIFIER = 5;

    public static final int IMMUNITY_AMPLIFIER = 4;

    private Resistance() {
    }

    public static int absorbValue(int amplifier) {
        return (amplifier + 1) * REDUCTION_PER_AMPLIFIER;
    }

    public static int remainingShare(int amplifier) {
        return SCALE - absorbValue(amplifier);
    }

    public static boolean grantsImmunity(int amplifier) {
        return remainingShare(amplifier) <= 0;
    }

    public static boolean bypassedBy(String damageType, EffectEra era) {
        return BypassesEffects.contains(damageType) || BypassesResistance.contains(damageType, era);
    }

    public static float damageAfter(float damage, int amplifier) {
        float scaled = damage * (float) remainingShare(amplifier);
        float result = scaled / (float) SCALE;
        return result < 0.0F ? 0.0F : result;
    }

    public static float damageAfter(float damage, int amplifier, String damageType, EffectEra era) {
        if (bypassedBy(damageType, era)) {
            return damage;
        }
        return damageAfter(damage, amplifier);
    }

    public static float damageResisted(float damage, int amplifier) {
        return damage - damageAfter(damage, amplifier);
    }

    public static Reality damageFrom(float damage, int amplifier) {
        return Reality.of(damageAfter(damage, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
