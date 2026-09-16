package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.Vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.Vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.Vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Luck {

    public static final String KEY = "luck";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final double AMOUNT_PER_AMPLIFIER = 1.0D;
    public static final Operation OPERATION = Operation.ADD_VALUE;

    private Luck() {
    }

    public static Modifier modifier(int amplifier) {
        return new Modifier(AMOUNT_PER_AMPLIFIER, OPERATION).at(amplifier);
    }

    public static double bonus(int amplifier) {
        return AMOUNT_PER_AMPLIFIER * (double) (amplifier + 1);
    }

    public static double attributeValue(int amplifier) {
        return AttributePipeline.resolve(Attributes.LUCK, modifier(amplifier));
    }

    public static double luckFrom(double baseLuck, int amplifier) {
        return Attributes.LUCK.sanitize(baseLuck + bonus(amplifier));
    }

    public static Reality valueFrom(double baseLuck, int amplifier) {
        return Reality.of(luckFrom(baseLuck, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
