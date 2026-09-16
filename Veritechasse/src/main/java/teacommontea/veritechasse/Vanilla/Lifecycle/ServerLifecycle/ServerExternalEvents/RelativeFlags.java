package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class RelativeFlags {

    public static final String KEY = "relative_flags";

    public static final int DELTA_FLAGS_MAJOR = 1;
    public static final int DELTA_FLAGS_MINOR = 21;
    public static final int DELTA_FLAGS_PATCH = 2;

    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int Y_ROT = 3;
    public static final int X_ROT = 4;
    public static final int DELTA_X = 5;
    public static final int DELTA_Y = 6;
    public static final int DELTA_Z = 7;
    public static final int ROTATE_DELTA = 8;

    public static final int LEGACY_FLAG_COUNT = 5;
    public static final int MODERN_FLAG_COUNT = 9;

    public static final float PITCH_MINIMUM = -90.0F;
    public static final float PITCH_MAXIMUM = 90.0F;

    private RelativeFlags() {
    }

    public static boolean hasDeltaFlags(Protocol protocol) {
        return protocol.atLeast(
            DELTA_FLAGS_MAJOR, DELTA_FLAGS_MINOR, DELTA_FLAGS_PATCH);
    }

    public static int flagCount(Protocol protocol) {
        return hasDeltaFlags(protocol) ? MODERN_FLAG_COUNT : LEGACY_FLAG_COUNT;
    }

    public static int mask(int flag) {
        return 1 << flag;
    }

    public static boolean isSet(int packed, int flag) {
        return (packed & mask(flag)) == mask(flag);
    }

    public static boolean preservesMomentumOnAxis(int packed, int positionFlag, int deltaFlag, Protocol protocol) {
        if (hasDeltaFlags(protocol)) {
            return isSet(packed, deltaFlag);
        }
        return isSet(packed, positionFlag);
    }

    public static boolean preservesMomentumX(int packed, Protocol protocol) {
        return preservesMomentumOnAxis(packed, X, DELTA_X, protocol);
    }

    public static boolean preservesMomentumY(int packed, Protocol protocol) {
        return preservesMomentumOnAxis(packed, Y, DELTA_Y, protocol);
    }

    public static boolean preservesMomentumZ(int packed, Protocol protocol) {
        return preservesMomentumOnAxis(packed, Z, DELTA_Z, protocol);
    }

    public static boolean rotatesMomentum(int packed, Protocol protocol) {
        return hasDeltaFlags(protocol) && isSet(packed, ROTATE_DELTA);
    }

    public static double resolveDelta(double currentDelta, double deltaChange, boolean preserves) {
        return preserves ? currentDelta + deltaChange : deltaChange;
    }

    public static double resolvePosition(double currentPosition, double positionChange, boolean relative) {
        return relative ? currentPosition + positionChange : positionChange;
    }

    public static float clampPitch(float pitch) {
        if (pitch < PITCH_MINIMUM) {
            return PITCH_MINIMUM;
        }
        return pitch > PITCH_MAXIMUM ? PITCH_MAXIMUM : pitch;
    }

    public static boolean absoluteTeleportZeroesMomentum(Protocol protocol) {
        return !hasDeltaFlags(protocol);
    }
}
