package teacommontea.veritechasse.Vanilla.PlayerMovement.Support;

public final class SurfaceExemption {

    public static final String WATER = "water";
    public static final String LAVA = "lava";
    public static final String POWDER_SNOW = "powder_snow";

    public static final String SPECTATOR = "spectator";

    private SurfaceExemption() {
    }

    public static boolean flightIsAuthorised(boolean flying, boolean mayFly) {
        return flying || mayFly;
    }

    public static boolean isSpectator(String gameMode) {
        return SPECTATOR.equals(gameMode);
    }

    public static boolean verticalIsExternallyDriven(
            boolean gliding,
            boolean rocketActive,
            boolean levitating,
            boolean riptiding,
            boolean inBubbleColumn) {
        return gliding || rocketActive || levitating || riptiding || inBubbleColumn;
    }

    public static boolean sinkExpectationIsValid(
            boolean passenger,
            boolean flying,
            boolean mayFly,
            String gameMode,
            boolean gliding,
            boolean rocketActive,
            boolean levitating,
            boolean riptiding,
            boolean inBubbleColumn,
            boolean supportedByCollision) {
        if (passenger) {
            return false;
        }
        if (flightIsAuthorised(flying, mayFly)) {
            return false;
        }
        if (isSpectator(gameMode)) {
            return false;
        }
        if (verticalIsExternallyDriven(
                gliding, rocketActive, levitating, riptiding, inBubbleColumn)) {
            return false;
        }
        return !supportedByCollision;
    }

    public static boolean surfaceChanged(String previousSurface, String currentSurface) {
        if (previousSurface == null) {
            return currentSurface != null;
        }
        return !previousSurface.equals(currentSurface);
    }

    public static boolean fluidContactChanged(
            boolean previousTouching, boolean currentTouching) {
        return previousTouching != currentTouching;
    }

    public static boolean poseChanged(String previousPose, String currentPose) {
        if (previousPose == null) {
            return currentPose != null;
        }
        return !previousPose.equals(currentPose);
    }

    public static boolean supportChanged(boolean previousSupported, boolean currentSupported) {
        return previousSupported != currentSupported;
    }

    public static boolean vehicleChanged(
            boolean previousPassenger,
            String previousVehicle,
            boolean currentPassenger,
            String currentVehicle) {
        if (previousPassenger != currentPassenger) {
            return true;
        }
        if (previousVehicle == null) {
            return currentVehicle != null;
        }
        return !previousVehicle.equals(currentVehicle);
    }

    public static boolean abilitiesChanged(
            boolean previousFlying,
            boolean previousMayFly,
            boolean currentFlying,
            boolean currentMayFly) {
        return previousFlying != currentFlying || previousMayFly != currentMayFly;
    }

    public static boolean footwearChanged(
            boolean previousWalksOnSnow, boolean currentWalksOnSnow) {
        return previousWalksOnSnow != currentWalksOnSnow;
    }

    public static boolean blockBelowChanged(String previousBlock, String currentBlock) {
        if (previousBlock == null) {
            return currentBlock != null;
        }
        return !previousBlock.equals(currentBlock);
    }

    public static boolean inTransition(
            boolean surfaceChanged,
            boolean fluidContactChanged,
            boolean poseChanged,
            boolean supportChanged,
            boolean vehicleChanged,
            boolean abilitiesChanged,
            boolean footwearChanged,
            boolean blockBelowChanged,
            boolean teleported,
            boolean worldChanged,
            boolean impulseLive) {
        return surfaceChanged
            || fluidContactChanged
            || poseChanged
            || supportChanged
            || vehicleChanged
            || abilitiesChanged
            || footwearChanged
            || blockBelowChanged
            || teleported
            || worldChanged
            || impulseLive;
    }
}
