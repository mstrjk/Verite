package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid;

import teacommontea.veritechasse.Vanilla.Reality;

public final class LavaMotion {

    public static final float SPEED = 0.02F;

    public static final double SHALLOW_HORIZONTAL_DRAG = 0.5D;
    public static final float SHALLOW_VERTICAL_DRAG = 0.8F;
    public static final double DEEP_DRAG = 0.5D;

    public static final double GRAVITY_DIVISOR = 4.0D;

    public static final boolean DEPTH_STRIDER_APPLIES = false;
    public static final boolean SPRINTING_APPLIES = false;

    private LavaMotion() {
    }

    public static double horizontalAfter(double component, boolean shallow) {
        return component * (shallow ? SHALLOW_HORIZONTAL_DRAG : DEEP_DRAG);
    }

    public static double verticalAfter(double component, boolean shallow) {
        return component * (shallow ? (double) SHALLOW_VERTICAL_DRAG : DEEP_DRAG);
    }

    public static double gravityPull(double baseGravity) {
        if (baseGravity == 0.0D) {
            return 0.0D;
        }
        return -baseGravity / GRAVITY_DIVISOR;
    }

    public static double nextVertical(double deltaY, double baseGravity, boolean shallow) {
        return verticalAfter(deltaY, shallow) + gravityPull(baseGravity);
    }

    public static double terminalDescent(double baseGravity, boolean shallow) {
        double drag = shallow ? (double) SHALLOW_VERTICAL_DRAG : DEEP_DRAG;
        if (drag >= 1.0D) {
            return Double.NEGATIVE_INFINITY;
        }
        return gravityPull(baseGravity) / (1.0D - drag);
    }

    public static double terminalSpeed(float acceleration, boolean shallow) {
        double drag = shallow ? SHALLOW_HORIZONTAL_DRAG : DEEP_DRAG;
        if (drag >= 1.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) acceleration * drag / (1.0D - drag);
    }

    public static final int SINK_TICK_LIMIT = 200;

    public static int ticksBeforeDescentIsVisible(
            double epsilon, double baseGravity, boolean shallow) {
        double descended = 0.0D;
        double carried = 0.0D;
        int ticks = 0;
        while (descended < epsilon && ticks < SINK_TICK_LIMIT) {
            ticks = ticks + 1;
            carried = nextVertical(carried, baseGravity, shallow);
            descended = descended - carried;
        }
        return ticks;
    }

    public static boolean isShallow(double lavaHeight, double jumpThreshold) {
        return lavaHeight <= jumpThreshold;
    }

    public static Reality horizontalFrom(boolean shallow) {
        return Reality.of(terminalSpeed(SPEED, shallow));
    }
}
