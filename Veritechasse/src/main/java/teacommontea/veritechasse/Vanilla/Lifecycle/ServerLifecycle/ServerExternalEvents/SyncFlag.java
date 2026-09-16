package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class SyncFlag {

    public static final String KEY = "sync_flag";

    public static final int RENAMED_MAJOR = 1;
    public static final int RENAMED_MINOR = 21;
    public static final int RENAMED_PATCH = 11;

    public static final String LEGACY_NAME = "hasImpulse";
    public static final String MODERN_NAME = "needsSync";

    private SyncFlag() {
    }

    public static boolean usesModernName(Protocol protocol) {
        return protocol.atLeast(RENAMED_MAJOR, RENAMED_MINOR, RENAMED_PATCH);
    }

    public static String fieldName(Protocol protocol) {
        return usesModernName(protocol) ? MODERN_NAME : LEGACY_NAME;
    }

    public static boolean guardsAgainstNonFinite(Protocol protocol) {
        return usesModernName(protocol);
    }

    public static boolean pushIsFinite(double xa, double ya, double za) {
        return Double.isFinite(xa) && Double.isFinite(ya) && Double.isFinite(za);
    }

    public static boolean pushApplies(double xa, double ya, double za, Protocol protocol) {
        if (!guardsAgainstNonFinite(protocol)) {
            return true;
        }
        return pushIsFinite(xa, ya, za);
    }

    public static boolean pushCorruptsDelta(double xa, double ya, double za, Protocol protocol) {
        return !guardsAgainstNonFinite(protocol) && !pushIsFinite(xa, ya, za);
    }

    public static boolean motionPacketSent(boolean flagSet) {
        return flagSet;
    }

    public static boolean afterSend() {
        return false;
    }
}
