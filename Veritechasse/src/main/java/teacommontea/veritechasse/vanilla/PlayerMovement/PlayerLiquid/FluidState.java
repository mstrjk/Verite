package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class FluidState {

    public static boolean swimmingImpliesSprinting(boolean swimming, boolean sprinting) {
        return swimming || sprinting;
    }

    public static final String WATER = "water";
    public static final String LAVA = "lava";

    public static final double JUMP_THRESHOLD = 0.4D;
    public static final double SHORT_ENTITY_EYE_HEIGHT = 0.4D;
    public static final double SHORT_ENTITY_THRESHOLD = 0.0D;

    public static final double SWIM_START_FLUID_HEIGHT = 0.4D;

    public static final int METHOD_SPLIT_PROTOCOL_MAJOR = 1;
    public static final int METHOD_SPLIT_PROTOCOL_MINOR = 21;
    public static final int METHOD_SPLIT_PROTOCOL_PATCH = 11;

    private FluidState() {
    }

    public static boolean travelMethodsAreSplit(Protocol protocol) {
        return protocol.atLeast(
            METHOD_SPLIT_PROTOCOL_MAJOR,
            METHOD_SPLIT_PROTOCOL_MINOR,
            METHOD_SPLIT_PROTOCOL_PATCH);
    }

    public static double jumpThreshold(double eyeHeight) {
        return eyeHeight < SHORT_ENTITY_EYE_HEIGHT ? SHORT_ENTITY_THRESHOLD : JUMP_THRESHOLD;
    }

    public static boolean isShallow(double fluidHeight, double eyeHeight) {
        return fluidHeight <= jumpThreshold(eyeHeight);
    }

    public static boolean travelsInFluid(
            boolean inWater,
            boolean inLava,
            boolean affectedByFluids,
            boolean canStandOnFluid) {
        if (!affectedByFluids || canStandOnFluid) {
            return false;
        }
        return inWater || inLava;
    }

    public static boolean waterTakesPriority(boolean inWater, boolean inLava) {
        if (inWater) {
            return true;
        }
        return !inLava;
    }

    public static boolean canSwim(boolean sprinting, boolean underWater, boolean passenger) {
        if (passenger || !sprinting) {
            return false;
        }
        return underWater;
    }

    public static boolean canJumpOutOfFluid(
            boolean horizontalCollision,
            boolean spaceAboveIsFree) {
        return horizontalCollision && spaceAboveIsFree;
    }
}
