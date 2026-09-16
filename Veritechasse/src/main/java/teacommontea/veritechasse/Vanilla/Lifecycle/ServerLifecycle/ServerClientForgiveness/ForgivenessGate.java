package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness;

import teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientLatencyForgiveness.ClientHealth;
import teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientLatencyForgiveness.PingProbe;
import teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientToServerForgiveness.PacketBatching;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class ForgivenessGate {

    public static final String KEY = "forgiveness_gate";

    private ForgivenessGate() {
    }

    public static String route(
            boolean sprinting,
            boolean frozen,
            long behindTimeNanos,
            long nanosecondsPerTick,
            int emptyTicks,
            int pauseWhenEmptySeconds,
            double worstStallMillis,
            float tickrate,
            int packetsThisWindow,
            long ticksSinceLastPacket,
            boolean keepAlivePending,
            long ticksSincePending,
            long millisSinceLastPingResponse,
            Protocol protocol) {
        String transport = PacketBatching.route(packetsThisWindow, ticksSinceLastPacket);
        if (Verdict.isBadPackets(transport)) {
            return Verdict.BAD_PACKETS;
        }

        String server = ServerHealth.route(sprinting, frozen, behindTimeNanos,
            nanosecondsPerTick, emptyTicks, pauseWhenEmptySeconds,
            worstStallMillis, tickrate, protocol);
        if (Verdict.isDiscard(server)) {
            return Verdict.DISCARD;
        }

        if (PingProbe.movementSilenceWhileAnsweringPings(
                keepAlivePending, millisSinceLastPingResponse, ticksSinceLastPacket)) {
            return Verdict.BAD_PACKETS;
        }

        String client = ClientHealth.route(
            ticksSinceLastPacket, keepAlivePending, ticksSincePending);
        return Verdict.worst(transport, client);
    }

    public static boolean badPacketsOutranksDiscard() {
        return true;
    }

    public static boolean serverDegradationIsCheckedBeforeClient() {
        return true;
    }

    public static boolean discardRequiresServerSideEvidence(String verdict) {
        return Verdict.isDiscard(verdict);
    }

    public static boolean failsClosed() {
        return true;
    }

    public static boolean unknownStateEvaluates(
            boolean anyDegradationProven) {
        return !anyDegradationProven;
    }

    public static boolean baselineResets(String verdict) {
        return !Verdict.baselineSurvives(verdict);
    }
}
