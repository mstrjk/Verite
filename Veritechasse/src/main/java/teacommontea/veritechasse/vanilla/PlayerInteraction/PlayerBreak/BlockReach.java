package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerBreak;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class BlockReach {

    public static final String KEY = "block_reach";

    public static final double LEGACY_MAX_INTERACTION_DISTANCE = 6.0D;

    public static final double DEFAULT_BLOCK_INTERACTION_RANGE = 4.5D;
    public static final double INTERACTION_BUFFER = 1.0D;
    public static final double CREATIVE_RANGE_BONUS = 0.5D;

    public static final double ATTRIBUTE_MINIMUM = 0.0D;
    public static final double ATTRIBUTE_MAXIMUM = 64.0D;

    public static final int ATTRIBUTE_MAJOR = 1;
    public static final int ATTRIBUTE_MINOR = 20;
    public static final int ATTRIBUTE_PATCH = 5;

    private BlockReach() {
    }

    public static boolean isAttributeDriven(Protocol protocol) {
        return protocol.atLeast(ATTRIBUTE_MAJOR, ATTRIBUTE_MINOR, ATTRIBUTE_PATCH);
    }

    public static boolean measuresToBoundingBox(Protocol protocol) {
        return isAttributeDriven(protocol);
    }

    public static double clampAttribute(double attributeValue) {
        if (attributeValue < ATTRIBUTE_MINIMUM) {
            return ATTRIBUTE_MINIMUM;
        }
        return attributeValue > ATTRIBUTE_MAXIMUM ? ATTRIBUTE_MAXIMUM : attributeValue;
    }

    public static double interactionRange(boolean creative, Protocol protocol) {
        return interactionRange(DEFAULT_BLOCK_INTERACTION_RANGE, creative, protocol);
    }

    public static double interactionRange(
            double attributeValue,
            boolean creative,
            Protocol protocol) {
        if (!isAttributeDriven(protocol)) {
            return LEGACY_MAX_INTERACTION_DISTANCE;
        }
        double range = clampAttribute(attributeValue);
        if (creative) {
            range = range + CREATIVE_RANGE_BONUS;
        }
        return range + INTERACTION_BUFFER;
    }

    public static double maximumDistanceSquared(
            double attributeValue,
            boolean creative,
            Protocol protocol) {
        double range = interactionRange(attributeValue, creative, protocol);
        return range * range;
    }

    public static double distanceSquaredToBlockCentre(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ) {
        double dx = eyeX - ((double) blockX + 0.5D);
        double dy = eyeY - ((double) blockY + 0.5D);
        double dz = eyeZ - ((double) blockZ + 0.5D);
        return dx * dx + dy * dy + dz * dz;
    }

    public static double axisGap(double eye, double minimum, double maximum) {
        if (eye < minimum) {
            return minimum - eye;
        }
        return eye > maximum ? eye - maximum : 0.0D;
    }

    public static double distanceSquaredToBlockBox(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ) {
        double dx = axisGap(eyeX, (double) blockX, (double) blockX + 1.0D);
        double dy = axisGap(eyeY, (double) blockY, (double) blockY + 1.0D);
        double dz = axisGap(eyeZ, (double) blockZ, (double) blockZ + 1.0D);
        return dx * dx + dy * dy + dz * dz;
    }

    public static double distanceSquared(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            Protocol protocol) {
        if (measuresToBoundingBox(protocol)) {
            return distanceSquaredToBlockBox(eyeX, eyeY, eyeZ, blockX, blockY, blockZ);
        }
        return distanceSquaredToBlockCentre(eyeX, eyeY, eyeZ, blockX, blockY, blockZ);
    }

    public static boolean withinRange(
            double eyeX,
            double eyeY,
            double eyeZ,
            int blockX,
            int blockY,
            int blockZ,
            double attributeValue,
            boolean creative,
            Protocol protocol) {
        double observed = distanceSquared(eyeX, eyeY, eyeZ, blockX, blockY, blockZ, protocol);
        double limit = maximumDistanceSquared(attributeValue, creative, protocol);
        if (measuresToBoundingBox(protocol)) {
            return observed < limit;
        }
        return observed <= limit;
    }

    public static Reality rangeFrom(double attributeValue, boolean creative, Protocol protocol) {
        return Reality.of(interactionRange(attributeValue, creative, protocol));
    }
}
