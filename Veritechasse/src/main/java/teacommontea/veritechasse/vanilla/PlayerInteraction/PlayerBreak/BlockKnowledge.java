package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak;

import teacommontea.veritechasse.vanilla.Protocol;

public final class BlockKnowledge {

    public static final String KEY = "block_knowledge";

    public static final int UNACKNOWLEDGED = -1;

    public static final int SEQUENCE_MAJOR = 1;
    public static final int SEQUENCE_MINOR = 19;
    public static final int SEQUENCE_PATCH = 0;

    private BlockKnowledge() {
    }

    public static boolean sequencedPredictionExists(Protocol protocol) {
        return protocol.atLeast(SEQUENCE_MAJOR, SEQUENCE_MINOR, SEQUENCE_PATCH);
    }

    public static boolean serverKnowsBlock(boolean chunkLoaded, boolean withinWorldHeight) {
        return chunkLoaded && withinWorldHeight;
    }

    public static boolean clientCouldKnowBlock(boolean chunkSentToClient, boolean clientLoaded) {
        return chunkSentToClient && clientLoaded;
    }

    public static boolean bothKnowBlock(
            boolean chunkLoaded,
            boolean withinWorldHeight,
            boolean chunkSentToClient,
            boolean clientLoaded) {
        return serverKnowsBlock(chunkLoaded, withinWorldHeight)
            && clientCouldKnowBlock(chunkSentToClient, clientLoaded);
    }

    public static boolean actionPrecedesKnowledge(
            boolean chunkSentToClient,
            boolean clientLoaded) {
        return !clientCouldKnowBlock(chunkSentToClient, clientLoaded);
    }

    public static boolean sequenceIsMonotonic(int previousSequence, int sequence) {
        if (previousSequence == UNACKNOWLEDGED) {
            return sequence >= 0;
        }
        return sequence > previousSequence;
    }

    public static boolean sequenceIsAcknowledgeable(int sequence, int highestAcknowledged) {
        return sequence > highestAcknowledged;
    }

    public static boolean predictionWasRolledBack(
            String clientPredictedBlock,
            String serverAcknowledgedBlock) {
        if (clientPredictedBlock == null || serverAcknowledgedBlock == null) {
            return false;
        }
        return !clientPredictedBlock.equals(serverAcknowledgedBlock);
    }

    public static boolean targetIsAir(String blockName) {
        return blockName == null || "air".equals(blockName);
    }

    public static boolean breakingAlreadyBrokenBlock(String serverBlockName) {
        return targetIsAir(serverBlockName);
    }

    public static boolean withinWorldHeight(int blockY, int minimumY, int maximumY) {
        return blockY >= minimumY && blockY <= maximumY;
    }

    public static boolean aboveBuildLimit(int blockY, int maximumY) {
        return blockY > maximumY;
    }

    public static boolean belowBuildLimit(int blockY, int minimumY) {
        return blockY < minimumY;
    }
}
