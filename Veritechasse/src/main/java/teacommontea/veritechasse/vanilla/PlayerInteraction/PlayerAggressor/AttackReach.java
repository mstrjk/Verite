package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerAggressor;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class AttackReach {

    public static final String KEY = "attack_reach";

    public static final double LEGACY_MAX_INTERACTION_DISTANCE = 6.0D;

    public static final double DEFAULT_ENTITY_INTERACTION_RANGE = 3.0D;
    public static final double INTERACTION_BUFFER = 3.0D;
    public static final double CREATIVE_RANGE_BONUS = 2.0D;

    public static final double ATTRIBUTE_MINIMUM = 0.0D;
    public static final double ATTRIBUTE_MAXIMUM = 64.0D;

    public static final int ATTRIBUTE_MAJOR = 1;
    public static final int ATTRIBUTE_MINOR = 20;
    public static final int ATTRIBUTE_PATCH = 5;

    private AttackReach() {
    }

    public static boolean isAttributeDriven(Protocol protocol) {
        return protocol.atLeast(ATTRIBUTE_MAJOR, ATTRIBUTE_MINOR, ATTRIBUTE_PATCH);
    }

    public static boolean measuresToBoundingBox(Protocol protocol) {
        return isAttributeDriven(protocol);
    }

    public static double interactionRange(boolean creative, Protocol protocol) {
        if (!isAttributeDriven(protocol)) {
            return LEGACY_MAX_INTERACTION_DISTANCE;
        }
        double range = DEFAULT_ENTITY_INTERACTION_RANGE;
        if (creative) {
            range = range + CREATIVE_RANGE_BONUS;
        }
        return range + INTERACTION_BUFFER;
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

    public static double clampAttribute(double attributeValue) {
        if (attributeValue < ATTRIBUTE_MINIMUM) {
            return ATTRIBUTE_MINIMUM;
        }
        return attributeValue > ATTRIBUTE_MAXIMUM ? ATTRIBUTE_MAXIMUM : attributeValue;
    }

    public static double maximumDistanceSquared(boolean creative, Protocol protocol) {
        double range = interactionRange(creative, protocol);
        return range * range;
    }

    public static double maximumDistanceSquared(
            double attributeValue,
            boolean creative,
            Protocol protocol) {
        double range = interactionRange(attributeValue, creative, protocol);
        return range * range;
    }

    public static boolean withinReach(
            double distanceSquaredToTarget,
            boolean creative,
            Protocol protocol) {
        return distanceSquaredToTarget < maximumDistanceSquared(creative, protocol);
    }

    public static boolean exceedsReach(
            double distanceSquaredToTarget,
            boolean creative,
            Protocol protocol) {
        return !withinReach(distanceSquaredToTarget, creative, protocol);
    }

    public static Reality reachFrom(boolean creative, Protocol protocol) {
        return Reality.of(interactionRange(creative, protocol));
    }
}
