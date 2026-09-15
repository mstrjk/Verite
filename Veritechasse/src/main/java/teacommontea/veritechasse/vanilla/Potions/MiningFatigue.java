package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.vanilla.Reality;

public final class MiningFatigue {

    public static final String KEY = "mining_fatigue";
    public static final int VANILLA_MAX_AMPLIFIER = 3;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final double ATTACK_SPEED_PER_AMPLIFIER = -0.1D;
    public static final Operation OPERATION = Operation.ADD_MULTIPLIED_TOTAL;

    public static final float DIG_SPEED_AMPLIFIER_0 = 0.3F;
    public static final float DIG_SPEED_AMPLIFIER_1 = 0.09F;
    public static final float DIG_SPEED_AMPLIFIER_2 = 0.0027F;
    public static final float DIG_SPEED_AMPLIFIER_3_AND_ABOVE = 8.1E-4F;

    public static final int DIG_SPEED_SATURATION_AMPLIFIER = 3;

    private MiningFatigue() {
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
        switch (amplifier) {
            case 0:
                return DIG_SPEED_AMPLIFIER_0;
            case 1:
                return DIG_SPEED_AMPLIFIER_1;
            case 2:
                return DIG_SPEED_AMPLIFIER_2;
            default:
                return DIG_SPEED_AMPLIFIER_3_AND_ABOVE;
        }
    }

    public static float applyDigSpeed(float speed, int amplifier) {
        return speed * digSpeedMultiplier(amplifier);
    }

    public static boolean saturatesDigSpeed(int amplifier) {
        return amplifier >= DIG_SPEED_SATURATION_AMPLIFIER;
    }

    public static Reality digSpeedFrom(float speed, int amplifier) {
        return Reality.of(applyDigSpeed(speed, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
