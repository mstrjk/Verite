package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientToServerForgiveness;

public final class PacketValidity {

    public static final String KEY = "packet_validity";

    public static final double HORIZONTAL_CLAMP = 3.0E7D;

    public static final double VERTICAL_CLAMP = 2.0E7D;

    private PacketValidity() {
    }

    public static boolean positionIsRejected(double x, double y, double z) {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }

    public static boolean rotationIsRejected(float yRot, float xRot) {
        return !Float.isFinite(yRot) || !Float.isFinite(xRot);
    }

    public static boolean containsInvalidValues(
            double x, double y, double z, float yRot, float xRot) {
        return positionIsRejected(x, y, z) || rotationIsRejected(yRot, xRot);
    }

    public static boolean infinityIsRejectedOnPosition() {
        return false;
    }

    public static boolean infinityIsClampedInstead(double coordinate) {
        return Double.isInfinite(coordinate);
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

    public static boolean wasClamped(double requested, double applied) {
        return requested != applied;
    }

    public static boolean requestedBeyondWorldLimit(double x, double y, double z) {
        return wasClamped(x, clampHorizontal(x))
            || wasClamped(y, clampVertical(y))
            || wasClamped(z, clampHorizontal(z));
    }

    public static boolean isCrashShapedPosition(double x, double y, double z) {
        if (positionIsRejected(x, y, z)) {
            return true;
        }
        return requestedBeyondWorldLimit(x, y, z);
    }
}
