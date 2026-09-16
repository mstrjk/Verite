package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets;

import java.util.Locale;

import teacommontea.veritechasse.vanilla.PlayerMovement.Support.GroundState;
import teacommontea.veritechasse.vanilla.Protocol;

public final class MovePlayerPacket {

    public static final String POS = "pos";
    public static final String POS_ROT = "pos_rot";
    public static final String ROT = "rot";
    public static final String STATUS_ONLY = "status_only";

    public static final int HORIZONTAL_COLLISION_PROTOCOL_MAJOR = 1;
    public static final int HORIZONTAL_COLLISION_PROTOCOL_MINOR = 21;
    public static final int HORIZONTAL_COLLISION_PROTOCOL_PATCH = 2;

    public static final double HORIZONTAL_CLAMP = 3.0E7D;
    public static final double VERTICAL_CLAMP = 2.0E7D;

    public static final float VANILLA_SQUARED_SPEED_LIMIT = 100.0F;
    public static final float VANILLA_SQUARED_SPEED_LIMIT_GLIDING = 300.0F;

    public static final int VANILLA_PACKETS_PER_TICK_WARNING = 5;

    private MovePlayerPacket() {
    }

    public static String normalise(String variant) {
        return variant == null ? null : variant.toLowerCase(Locale.ROOT);
    }

    public static boolean isPos(String variant) {
        return POS.equals(normalise(variant));
    }

    public static boolean isPosRot(String variant) {
        return POS_ROT.equals(normalise(variant));
    }

    public static boolean isRot(String variant) {
        return ROT.equals(normalise(variant));
    }

    public static boolean isStatusOnly(String variant) {
        return STATUS_ONLY.equals(normalise(variant));
    }

    public static boolean hasPosition(String variant) {
        String key = normalise(variant);
        return POS.equals(key) || POS_ROT.equals(key);
    }

    public static boolean hasRotation(String variant) {
        String key = normalise(variant);
        return ROT.equals(key) || POS_ROT.equals(key);
    }

    public static boolean isStatusUpdateOnly(String variant) {
        return isStatusOnly(variant);
    }

    public static boolean hasHorizontalCollision(Protocol protocol) {
        return protocol.atLeast(
            HORIZONTAL_COLLISION_PROTOCOL_MAJOR,
            HORIZONTAL_COLLISION_PROTOCOL_MINOR,
            HORIZONTAL_COLLISION_PROTOCOL_PATCH);
    }

    public static boolean claimsOnGround(boolean onGroundFlag) {
        return onGroundFlag;
    }

    public static boolean groundClaimIsTrustworthy() {
        return GroundState.CLIENT_CLAIM_IS_AUTHORITATIVE;
    }

    public static boolean isOnGround(double attemptedY, double resolvedY) {
        return GroundState.onGround(attemptedY, resolvedY);
    }

    public static boolean groundClaimIsFalsified(
            boolean onGroundFlag,
            double attemptedY,
            double resolvedY) {
        return GroundState.claimIsFalsified(onGroundFlag, attemptedY, resolvedY);
    }

    public static boolean containsInvalidValues(double x, double y, double z, float yaw, float pitch) {
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
            return true;
        }
        if (Float.isNaN(pitch) || Float.isInfinite(pitch)) {
            return true;
        }
        return Float.isNaN(yaw) || Float.isInfinite(yaw);
    }

    public static boolean isWellFormed(double x, double y, double z, float yaw, float pitch) {
        return !containsInvalidValues(x, y, z, yaw, pitch);
    }

    public static double clampHorizontal(double value) {
        if (value < -HORIZONTAL_CLAMP) {
            return -HORIZONTAL_CLAMP;
        }
        return value > HORIZONTAL_CLAMP ? HORIZONTAL_CLAMP : value;
    }

    public static double clampVertical(double value) {
        if (value < -VERTICAL_CLAMP) {
            return -VERTICAL_CLAMP;
        }
        return value > VERTICAL_CLAMP ? VERTICAL_CLAMP : value;
    }

    public static boolean wasClamped(double x, double y, double z) {
        return clampHorizontal(x) != x || clampVertical(y) != y || clampHorizontal(z) != z;
    }

    public static float vanillaSquaredSpeedLimit(boolean gliding) {
        return gliding ? VANILLA_SQUARED_SPEED_LIMIT_GLIDING : VANILLA_SQUARED_SPEED_LIMIT;
    }

    public static boolean exceedsVanillaSpeedLimit(
            double squaredDistanceMoved,
            double squaredDistanceExpected,
            boolean gliding,
            int packetsSinceLastTick) {
        int packets = packetsSinceLastTick < 1 ? 1 : packetsSinceLastTick;
        double allowance = (double) vanillaSquaredSpeedLimit(gliding) * (double) packets;
        return squaredDistanceMoved - squaredDistanceExpected > allowance;
    }

    public static boolean sendingTooFrequently(int packetsSinceLastTick) {
        return packetsSinceLastTick > VANILLA_PACKETS_PER_TICK_WARNING;
    }

    public static final double MOVED_WRONGLY_THRESHOLD = 0.0625D;
    public static final double SLEEPING_DISPLACEMENT_THRESHOLD = 1.0D;
    public static final int MAXIMUM_FLYING_TICKS = 80;
    public static final double FLOATING_Y_THRESHOLD = -0.03125D;

    public static boolean movedWrongly(
            double squaredResidual,
            boolean changingDimension,
            boolean sleeping,
            boolean creative,
            boolean spectator,
            boolean inPostImpulseGraceTime) {
        if (changingDimension || sleeping || creative || spectator) {
            return false;
        }
        if (inPostImpulseGraceTime) {
            return false;
        }
        return squaredResidual > MOVED_WRONGLY_THRESHOLD;
    }

    public static boolean sleepingMovedTooFar(double squaredResidual, boolean sleeping) {
        return sleeping && squaredResidual > SLEEPING_DISPLACEMENT_THRESHOLD;
    }

    public static boolean speedCheckApplies(
            boolean singleplayerOwner,
            boolean changingDimension,
            boolean playerMovementCheckRule,
            boolean elytraMovementCheckRule,
            boolean fallFlying) {
        if (singleplayerOwner || changingDimension) {
            return false;
        }
        if (!playerMovementCheckRule) {
            return false;
        }
        return !fallFlying || elytraMovementCheckRule;
    }

    public static boolean exceedsVanillaSpeedLimit(
            double squaredDistanceMoved,
            double squaredDistanceExpected,
            boolean gliding,
            int packetsSinceLastTick,
            boolean singleplayerOwner,
            boolean changingDimension,
            boolean playerMovementCheckRule,
            boolean elytraMovementCheckRule) {
        if (!speedCheckApplies(singleplayerOwner, changingDimension,
                playerMovementCheckRule, elytraMovementCheckRule, gliding)) {
            return false;
        }
        return exceedsVanillaSpeedLimit(
            squaredDistanceMoved, squaredDistanceExpected, gliding, packetsSinceLastTick);
    }

    public static boolean floatingTooLong(int aboveGroundTickCount) {
        return aboveGroundTickCount > MAXIMUM_FLYING_TICKS;
    }

    public static boolean countsAsFloating(
            double verticalDistance,
            boolean standsOnSomething,
            boolean spectator,
            boolean allowFlight,
            boolean mayFly,
            boolean levitating,
            boolean fallFlying,
            boolean autoSpinAttack) {
        if (standsOnSomething || spectator || allowFlight || mayFly) {
            return false;
        }
        if (levitating || fallFlying || autoSpinAttack) {
            return false;
        }
        return verticalDistance >= FLOATING_Y_THRESHOLD;
    }
}
