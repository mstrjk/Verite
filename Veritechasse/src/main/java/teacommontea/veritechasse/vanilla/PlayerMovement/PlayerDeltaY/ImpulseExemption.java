package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class ImpulseExemption {

    public static final String KEY = "impulse_exemption";

    public static final int PROTOCOL_MAJOR = 1;
    public static final int PROTOCOL_MINOR = 21;
    public static final int PROTOCOL_PATCH = 0;

    public static final int MOVEMENT_GATE_MAJOR = 1;
    public static final int MOVEMENT_GATE_MINOR = 21;
    public static final int MOVEMENT_GATE_PATCH = 11;

    public static final int GRACE_TICKS = 40;

    public static final String WIND_CHARGE = "wind_charge";
    public static final String MACE = "mace";

    public static final double MACE_LAUNCH_DELTA_Y = 0.01D;

    private ImpulseExemption() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(PROTOCOL_MAJOR, PROTOCOL_MINOR, PROTOCOL_PATCH);
    }

    public static boolean exemptsMovementGate(Protocol protocol) {
        return protocol.atLeast(
            MOVEMENT_GATE_MAJOR, MOVEMENT_GATE_MINOR, MOVEMENT_GATE_PATCH);
    }

    public static boolean movementGateForgiven(int graceRemaining, Protocol protocol) {
        return exemptsMovementGate(protocol) && inGracePeriod(graceRemaining);
    }

    public static boolean grantedByExplosion(String explosionSource, Protocol protocol) {
        return exists(protocol) && WIND_CHARGE.equals(explosionSource);
    }

    public static boolean grantedByMaceSmash(boolean smashAttack, Protocol protocol) {
        return exists(protocol) && smashAttack;
    }

    public static int graceTicksAfterGrant() {
        return GRACE_TICKS;
    }

    public static int graceAfterTick(int graceRemaining) {
        return graceRemaining <= 0 ? 0 : graceRemaining - 1;
    }

    public static boolean inGracePeriod(int graceRemaining) {
        return graceRemaining > 0;
    }

    public static boolean contextResets(int graceRemaining) {
        return graceRemaining == 0;
    }

    public static double effectiveFallDistance(
            double fallDistance,
            boolean ignoring,
            double impactPosY,
            double landingY) {
        if (!ignoring) {
            return fallDistance;
        }
        double capped = impactPosY - landingY;
        return Math.min(fallDistance, capped);
    }

    public static boolean landedAboveImpactPos(
            double fallDistance,
            boolean ignoring,
            double impactPosY,
            double landingY) {
        if (!ignoring) {
            return false;
        }
        return effectiveFallDistance(fallDistance, ignoring, impactPosY, landingY) <= 0.0D;
    }

    public static boolean exemptsFallDamage(
            double fallDistance,
            boolean ignoring,
            double impactPosY,
            double landingY,
            double safeFallDistance) {
        double effective = effectiveFallDistance(
            fallDistance, ignoring, impactPosY, landingY);
        return effective <= safeFallDistance;
    }

    public static Reality fallDistanceFrom(
            double fallDistance,
            boolean ignoring,
            double impactPosY,
            double landingY) {
        return Reality.of(effectiveFallDistance(
            fallDistance, ignoring, impactPosY, landingY));
    }
}
