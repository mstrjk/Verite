package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.Vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.Vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.Vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Haste {

    public static final String KEY = "haste";
    public static final int VANILLA_MAX_AMPLIFIER = 2;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final double ATTACK_SPEED_PER_AMPLIFIER = 0.1D;
    public static final Operation OPERATION = Operation.ADD_MULTIPLIED_TOTAL;

    public static final float DIG_SPEED_PER_AMPLIFIER = 0.2F;

    private Haste() {
    }

    public static Modifier attackSpeedModifier(int amplifier) {
        return new Modifier(ATTACK_SPEED_PER_AMPLIFIER, OPERATION).at(amplifier);
    }

    public static double attackSpeedMultiplier(int amplifier) {
        return 1.0D + ATTACK_SPEED_PER_AMPLIFIER * (double) (amplifier + 1);
    }

    public static double attackSpeedValue(int amplifier) {
        return AttributePipeline.resolve(Attributes.ATTACK_SPEED, attackSpeedModifier(amplifier));
    }

    public static float digSpeedMultiplier(int amplifier) {
        return 1.0F + (float) (amplifier + 1) * DIG_SPEED_PER_AMPLIFIER;
    }

    public static float applyDigSpeed(float speed, int amplifier) {
        return speed * digSpeedMultiplier(amplifier);
    }

    public static Reality digSpeedFrom(float speed, int amplifier) {
        return Reality.of(applyDigSpeed(speed, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
