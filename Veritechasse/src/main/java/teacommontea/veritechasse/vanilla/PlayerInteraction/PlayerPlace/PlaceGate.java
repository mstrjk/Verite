package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerPlace;

import teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak.BlockKnowledge;
import teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak.BlockReach;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class PlaceGate {

    public static final String KEY = "place_gate";

    public static final double REACH_BUFFER = 1.0D;

    private PlaceGate() {
    }

    public static boolean withinReach(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            double blockInteractionRange,
            boolean creative,
            Protocol protocol) {
        return BlockReach.withinRange(
            eyeX, eyeY, eyeZ, blockX, blockY, blockZ,
            blockInteractionRange, creative, protocol);
    }

    public static boolean hitLocationIsLegal(
            double hitX,
            double hitY,
            double hitZ,
            int blockX,
            int blockY,
            int blockZ) {
        return BlockFace.hitLocationIsWithinBlock(hitX, hitY, hitZ, blockX, blockY, blockZ);
    }

    public static boolean withinBuildHeight(int blockY, int minimumY, int maximumY) {
        return BlockKnowledge.withinWorldHeight(blockY, minimumY, maximumY);
    }

    public static int placementX(int blockX, String face, boolean replacingClicked) {
        return replacingClicked ? blockX : BlockFace.relativeX(blockX, face);
    }

    public static int placementY(int blockY, String face, boolean replacingClicked) {
        return replacingClicked ? blockY : BlockFace.relativeY(blockY, face);
    }

    public static int placementZ(int blockZ, String face, boolean replacingClicked) {
        return replacingClicked ? blockZ : BlockFace.relativeZ(blockZ, face);
    }

    public static boolean targetIsOccupied(String targetBlockName, boolean targetReplaceable) {
        if (BlockKnowledge.targetIsAir(targetBlockName)) {
            return false;
        }
        return !targetReplaceable;
    }

    public static boolean placingIntoExistingBlock(
            String placementBlockName,
            boolean placementReplaceable) {
        return targetIsOccupied(placementBlockName, placementReplaceable);
    }

    public static boolean serverWouldAccept(
            boolean clientLoaded,
            boolean chunkLoaded,
            boolean withinReach,
            boolean hitLocationLegal,
            boolean withinBuildHeight,
            boolean spawnProtected,
            boolean mayInteract,
            boolean awaitingTeleport,
            boolean placementReplaceable,
            boolean unobstructed,
            boolean canSurvive,
            boolean onCooldown,
            boolean itemEnabled) {
        if (!clientLoaded || !chunkLoaded || !itemEnabled) {
            return false;
        }
        if (!withinReach || !hitLocationLegal || !withinBuildHeight) {
            return false;
        }
        if (spawnProtected || !mayInteract || awaitingTeleport) {
            return false;
        }
        if (onCooldown) {
            return false;
        }
        return placementReplaceable && unobstructed && canSurvive;
    }

    public static boolean placementIsImpossible(
            boolean clientLoaded,
            boolean chunkLoaded,
            boolean withinReach,
            boolean hitLocationLegal,
            boolean withinBuildHeight,
            boolean spawnProtected,
            boolean mayInteract,
            boolean awaitingTeleport,
            boolean placementReplaceable,
            boolean unobstructed,
            boolean canSurvive,
            boolean onCooldown,
            boolean itemEnabled) {
        return !serverWouldAccept(
            clientLoaded, chunkLoaded, withinReach, hitLocationLegal, withinBuildHeight,
            spawnProtected, mayInteract, awaitingTeleport, placementReplaceable,
            unobstructed, canSurvive, onCooldown, itemEnabled);
    }

    public static boolean secondaryUseSuppressesBlockUse(
            boolean secondaryUseActive,
            boolean holdingSomething) {
        return secondaryUseActive && holdingSomething;
    }

    public static boolean consumesStack(boolean creative) {
        return !creative;
    }
}
