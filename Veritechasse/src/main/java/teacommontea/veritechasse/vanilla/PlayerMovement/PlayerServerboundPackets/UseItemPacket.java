package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class UseItemPacket {

    public static final String KEY = "use_item";

    public static final int COMPLETION_REMAINING = 0;

    public static final boolean SERVER_COUNTS_DOWN = true;

    private UseItemPacket() {
    }

    public static boolean carriesRotation(Protocol protocol) {
        return InteractionPackets.useItemCarriesRotation(protocol);
    }

    public static boolean serverCountsDown() {
        return SERVER_COUNTS_DOWN;
    }

    public static int remainingAfterTick(int remaining) {
        return remaining <= 0 ? 0 : remaining - 1;
    }

    public static int elapsedTicks(int useDuration, int remaining) {
        int elapsed = useDuration - remaining;
        return elapsed < 0 ? 0 : elapsed;
    }

    public static boolean completesOnTick(int remainingBeforeTick, boolean useOnRelease) {
        if (useOnRelease) {
            return false;
        }
        return remainingBeforeTick - 1 == COMPLETION_REMAINING;
    }

    public static boolean completionIsServerDriven(boolean useOnRelease) {
        return !useOnRelease;
    }

    public static boolean releaseArrivedTooEarly(
            int useDuration,
            int observedElapsedTicks,
            int minimumTicks) {
        if (observedElapsedTicks < 0) {
            return true;
        }
        if (useDuration <= 0) {
            return false;
        }
        return observedElapsedTicks < minimumTicks;
    }

    public static boolean rotationPrecedesUse(Protocol protocol) {
        return carriesRotation(protocol);
    }
}
