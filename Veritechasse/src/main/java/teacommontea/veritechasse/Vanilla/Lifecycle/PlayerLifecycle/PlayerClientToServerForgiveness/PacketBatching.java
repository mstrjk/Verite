package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientToServerForgiveness;

import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness.Verdict;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents.MovedTooQuickly;

public final class PacketBatching {

    public static final String KEY = "packet_batching";

    public static final int EXPECTED_PER_TICK = 1;

    public static final int VANILLA_LOG_THRESHOLD = MovedTooQuickly.MAXIMUM_PACKETS_PER_TICK;

    public static final int IMPOSSIBLE_BURST = 100;

    private PacketBatching() {
    }

    public static boolean arrivedAtAll(int packets) {
        return packets > 0;
    }

    public static boolean batched(int packets) {
        return packets > EXPECTED_PER_TICK;
    }

    public static boolean starved(int packets) {
        return packets == 0;
    }

    public static boolean exceedsVanillaLogThreshold(int packets) {
        return MovedTooQuickly.packetRateExceeded(packets);
    }

    public static boolean impossibleBurst(int packets) {
        return packets >= IMPOSSIBLE_BURST;
    }

    public static boolean explainedByBatching(int packets, long ticksSinceLastPacket) {
        if (!batched(packets)) {
            return true;
        }
        return (long) packets <= ticksSinceLastPacket + 1L;
    }

    public static int owedPackets(long ticksSinceLastPacket) {
        return (int) Math.max(0L, ticksSinceLastPacket);
    }

    public static boolean surplusBeyondOwed(int packets, long ticksSinceLastPacket) {
        return (long) packets > ticksSinceLastPacket + 1L;
    }

    public static int surplus(int packets, long ticksSinceLastPacket) {
        long owed = ticksSinceLastPacket + 1L;
        long extra = (long) packets - owed;
        return extra <= 0L ? 0 : (int) extra;
    }

    public static final long STATIONARY_REMINDER_TICKS = 20L;

    public static boolean silenceIsLegal(long ticksSinceLastPacket) {
        return ticksSinceLastPacket <= STATIONARY_REMINDER_TICKS;
    }

    public static String route(int packets, long ticksSinceLastPacket) {
        if (impossibleBurst(packets)) {
            return Verdict.BAD_PACKETS;
        }
        if (starved(packets)) {
            return silenceIsLegal(ticksSinceLastPacket)
                ? Verdict.EVALUATE : Verdict.DISCARD;
        }
        if (surplusBeyondOwed(packets, ticksSinceLastPacket)) {
            return Verdict.BAD_PACKETS;
        }
        if (exceedsVanillaLogThreshold(packets)) {
            return Verdict.DISCARD;
        }
        return Verdict.EVALUATE;
    }

    public static boolean batchingCreatesFreeMovement() {
        return false;
    }

    public static boolean totalDistanceIsStillBounded() {
        return true;
    }
}
