package teacommontea.veritechasse.Vanilla.ControllableEntities.Support;

import teacommontea.veritechasse.Vanilla.Reality;

public final class FluidCurrent {

    public static final double WATER_FLOW_SCALE = 0.014D;
    public static final double LAVA_FAST_FLOW_SCALE = 0.007D;
    public static final double LAVA_SLOW_FLOW_SCALE = 0.0023333333333333335D;

    public static final double MINIMUM_FLOW_LENGTH = 1.0E-4D;

    public static final boolean BOAT_PASSENGER_EXEMPT_FROM_WATER = true;

    private FluidCurrent() {
    }

    public static double lavaFlowScale(boolean ultraWarmDimension) {
        return ultraWarmDimension ? LAVA_FAST_FLOW_SCALE : LAVA_SLOW_FLOW_SCALE;
    }

    public static double waterFlowScale() {
        return WATER_FLOW_SCALE;
    }

    public static double lavaMultiplierVersusOverworld() {
        return LAVA_FAST_FLOW_SCALE / LAVA_SLOW_FLOW_SCALE;
    }

    public static boolean pushedByWater(boolean ridingBoat) {
        if (ridingBoat) {
            return !BOAT_PASSENGER_EXEMPT_FROM_WATER;
        }
        return true;
    }

    public static double impulse(double normalisedFlowComponent, double flowScale) {
        return normalisedFlowComponent * flowScale;
    }

    public static double waterImpulse(double normalisedFlowComponent) {
        return impulse(normalisedFlowComponent, WATER_FLOW_SCALE);
    }

    public static double lavaImpulse(double normalisedFlowComponent, boolean ultraWarmDimension) {
        return impulse(normalisedFlowComponent, lavaFlowScale(ultraWarmDimension));
    }

    public static boolean flowIsSignificant(double flowLength) {
        return flowLength >= MINIMUM_FLOW_LENGTH;
    }

    public static Reality waterImpulseFrom(double normalisedFlowComponent) {
        return Reality.of(waterImpulse(normalisedFlowComponent));
    }

    public static Reality lavaImpulseFrom(double normalisedFlowComponent, boolean ultraWarmDimension) {
        return Reality.of(lavaImpulse(normalisedFlowComponent, ultraWarmDimension));
    }
}
