package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.Vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.Vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.Vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Strength {

    public static final String KEY = "strength";
    public static final int VANILLA_MAX_AMPLIFIER = 1;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final double AMOUNT_PER_AMPLIFIER = 3.0D;
    public static final Operation OPERATION = Operation.ADD_VALUE;

    private Strength() {
    }

    public static Modifier modifier(int amplifier) {
        return new Modifier(AMOUNT_PER_AMPLIFIER, OPERATION).at(amplifier);
    }

    public static double bonusDamage(int amplifier) {
        return AMOUNT_PER_AMPLIFIER * (double) (amplifier + 1);
    }

    public static double attackDamage(double baseDamage, int amplifier) {
        double total = baseDamage + bonusDamage(amplifier);
        return total > Attributes.ATTACK_DAMAGE.max() ? Attributes.ATTACK_DAMAGE.max() : total;
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
