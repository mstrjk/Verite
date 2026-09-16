package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets;

import java.util.List;
import java.util.Locale;

import teacommontea.veritechasse.vanilla.Protocol;

public final class InteractionPackets {

    public static final String MAIN_HAND = "main_hand";
    public static final String OFF_HAND = "off_hand";

    public static final int FIRST_SEQUENCE = 0;
    public static final int NO_SEQUENCE = 0;
    public static final int UNACKNOWLEDGED = -1;

    public static final int USE_ITEM_ROTATION_MAJOR = 1;
    public static final int USE_ITEM_ROTATION_MINOR = 21;
    public static final int USE_ITEM_ROTATION_PATCH = 0;

    private static final List<String> HANDS = List.of(MAIN_HAND, OFF_HAND);

    private static final List<String> SEQUENCED = List.of(
        PacketNames.USE_ITEM_ON,
        PacketNames.USE_ITEM,
        PacketNames.PLAYER_ACTION);

    private InteractionPackets() {
    }

    public static String normalise(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    public static boolean isHand(String hand) {
        String key = normalise(hand);
        return key != null && HANDS.contains(key);
    }

    public static boolean isMainHand(String hand) {
        return MAIN_HAND.equals(normalise(hand));
    }

    public static boolean isOffHand(String hand) {
        return OFF_HAND.equals(normalise(hand));
    }

    public static List<String> hands() {
        return HANDS;
    }

    public static boolean carriesSequence(String packetName) {
        String key = normalise(packetName);
        return key != null && SEQUENCED.contains(key);
    }

    public static List<String> sequencedPackets() {
        return SEQUENCED;
    }

    public static boolean useItemCarriesRotation(Protocol protocol) {
        return protocol.atLeast(
            USE_ITEM_ROTATION_MAJOR,
            USE_ITEM_ROTATION_MINOR,
            USE_ITEM_ROTATION_PATCH);
    }

    public static boolean rotationIsClientAuthoritative(Protocol protocol) {
        return useItemCarriesRotation(protocol);
    }

    public static final boolean SERVER_REJECTS_REPLAYED_SEQUENCE = false;

    public static boolean sequenceIsWellFormed(int sequence) {
        return sequence >= FIRST_SEQUENCE;
    }

    public static boolean serverRejectsSequence(int sequence) {
        return !sequenceIsWellFormed(sequence);
    }

    public static int acknowledgedAfter(int highestAcknowledged, int sequence) {
        if (!sequenceIsWellFormed(sequence)) {
            return highestAcknowledged;
        }
        return Math.max(sequence, highestAcknowledged);
    }

    public static boolean sequenceIsMonotonic(int previousSequence, int sequence) {
        if (previousSequence == UNACKNOWLEDGED) {
            return sequence >= FIRST_SEQUENCE;
        }
        return sequence > previousSequence;
    }

    public static boolean sequenceIsAcknowledgeable(int sequence, int highestAcknowledged) {
        return sequence > highestAcknowledged;
    }

    public static boolean replaysSequence(int previousSequence, int sequence) {
        return previousSequence != UNACKNOWLEDGED && sequence <= previousSequence;
    }

    public static boolean replayIsRejectedByServer() {
        return SERVER_REJECTS_REPLAYED_SEQUENCE;
    }

    public static boolean isInteractionPacket(String packetName) {
        String key = normalise(packetName);
        if (key == null) {
            return false;
        }
        return PacketNames.USE_ITEM_ON.equals(key)
            || PacketNames.USE_ITEM.equals(key)
            || PacketNames.PLAYER_ACTION.equals(key)
            || PacketNames.SWING.equals(key)
            || PacketNames.INTERACT.equals(key);
    }

    public static boolean requiresBlockPosition(String packetName) {
        String key = normalise(packetName);
        return PacketNames.USE_ITEM_ON.equals(key)
            || PacketNames.PLAYER_ACTION.equals(key);
    }

    public static boolean requiresHand(String packetName) {
        String key = normalise(packetName);
        return PacketNames.USE_ITEM_ON.equals(key)
            || PacketNames.USE_ITEM.equals(key)
            || PacketNames.SWING.equals(key);
    }
}
