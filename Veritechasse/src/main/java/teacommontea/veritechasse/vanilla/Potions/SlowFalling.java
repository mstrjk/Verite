package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class SlowFalling {

    public static final String KEY = "slow_falling";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    public static final double SLOW_GRAVITY = 0.01D;
    public static final double DEFAULT_GRAVITY = 0.08D;

    private SlowFalling() {
    }

    public static boolean applies(double deltaY) {
        return deltaY <= 0.0D;
    }

    public static boolean gravityIsAttributeDriven(Protocol protocol) {
        return protocol.atLeast(1, 20, 5);
    }

    public static double effectiveGravity(double gravity, double deltaY, Protocol protocol) {
        if (!applies(deltaY)) {
            return gravity;
        }
        if (gravityIsAttributeDriven(protocol)) {
            return gravity < SLOW_GRAVITY ? gravity : SLOW_GRAVITY;
        }
        return SLOW_GRAVITY;
    }

    public static double effectiveGravity(double deltaY, Protocol protocol) {
        return effectiveGravity(DEFAULT_GRAVITY, deltaY, protocol);
    }

    public static boolean resetsFallDistance() {
        return true;
    }

    public static Reality gravityFrom(double gravity, double deltaY, Protocol protocol) {
        return Reality.of(effectiveGravity(gravity, deltaY, protocol));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
