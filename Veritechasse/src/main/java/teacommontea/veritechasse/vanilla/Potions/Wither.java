package teacommontea.veritechasse.Vanilla.Potions;

import teacommontea.veritechasse.Vanilla.Potions.Support.TickInterval;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Wither {

    public static final String KEY = "wither";
    public static final int VANILLA_MAX_AMPLIFIER = 1;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final int BASE_INTERVAL_TICKS = 40;
    public static final float DAMAGE_PER_APPLICATION = 1.0F;

    public static final String DAMAGE_TYPE = "wither";
    public static final float DAMAGE_TYPE_EXHAUSTION = 0.0F;

    private Wither() {
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

    public static boolean canKill() {
        return true;
    }

    public static double damagePerSecond(int amplifier) {
        return TickInterval.applicationsPerSecond(BASE_INTERVAL_TICKS, amplifier) * DAMAGE_PER_APPLICATION;
    }

    public static float damageOver(int amplifier, int durationTicks) {
        int interval = intervalTicks(amplifier);
        int applications = interval <= 0 ? durationTicks : durationTicks / interval;
        return (float) applications * DAMAGE_PER_APPLICATION;
    }

    public static Reality damageOverFrom(int amplifier, int durationTicks) {
        return Reality.of(damageOver(amplifier, durationTicks));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
