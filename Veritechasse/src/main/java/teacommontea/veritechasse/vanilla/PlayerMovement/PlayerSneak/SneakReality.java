package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerSneak;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets.SneakSource;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerXZ.GroundSpeed;
import teacommontea.veritechasse.Vanilla.PlayerMovement.Support.MoveInput;
import teacommontea.veritechasse.Vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class SneakReality {

    public static final double INPUT_TOLERANCE = MoveInput.LENGTH_TOLERANCE;

    private SneakReality() {
    }

    public static double maximumInputLength(
            boolean crouchingPose,
            boolean visuallyCrawling,
            ItemStack leggings,
            Era era) {
        double length = MoveInput.LEGAL_LENGTH_CARDINAL;
        if (!SneakState.isMovingSlowly(crouchingPose, visuallyCrawling)) {
            return length;
        }
        return SneakSpeed.apply(length, SneakSpeed.factorFor(leggings, era));
    }

    public static double maximumTerminalSpeed(
            ActiveEffects effects,
            String blockBelow,
            String blockHere,
            boolean crouchingPose,
            boolean visuallyCrawling,
            ItemStack leggings,
            Era era) {
        double unsneaked = GroundSpeed.terminalSpeed(effects, false, blockBelow, blockHere);
        if (!SneakState.isMovingSlowly(crouchingPose, visuallyCrawling)) {
            return unsneaked;
        }
        return SneakSpeed.apply(unsneaked, SneakSpeed.factorFor(leggings, era));
    }

    public static boolean permitsSpeed(
            double observedHorizontal,
            ActiveEffects effects,
            String blockBelow,
            String blockHere,
            boolean crouchingPose,
            boolean visuallyCrawling,
            ItemStack leggings,
            Era era) {
        double bound = maximumTerminalSpeed(
            effects, blockBelow, blockHere, crouchingPose, visuallyCrawling, leggings, era);
        return observedHorizontal <= bound + INPUT_TOLERANCE;
    }

    public static boolean poseContradictsShiftKey(String resolvedPose, boolean shiftKeyDown) {
        if (SneakPose.outranksShiftKey(resolvedPose)) {
            return false;
        }
        if (SneakPose.forcedIntoCrouch(resolvedPose, shiftKeyDown)) {
            return false;
        }
        return shiftKeyDown && !SneakPose.isCrouching(resolvedPose);
    }

    public static boolean poseContradictsShiftKey(
            String resolvedPose, boolean shiftKeyDown, boolean flying) {
        if (flying) {
            return false;
        }
        return poseContradictsShiftKey(resolvedPose, shiftKeyDown);
    }

    public static boolean poseIsImpossible(
            String claimedPose,
            boolean fitsStanding,
            boolean fitsCrouching) {
        return !SneakPose.poseIsPossible(claimedPose, fitsStanding, fitsCrouching);
    }

    public static boolean sprintingWhileSneaking(boolean sprinting, boolean crouchingPose) {
        return sprinting && crouchingPose;
    }

    public static boolean ledgeProtectionExpected(
            boolean shiftKeyDown,
            boolean flying,
            double deltaY,
            boolean selfMovement) {
        return SneakState.ledgeProtectionApplies(shiftKeyDown, flying, deltaY, selfMovement);
    }

    public static boolean sneakStateIsKnown(Protocol protocol) {
        return SneakSource.isKnown(protocol);
    }

    public static Reality speedFrom(
            ActiveEffects effects,
            String blockBelow,
            String blockHere,
            boolean crouchingPose,
            boolean visuallyCrawling,
            ItemStack leggings,
            Era era) {
        return Reality.of(maximumTerminalSpeed(
            effects, blockBelow, blockHere, crouchingPose, visuallyCrawling, leggings, era));
    }
}
