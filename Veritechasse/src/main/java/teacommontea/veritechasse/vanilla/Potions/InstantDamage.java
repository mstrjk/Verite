package teacommontea.veritechasse.vanilla.Potions;

import org.bukkit.entity.EntityType;

import teacommontea.veritechasse.vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.vanilla.Potions.Tags.InvertedHealingAndHarm;
import teacommontea.veritechasse.vanilla.Reality;

public final class InstantDamage {

    public static final String KEY = "instant_damage";
    public static final int VANILLA_MAX_AMPLIFIER = 1;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = true;

    public static final int HEAL_BASE = 4;
    public static final int HARM_BASE = 6;

    private InstantDamage() {
    }

    public static boolean invertedFor(EntityType type, EffectEra era) {
        return InvertedHealingAndHarm.contains(type, era);
    }

    public static boolean healsTarget(boolean inverted) {
        return inverted;
    }

    public static int healAmount(int amplifier) {
        return Math.max(HEAL_BASE << amplifier, 0);
    }

    public static int hurtAmount(int amplifier) {
        return HARM_BASE << amplifier;
    }

    public static int healAmount(int amplifier, double scale) {
        return (int) (scale * (double) (HEAL_BASE << amplifier) + 0.5D);
    }

    public static int hurtAmount(int amplifier, double scale) {
        return (int) (scale * (double) (HARM_BASE << amplifier) + 0.5D);
    }

    public static Reality healFrom(int amplifier) {
        return Reality.of(healAmount(amplifier));
    }

    public static Reality hurtFrom(int amplifier) {
        return Reality.of(hurtAmount(amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
