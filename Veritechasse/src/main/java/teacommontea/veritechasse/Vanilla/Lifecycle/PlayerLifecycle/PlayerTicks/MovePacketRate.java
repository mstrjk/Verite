package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerTicks;

import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents.MovedTooQuickly;
import teacommontea.veritechasse.Vanilla.Reality;

public final class MovePacketRate {

    public static final String KEY = "move_packet_rate";

    public static final int EXPECTED_PACKETS_PER_TICK = 1;

    public static final int LOGGED_THRESHOLD = MovedTooQuickly.MAXIMUM_PACKETS_PER_TICK;

    private MovePacketRate() {
    }

    public static int received(int receivedMovePacketCount, int knownMovePacketCount) {
        return MovedTooQuickly.deltaPackets(receivedMovePacketCount, knownMovePacketCount);
    }

    public static boolean exceedsLoggedThreshold(int deltaPackets) {
        return MovedTooQuickly.packetRateExceeded(deltaPackets);
    }

    public static boolean vanillaRejectsOnRate() {
        return false;
    }

    public static int countedForAllowance(int deltaPackets) {
        return MovedTooQuickly.effectivePacketCount(deltaPackets);
    }

    public static double expectedPackets(long serverTicksElapsed) {
        return (double) serverTicksElapsed * (double) EXPECTED_PACKETS_PER_TICK;
    }

    public static double packetsPerTick(int packets, long serverTicksElapsed) {
        if (serverTicksElapsed <= 0L) {
            return 0.0D;
        }
        return (double) packets / (double) serverTicksElapsed;
    }

    public static final int POSITION_REMINDER_TICKS = 20;

    public static final double MOVEMENT_THRESHOLD = 2.0E-4D;

    public static double movementThresholdSquared() {
        return MOVEMENT_THRESHOLD * MOVEMENT_THRESHOLD;
    }

    public static boolean movementIsReported(double deltaX, double deltaY, double deltaZ) {
        double lengthSquared = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
        return lengthSquared > movementThresholdSquared();
    }

    public static boolean reminderIsDue(int positionReminder) {
        return positionReminder >= POSITION_REMINDER_TICKS;
    }

    public static boolean sendsPositionPacket(
            double deltaX, double deltaY, double deltaZ, int positionReminder) {
        return movementIsReported(deltaX, deltaY, deltaZ)
            || reminderIsDue(positionReminder);
    }

    public static boolean rotationIsReported(double deltaYaw, double deltaPitch) {
        return deltaYaw != 0.0D || deltaPitch != 0.0D;
    }

    public static boolean sendsStatusOnlyPacket(
            boolean groundChanged, boolean horizontalCollisionChanged) {
        return groundChanged || horizontalCollisionChanged;
    }

    public static boolean sendsAnyPacket(
            double deltaX,
            double deltaY,
            double deltaZ,
            double deltaYaw,
            double deltaPitch,
            int positionReminder,
            boolean groundChanged,
            boolean horizontalCollisionChanged) {
        if (sendsPositionPacket(deltaX, deltaY, deltaZ, positionReminder)) {
            return true;
        }
        if (rotationIsReported(deltaYaw, deltaPitch)) {
            return true;
        }
        return sendsStatusOnlyPacket(groundChanged, horizontalCollisionChanged);
    }

    public static int afterReminderTick(int positionReminder, boolean moved) {
        return moved ? 0 : positionReminder + 1;
    }

    public static double minimumPacketsPerTickWhenStationary() {
        return 1.0D / (double) POSITION_REMINDER_TICKS;
    }

    public static Reality expectedPacketsFrom(long serverTicksElapsed) {
        return Reality.of(expectedPackets(serverTicksElapsed));
    }
}
