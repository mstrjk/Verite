package teacommontea.veritechasse.vanilla.Potions.Support;

public final class TickInterval {

    private TickInterval() {
    }

    public static int of(int base, int amplifier) {
        if (amplifier < 0) {
            return base;
        }
        if (amplifier >= Integer.SIZE) {
            return 0;
        }
        return base >> amplifier;
    }

    public static boolean appliesThisTick(int base, int amplifier, int tickCount) {
        int interval = of(base, amplifier);
        if (interval <= 0) {
            return true;
        }
        return tickCount % interval == 0;
    }

    public static boolean saturated(int base, int amplifier) {
        return of(base, amplifier) <= 0;
    }

    public static int saturationAmplifier(int base) {
        int amplifier = 0;
        while (amplifier < Integer.SIZE && (base >> amplifier) > 0) {
            amplifier = amplifier + 1;
        }
        return amplifier;
    }

    public static double applicationsPerSecond(int base, int amplifier) {
        int interval = of(base, amplifier);
        if (interval <= 0) {
            return 20.0D;
        }
        return 20.0D / (double) interval;
    }
}
