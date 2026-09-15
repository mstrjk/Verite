package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerServerboundPackets;

import teacommontea.veritechasse.vanilla.Protocol;

public final class BlockHitPacket {

    public static final String KEY = "block_hit_result";

    public static final int WORLD_BORDER_FLAG_MAJOR = 26;
    public static final int WORLD_BORDER_FLAG_MINOR = 2;
    public static final int WORLD_BORDER_FLAG_PATCH = 0;

    public static final float RELATIVE_MINIMUM = 0.0F;
    public static final float RELATIVE_MAXIMUM = 1.0F;

    public static final double HIT_LOCATION_LIMIT = 1.0000001D;

    private BlockHitPacket() {
    }

    public static boolean carriesWorldBorderFlag(Protocol protocol) {
        return protocol.atLeast(
            WORLD_BORDER_FLAG_MAJOR,
            WORLD_BORDER_FLAG_MINOR,
            WORLD_BORDER_FLAG_PATCH);
    }

    public static double absoluteX(int blockX, float relativeX) {
        return (double) blockX + (double) relativeX;
    }

    public static double absoluteY(int blockY, float relativeY) {
        return (double) blockY + (double) relativeY;
    }

    public static double absoluteZ(int blockZ, float relativeZ) {
        return (double) blockZ + (double) relativeZ;
    }

    public static float relativeOf(double absolute, int block) {
        return (float) (absolute - (double) block);
    }

    public static boolean relativeIsWithinBlock(float relative) {
        return relative >= RELATIVE_MINIMUM && relative <= RELATIVE_MAXIMUM;
    }

    public static boolean relativesAreWithinBlock(
            float relativeX,
            float relativeY,
            float relativeZ) {
        return relativeIsWithinBlock(relativeX)
            && relativeIsWithinBlock(relativeY)
            && relativeIsWithinBlock(relativeZ);
    }

    public static boolean containsInvalidValues(
            float relativeX,
            float relativeY,
            float relativeZ) {
        return Float.isNaN(relativeX) || Float.isInfinite(relativeX)
            || Float.isNaN(relativeY) || Float.isInfinite(relativeY)
            || Float.isNaN(relativeZ) || Float.isInfinite(relativeZ);
    }

    public static boolean serverAcceptsHit(
            double hitX,
            double hitY,
            double hitZ,
            int blockX,
            int blockY,
            int blockZ) {
        double dx = hitX - ((double) blockX + 0.5D);
        double dy = hitY - ((double) blockY + 0.5D);
        double dz = hitZ - ((double) blockZ + 0.5D);
        return Math.abs(dx) < HIT_LOCATION_LIMIT
            && Math.abs(dy) < HIT_LOCATION_LIMIT
            && Math.abs(dz) < HIT_LOCATION_LIMIT;
    }

    public static boolean isWellFormed(
            float relativeX,
            float relativeY,
            float relativeZ) {
        return !containsInvalidValues(relativeX, relativeY, relativeZ);
    }
}
