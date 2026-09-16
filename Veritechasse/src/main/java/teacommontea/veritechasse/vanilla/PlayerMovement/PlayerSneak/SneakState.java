package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerSneak;

import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets.SneakSource;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class SneakState {

    public static final boolean FLYING_SUPPRESSES_CROUCH = true;

    private SneakState() {
    }

    public static boolean shiftKeyDown(
            Protocol protocol,
            String lastCommandAction,
            boolean clientInputShift) {
        return SneakSource.sneaking(protocol, lastCommandAction, clientInputShift);
    }

    public static boolean desiresCrouch(boolean shiftKeyDown, boolean flying) {
        if (flying && FLYING_SUPPRESSES_CROUCH) {
            return false;
        }
        return shiftKeyDown;
    }

    public static boolean staysOnGroundSurface(boolean shiftKeyDown) {
        return shiftKeyDown;
    }

    public static boolean ledgeProtectionApplies(
            boolean shiftKeyDown,
            boolean flying,
            double deltaY,
            boolean selfMovement) {
        if (flying || !selfMovement) {
            return false;
        }
        if (deltaY > 0.0D) {
            return false;
        }
        return staysOnGroundSurface(shiftKeyDown);
    }

    public static boolean isVisuallyCrawling(boolean visuallySwimming, boolean inWater) {
        return visuallySwimming && !inWater;
    }

    public static boolean isMovingSlowly(boolean crouchingPose, boolean visuallyCrawling) {
        return crouchingPose || visuallyCrawling;
    }

    public static boolean speedPenaltyApplies(boolean crouchingPose, boolean visuallyCrawling) {
        return isMovingSlowly(crouchingPose, visuallyCrawling);
    }

    public static boolean penaltyWithoutShiftKey(boolean crouchingPose, boolean shiftKeyDown) {
        return crouchingPose && !shiftKeyDown;
    }
}
