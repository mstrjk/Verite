package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.Vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.Vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.Vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Weakness {

    public static final String KEY = "weakness";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final double AMOUNT_PER_AMPLIFIER = -4.0D;
    public static final Operation OPERATION = Operation.ADD_VALUE;

    private Weakness() {
    }

    public static Modifier modifier(int amplifier) {
        return new Modifier(AMOUNT_PER_AMPLIFIER, OPERATION).at(amplifier);
    }

    public static double damagePenalty(int amplifier) {
        return AMOUNT_PER_AMPLIFIER * (double) (amplifier + 1);
    }

    public static double attackDamage(double baseDamage, int amplifier) {
        double total = baseDamage + damagePenalty(amplifier);
        return total < Attributes.ATTACK_DAMAGE.min() ? Attributes.ATTACK_DAMAGE.min() : total;
    }

    public static boolean disarms(double baseDamage, int amplifier) {
        return baseDamage + damagePenalty(amplifier) <= Attributes.ATTACK_DAMAGE.min();
    }

    public static double attributeValue(int amplifier) {
        return AttributePipeline.resolve(Attributes.ATTACK_DAMAGE, modifier(amplifier));
    }

    public static Reality damageFrom(double baseDamage, int amplifier) {
        return Reality.of(attackDamage(baseDamage, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
