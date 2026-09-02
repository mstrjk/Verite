package teacommontea.veritesauver.lockdown;

public final class SauverLockdown {

    private static volatile boolean active;
    private static volatile String reason;

    private SauverLockdown() {}

    public static boolean active() {
        return active;
    }

    public static String reason() {
        return reason == null ? "The server is locked down." : reason;
    }

    public static void begin(String why) {
        active = true;
        reason = why;
    }

    public static void end() {
        active = false;
        reason = null;
    }
}
