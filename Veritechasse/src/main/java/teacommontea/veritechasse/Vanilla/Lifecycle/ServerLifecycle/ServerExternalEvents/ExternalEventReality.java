package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class ExternalEventReality {

    private ExternalEventReality() {
    }

    public static boolean movementIsEvaluated(
            boolean awaitingTeleportAcknowledgement,
            boolean playerRuleValue,
            boolean elytraRuleValue,
            boolean fallFlying,
            Protocol protocol) {
        if (TeleportHandshake.movementIsIgnored(awaitingTeleportAcknowledgement)) {
            return false;
        }
        return MovementCheckRules.checkApplies(
            playerRuleValue, elytraRuleValue, fallFlying, protocol);
    }

    public static boolean motionIsExplained(
            double movedDistanceSquared,
            double authorisedDeltaX,
            double authorisedDeltaY,
            double authorisedDeltaZ,
            boolean fallFlying,
            int deltaPackets) {
        double expected = MovedTooQuickly.expectedDistanceSquared(
            authorisedDeltaX, authorisedDeltaY, authorisedDeltaZ);
        return !MovedTooQuickly.exceeds(
            movedDistanceSquared, expected, fallFlying, deltaPackets);
    }

    public static Reality permittedDistanceSquared(
            double authorisedDeltaX,
            double authorisedDeltaY,
            double authorisedDeltaZ,
            boolean fallFlying,
            int deltaPackets) {
        double expected = MovedTooQuickly.expectedDistanceSquared(
            authorisedDeltaX, authorisedDeltaY, authorisedDeltaZ);
        return MovedTooQuickly.permittedDistanceSquared(
            expected, fallFlying, deltaPackets);
    }

    public static boolean momentumSurvivesTeleport(int packedRelatives, Protocol protocol) {
        return RelativeFlags.preservesMomentumX(packedRelatives, protocol)
            || RelativeFlags.preservesMomentumY(packedRelatives, protocol)
            || RelativeFlags.preservesMomentumZ(packedRelatives, protocol);
    }

    public static double authorisedDeltaX(
            double currentDelta, double deltaChange, int packedRelatives, Protocol protocol) {
        return RelativeFlags.resolveDelta(currentDelta, deltaChange,
            RelativeFlags.preservesMomentumX(packedRelatives, protocol));
    }

    public static double authorisedDeltaY(
            double currentDelta, double deltaChange, int packedRelatives, Protocol protocol) {
        return RelativeFlags.resolveDelta(currentDelta, deltaChange,
            RelativeFlags.preservesMomentumY(packedRelatives, protocol));
    }

    public static double authorisedDeltaZ(
            double currentDelta, double deltaChange, int packedRelatives, Protocol protocol) {
        return RelativeFlags.resolveDelta(currentDelta, deltaChange,
            RelativeFlags.preservesMomentumZ(packedRelatives, protocol));
    }

    public static boolean positionIsUnexplained(
            double xDist,
            double yDist,
            double zDist,
            boolean changingDimension,
            boolean sleeping,
            boolean creative,
            boolean spectator,
            int impulseGraceRemaining,
            Protocol protocol) {
        return MovedWrongly.fails(
            xDist, yDist, zDist,
            changingDimension, sleeping, creative, spectator,
            impulseGraceRemaining, protocol);
    }

    public static boolean floatingTooLong(
            int aboveGroundTickCount, double gravity, Protocol protocol) {
        return FlyingTicks.exceedsLimit(aboveGroundTickCount, gravity, protocol);
    }
}
