package teacommontea.veritechasse.Vanilla.Potions;

import org.bukkit.entity.EntityType;

import teacommontea.veritechasse.Vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.Vanilla.Potions.Support.TickInterval;
import teacommontea.veritechasse.Vanilla.Potions.Tags.IgnoresPoisonAndRegen;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Poison {

    public static final String KEY = "poison";
    public static final int VANILLA_MAX_AMPLIFIER = 1;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final int BASE_INTERVAL_TICKS = 25;
    public static final float DAMAGE_PER_APPLICATION = 1.0F;
    public static final float MINIMUM_HEALTH = 1.0F;

    private Poison() {
    }

    public static int intervalTicks(int amplifier) {
        return TickInterval.of(BASE_INTERVAL_TICKS, amplifier);
    }

    public static boolean appliesThisTick(int amplifier, int tickCount) {
        return TickInterval.appliesThisTick(BASE_INTERVAL_TICKS, amplifier, tickCount);
    }

    public static boolean saturated(int amplifier) {
        return TickInterval.saturated(BASE_INTERVAL_TICKS, amplifier);
    }

    public static boolean damages(float health) {
        return health > MINIMUM_HEALTH;
    }

    public static boolean canKill() {
        return false;
    }

    public static double damagePerSecond(int amplifier) {
        return TickInterval.applicationsPerSecond(BASE_INTERVAL_TICKS, amplifier) * DAMAGE_PER_APPLICATION;
    }

    public static float damageOver(int amplifier, int durationTicks, float health) {
        int interval = intervalTicks(amplifier);
        int applications = interval <= 0 ? durationTicks : durationTicks / interval;
        float dealt = (float) applications * DAMAGE_PER_APPLICATION;
        float survivable = health - MINIMUM_HEALTH;
        if (survivable <= 0.0F) {
            return 0.0F;
        }
        return dealt > survivable ? survivable : dealt;
    }

    public static boolean immune(EntityType type, EffectEra era) {
        return IgnoresPoisonAndRegen.contains(type, era);
    }

    public static Reality damageOverFrom(int amplifier, int durationTicks, float health) {
        return Reality.of(damageOver(amplifier, durationTicks, health));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
