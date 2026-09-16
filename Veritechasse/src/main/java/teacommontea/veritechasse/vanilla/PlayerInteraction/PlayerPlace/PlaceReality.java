package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerPlace;

import teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak.BlockKnowledge;
import teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak.BlockReach;
import teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerBreak.LookGeometry;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class PlaceReality {

    public static final double TOLERANCE = 1.0E-6D;

    public static final boolean SERVER_SNAPS_TO_CLIENT_ROTATION = true;

    private PlaceReality() {
    }

    public static boolean outOfReach(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            double blockInteractionRange,
            boolean creative,
            Protocol protocol) {
        return !BlockReach.withinRange(
            eyeX, eyeY, eyeZ, blockX, blockY, blockZ,
            blockInteractionRange, creative, protocol);
    }

    public static boolean hitLocationIsImpossible(
            double hitX,
            double hitY,
            double hitZ,
            int blockX,
            int blockY,
            int blockZ) {
        return !BlockFace.hitLocationIsWithinBlock(hitX, hitY, hitZ, blockX, blockY, blockZ);
    }

    public static boolean serverValidatesFacePlane() {
        return false;
    }

    public static boolean placingIntoOccupiedSpace(
            String placementBlockName,
            boolean placementReplaceable) {
        return PlaceGate.placingIntoExistingBlock(placementBlockName, placementReplaceable);
    }

    public static boolean placedBeforeClientKnewChunk(
            boolean chunkSentToClient,
            boolean clientLoaded) {
        return BlockKnowledge.actionPrecedesKnowledge(chunkSentToClient, clientLoaded);
    }

    public static boolean facingAwayFromTarget(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            float xRot,
            float yRot) {
        return !LookGeometry.facingCouldReach(
            eyeX, eyeY, eyeZ, blockX, blockY, blockZ, xRot, yRot);
    }

    public static boolean facingIsServerProven() {
        return LookGeometry.serverValidatesFacing();
    }

    public static boolean rotationIsClientAuthoritative() {
        return SERVER_SNAPS_TO_CLIENT_ROTATION;
    }

    public static int placementX(int blockX, String face, boolean replacingClicked) {
        return PlaceGate.placementX(blockX, face, replacingClicked);
    }

    public static int placementY(int blockY, String face, boolean replacingClicked) {
        return PlaceGate.placementY(blockY, face, replacingClicked);
    }

    public static int placementZ(int blockZ, String face, boolean replacingClicked) {
        return PlaceGate.placementZ(blockZ, face, replacingClicked);
    }

    public static boolean sequenceIsOutOfOrder(int previousSequence, int sequence) {
        return !BlockKnowledge.sequenceIsMonotonic(previousSequence, sequence);
    }

    public static Reality reachFrom(
            double blockInteractionRange,
            boolean creative,
            Protocol protocol) {
        return BlockReach.rangeFrom(blockInteractionRange, creative, protocol);
    }

    public static Reality hitLocationFrom() {
        return Reality.of(BlockFace.HIT_LOCATION_LIMIT);
    }
}
