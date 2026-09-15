package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.vanilla.Reality;

public final class Speed {

    public static final String KEY = "speed";
    public static final int VANILLA_MAX_AMPLIFIER = 1;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final double AMOUNT_PER_AMPLIFIER = 0.2D;
    public static final Operation OPERATION = Operation.ADD_MULTIPLIED_TOTAL;

    private Speed() {
    }

    public static Modifier modifier(int amplifier) {
        return new Modifier(AMOUNT_PER_AMPLIFIER, OPERATION).at(amplifier);
    }

    public static double multiplier(int amplifier) {
        return 1.0D + AMOUNT_PER_AMPLIFIER * (double) (amplifier + 1);
    }

    public static double movementSpeed(double baseSpeed, int amplifier) {
        return baseSpeed * multiplier(amplifier);
    }

    public static double attributeValue(int amplifier) {
        return AttributePipeline.resolve(Attributes.MOVEMENT_SPEED, modifier(amplifier));
    }

    public static Reality speedFrom(double baseSpeed, int amplifier) {
        return Reality.of(movementSpeed(baseSpeed, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
