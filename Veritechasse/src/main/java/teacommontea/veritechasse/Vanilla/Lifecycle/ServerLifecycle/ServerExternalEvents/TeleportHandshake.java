package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents;

public final class TeleportHandshake {

    public static final String KEY = "teleport_handshake";

    public static final int RESEND_AFTER_TICKS = 20;

    public static final int FIRST_TELEPORT_ID = 1;

    private TeleportHandshake() {
    }

    public static int nextTeleportId(int awaitingTeleport) {
        int next = awaitingTeleport + 1;
        return next == Integer.MAX_VALUE ? 0 : next;
    }

    public static boolean awaitingAcknowledgement(boolean hasAwaitingPosition) {
        return hasAwaitingPosition;
    }

    public static boolean movementIsIgnored(boolean hasAwaitingPosition) {
        return hasAwaitingPosition;
    }

    public static boolean acknowledgementMatches(int packetId, int awaitingTeleport) {
        return packetId == awaitingTeleport;
    }

    public static boolean acknowledgementIsFatal(int packetId, int awaitingTeleport, boolean hasAwaitingPosition) {
        return acknowledgementMatches(packetId, awaitingTeleport) && !hasAwaitingPosition;
    }

    public static boolean acknowledgementAccepted(
            int packetId, int awaitingTeleport, boolean hasAwaitingPosition) {
        return acknowledgementMatches(packetId, awaitingTeleport) && hasAwaitingPosition;
    }

    public static boolean staleAcknowledgement(int packetId, int awaitingTeleport) {
        return !acknowledgementMatches(packetId, awaitingTeleport);
    }

    public static int ticksSinceSent(int tickCount, int awaitingTeleportTime) {
        return tickCount - awaitingTeleportTime;
    }

    public static boolean resendDue(int tickCount, int awaitingTeleportTime, boolean hasAwaitingPosition) {
        if (!hasAwaitingPosition) {
            return false;
        }
        return ticksSinceSent(tickCount, awaitingTeleportTime) > RESEND_AFTER_TICKS;
    }

    public static boolean positionIsAuthorised(boolean hasAwaitingPosition) {
        return !hasAwaitingPosition;
    }

    public static boolean baselineResetsOnAcknowledgement() {
        return true;
    }
}
