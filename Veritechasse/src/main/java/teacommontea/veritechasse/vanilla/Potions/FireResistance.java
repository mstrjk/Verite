package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.vanilla.Potions.Tags.IsFire;
import teacommontea.veritechasse.vanilla.Reality;

public final class FireResistance {

    public static final String KEY = "fire_resistance";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    private FireResistance() {
    }

    public static boolean blocks(String damageType, EffectEra era) {
        return IsFire.contains(damageType, era);
    }

    public static float damageAfter(float damage, String damageType, EffectEra era) {
        return blocks(damageType, era) ? 0.0F : damage;
    }

    public static Reality damageFrom(float damage, String damageType, EffectEra era) {
        if (blocks(damageType, era)) {
            return Reality.impossible();
        }
        return Reality.of(damage);
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
