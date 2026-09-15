package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Reality;

public final class Levitation {

    public static final String KEY = "levitation";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = false;
    public static final boolean INSTANTANEOUS = false;

    public static final double RISE_PER_AMPLIFIER = 0.05D;
    public static final double APPROACH_RATE = 0.2D;

    private Levitation() {
    }

    public static double targetVelocity(int amplifier) {
        return RISE_PER_AMPLIFIER * (double) (amplifier + 1);
    }

    public static double nextVelocity(double currentY, int amplifier) {
        return currentY + (targetVelocity(amplifier) - currentY) * APPROACH_RATE;
    }

    public static boolean resetsFallDistance() {
        return true;
    }

    public static boolean preventsGliding() {
        return true;
    }

    public static boolean overridesGravity() {
        return true;
    }

    public static boolean descends(int amplifier) {
        return targetVelocity(amplifier) < 0.0D;
    }

    public static Reality velocityFrom(double currentY, int amplifier) {
        return Reality.of(nextVelocity(currentY, amplifier));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
