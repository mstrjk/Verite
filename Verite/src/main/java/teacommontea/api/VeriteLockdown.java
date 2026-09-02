package teacommontea.api;


import teacommontea.veritesauver.lockdown.SauverLockdown;

public final class VeriteLockdown {

    private VeriteLockdown() {}

    public static boolean active() {
        return SauverLockdown.active();
    }

    public static String reason() {
        return SauverLockdown.reason();
    }

    public static void begin(String why) {
        SauverLockdown.begin(why);
    }

    public static void end() {
        SauverLockdown.end();
    }
}
