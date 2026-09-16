package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerDeltaY;

import java.util.Locale;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Bounce {

    public static final String SLIME_BLOCK = "slime_block";
    public static final String BED = "bed";

    public static final float SLIME_RESTITUTION_SOURCE = 1.0F;
    public static final float BED_RESTITUTION_SOURCE = 0.66F;
    public static final float BED_RESTITUTION_MODERN_SOURCE = 0.75F;

    public static final double SLIME_RESTITUTION = SLIME_RESTITUTION_SOURCE;
    public static final double BED_RESTITUTION = BED_RESTITUTION_SOURCE;
    public static final double BED_RESTITUTION_MODERN = BED_RESTITUTION_MODERN_SOURCE;

    public static final double LIVING_FACTOR = 1.0D;
    public static final double NON_LIVING_FACTOR = 0.8D;

    public static final int RESTITUTION_PROTOCOL_MAJOR = 26;
    public static final int RESTITUTION_PROTOCOL_MINOR = 2;
    public static final int RESTITUTION_PROTOCOL_PATCH = 0;

    private Bounce() {
    }

    public static String normalise(String blockName) {
        if (blockName == null) {
            return "";
        }
        String lower = blockName.toLowerCase(Locale.ROOT);
        int colon = lower.indexOf(':');
        String bare = colon < 0 ? lower : lower.substring(colon + 1);
        return bare.endsWith("_bed") ? BED : bare;
    }

    public static boolean restitutionIsBlockProperty(Protocol protocol) {
        return protocol.atLeast(
            RESTITUTION_PROTOCOL_MAJOR,
            RESTITUTION_PROTOCOL_MINOR,
            RESTITUTION_PROTOCOL_PATCH);
    }

    public static double restitutionOf(String blockName, Protocol protocol) {
        String key = normalise(blockName);
        if (SLIME_BLOCK.equals(key)) {
            return SLIME_RESTITUTION;
        }
        if (BED.equals(key)) {
            return restitutionIsBlockProperty(protocol)
                ? BED_RESTITUTION_MODERN
                : BED_RESTITUTION;
        }
        return 0.0D;
    }

    public static boolean isBouncy(String blockName, Protocol protocol) {
        return restitutionOf(blockName, protocol) > 0.0D;
    }

    public static double entityFactor(boolean living) {
        return living ? LIVING_FACTOR : NON_LIVING_FACTOR;
    }

    public static boolean applies(
            String blockName,
            double deltaY,
            boolean sneaking,
            Protocol protocol) {
        if (sneaking) {
            return false;
        }
        if (deltaY >= 0.0D) {
            return false;
        }
        return isBouncy(blockName, protocol);
    }

    public static double verticalAfter(
            double deltaY,
            String blockName,
            boolean sneaking,
            boolean living,
            Protocol protocol) {
        if (!applies(blockName, deltaY, sneaking, protocol)) {
            return deltaY;
        }
        double restitution = restitutionOf(blockName, protocol);
        return -deltaY * restitution * entityFactor(living);
    }

    public static double maximumRebound(
            double impactSpeed,
            String blockName,
            boolean living,
            Protocol protocol) {
        double restitution = restitutionOf(blockName, protocol);
        return Math.abs(impactSpeed) * restitution * entityFactor(living);
    }

    public static boolean suppressedBySneaking(boolean sneaking) {
        return sneaking;
    }

    public static Reality reboundFrom(
            double impactSpeed,
            String blockName,
            boolean living,
            Protocol protocol) {
        return Reality.of(maximumRebound(impactSpeed, blockName, living, protocol));
    }
}
