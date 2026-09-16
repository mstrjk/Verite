package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents;

import teacommontea.veritechasse.Vanilla.Reality;

public final class MovedTooQuickly {

    public static final String KEY = "moved_too_quickly";

    public static final float WALKING_ALLOWANCE = 100.0F;

    public static final float FALL_FLYING_ALLOWANCE = 300.0F;

    public static final int MAXIMUM_PACKETS_PER_TICK = 5;

    public static final int CLAMPED_PACKET_COUNT = 1;

    private MovedTooQuickly() {
    }

    public static float allowancePerPacket(boolean fallFlying) {
        return fallFlying ? FALL_FLYING_ALLOWANCE : WALKING_ALLOWANCE;
    }

    public static int deltaPackets(int receivedMovePacketCount, int knownMovePacketCount) {
        return receivedMovePacketCount - knownMovePacketCount;
    }

    public static boolean packetRateExceeded(int deltaPackets) {
        return deltaPackets > MAXIMUM_PACKETS_PER_TICK;
    }

    public static int effectivePacketCount(int deltaPackets) {
        return packetRateExceeded(deltaPackets) ? CLAMPED_PACKET_COUNT : deltaPackets;
    }

    public static double expectedDistanceSquared(double deltaX, double deltaY, double deltaZ) {
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public static double movedDistanceSquared(double xDist, double yDist, double zDist) {
        return xDist * xDist + yDist * yDist + zDist * zDist;
    }

    public static double allowance(boolean fallFlying, int deltaPackets) {
        return (double) (allowancePerPacket(fallFlying)
            * (float) effectivePacketCount(deltaPackets));
    }

    public static boolean exceeds(
            double movedDistanceSquared,
            double expectedDistanceSquared,
            boolean fallFlying,
            int deltaPackets) {
        return movedDistanceSquared - expectedDistanceSquared
            > allowance(fallFlying, deltaPackets);
    }

    public static boolean checkApplies(
            boolean movementCheckGameRule,
            boolean elytraMovementCheckGameRule,
            boolean fallFlying) {
        if (!movementCheckGameRule) {
            return false;
        }
        return !fallFlying || elytraMovementCheckGameRule;
    }

    public static Reality permittedDistanceSquared(
            double expectedDistanceSquared,
            boolean fallFlying,
            int deltaPackets) {
        return Reality.of(expectedDistanceSquared + allowance(fallFlying, deltaPackets));
    }
}
