package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.Vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.Vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.Vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.Vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Absorption {

    public static final String KEY = "absorption";
    public static final int VANILLA_MAX_AMPLIFIER = 3;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final double HEARTS_PER_AMPLIFIER = 4.0D;
    public static final Operation OPERATION = Operation.ADD_VALUE;

    private Absorption() {
    }

    public static double granted(int amplifier) {
        return HEARTS_PER_AMPLIFIER * (double) (amplifier + 1);
    }

    public static Modifier modifier(int amplifier) {
        return new Modifier(HEARTS_PER_AMPLIFIER, OPERATION).at(amplifier);
    }

    public static double maxAbsorption(int amplifier) {
        return AttributePipeline.resolve(Attributes.MAX_ABSORPTION, modifier(amplifier));
    }

    public static double onApplied(double currentAbsorption, int amplifier, EffectEra era) {
        double granted = granted(amplifier);
        if (era == EffectEra.EFFECTS_AS_MODIFIERS) {
            return currentAbsorption > granted ? currentAbsorption : granted;
        }
        return currentAbsorption + granted;
    }

    public static double onRemoved(double currentAbsorption, int amplifier, EffectEra era) {
        if (era == EffectEra.EFFECTS_AS_MODIFIERS) {
            return currentAbsorption;
        }
        double remaining = currentAbsorption - granted(amplifier);
        return remaining < 0.0D ? 0.0D : remaining;
    }

    public static boolean expires(double currentAbsorption) {
        return currentAbsorption <= 0.0D;
    }

    public static Reality absorptionFrom(double currentAbsorption, int amplifier, EffectEra era) {
        return Reality.of(onApplied(currentAbsorption, amplifier, era));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
