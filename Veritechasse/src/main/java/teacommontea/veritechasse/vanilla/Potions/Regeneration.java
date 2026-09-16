package teacommontea.veritechasse.Vanilla.Potions;

import org.bukkit.entity.EntityType;

import teacommontea.veritechasse.Vanilla.Potions.Support.EffectEra;
import teacommontea.veritechasse.Vanilla.Potions.Support.TickInterval;
import teacommontea.veritechasse.Vanilla.Potions.Tags.IgnoresPoisonAndRegen;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Regeneration {

    public static final String KEY = "regeneration";
    public static final int VANILLA_MAX_AMPLIFIER = 1;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final int BASE_INTERVAL_TICKS = 50;
    public static final float HEAL_PER_APPLICATION = 1.0F;

    private Regeneration() {
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

    public static double healPerSecond(int amplifier) {
        return TickInterval.applicationsPerSecond(BASE_INTERVAL_TICKS, amplifier) * HEAL_PER_APPLICATION;
    }

    public static float healOver(int amplifier, int durationTicks) {
        int interval = intervalTicks(amplifier);
        if (interval <= 0) {
            return (float) durationTicks * HEAL_PER_APPLICATION;
        }
        return (float) (durationTicks / interval) * HEAL_PER_APPLICATION;
    }

    public static boolean immune(EntityType type, EffectEra era) {
        return IgnoresPoisonAndRegen.contains(type, era);
    }

    public static Reality healOverFrom(int amplifier, int durationTicks) {
        return Reality.of(healOver(amplifier, durationTicks));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
