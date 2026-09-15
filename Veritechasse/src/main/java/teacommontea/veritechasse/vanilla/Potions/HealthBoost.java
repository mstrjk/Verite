package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Potions.Support.AttributePipeline;
import teacommontea.veritechasse.vanilla.Potions.Support.Attributes;
import teacommontea.veritechasse.vanilla.Potions.Support.Modifier;
import teacommontea.veritechasse.vanilla.Potions.Support.Operation;
import teacommontea.veritechasse.vanilla.Reality;

public final class HealthBoost {

    public static final String KEY = "health_boost";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final double HEALTH_PER_AMPLIFIER = 4.0D;
    public static final Operation OPERATION = Operation.ADD_VALUE;

    private HealthBoost() {
    }

    public static double bonusHealth(int amplifier) {
        return HEALTH_PER_AMPLIFIER * (double) (amplifier + 1);
    }

    public static Modifier modifier(int amplifier) {
        return new Modifier(HEALTH_PER_AMPLIFIER, OPERATION).at(amplifier);
    }

    public static double maxHealth(int amplifier) {
        return AttributePipeline.resolve(Attributes.MAX_HEALTH, modifier(amplifier));
    }

    public static double maxHealthFrom(double baseMaxHealth, int amplifier) {
        double total = baseMaxHealth + bonusHealth(amplifier);
        if (total > Attributes.MAX_HEALTH.max()) {
            return Attributes.MAX_HEALTH.max();
        }
        if (total < Attributes.MAX_HEALTH.min()) {
            return Attributes.MAX_HEALTH.min();
        }
        return total;
    }

    public static double healthOnRemoval(double currentHealth, double baseMaxHealth) {
        return currentHealth > baseMaxHealth ? baseMaxHealth : currentHealth;
    }

    public static Reality healthFrom(double baseMaxHealth, int amplifier) {
        return Reality.of(maxHealthFrom(baseMaxHealth, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
