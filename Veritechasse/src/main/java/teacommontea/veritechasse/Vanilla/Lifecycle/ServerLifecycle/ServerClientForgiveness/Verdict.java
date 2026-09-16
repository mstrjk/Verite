package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness;

public final class Verdict {

    public static final String EVALUATE = "evaluate";

    public static final String DISCARD = "discard";

    public static final String BAD_PACKETS = "bad_packets";

    private Verdict() {
    }

    public static boolean isEvaluate(String verdict) {
        return EVALUATE.equals(verdict);
    }

    public static boolean isDiscard(String verdict) {
        return DISCARD.equals(verdict);
    }

    public static boolean isBadPackets(String verdict) {
        return BAD_PACKETS.equals(verdict);
    }

    public static boolean baselineSurvives(String verdict) {
        return isEvaluate(verdict);
    }

    public static boolean reportable(String verdict) {
        return isBadPackets(verdict);
    }

    public static String worst(String first, String second) {
        if (isBadPackets(first) || isBadPackets(second)) {
            return BAD_PACKETS;
        }
        if (isDiscard(first) || isDiscard(second)) {
            return DISCARD;
        }
        return EVALUATE;
    }

    public static String worst(String first, String second, String third) {
        return worst(worst(first, second), third);
    }
}
