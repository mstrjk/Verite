package teacommontea.api;


public final class VeriteAPI {

    private VeriteAPI() {}

    public static final String VERSION = "1";

    public static boolean moderationEnabled() {
        return VeriteModeration.enabled();
    }

    public static boolean vanishEnabled() {
        return VeriteVanish.enabled();
    }

    public static boolean captchaEnabled() {
        return VeriteCaptcha.enabled();
    }

    public static Class<VeriteModeration> moderation() {
        return VeriteModeration.class;
    }

    public static Class<VeritePlayerData> playerData() {
        return VeritePlayerData.class;
    }

    public static Class<VeriteStore> store() {
        return VeriteStore.class;
    }

    public static Class<VeriteVanish> vanish() {
        return VeriteVanish.class;
    }

    public static Class<VeriteCaptcha> captcha() {
        return VeriteCaptcha.class;
    }

    public static Class<VeriteFilter> filter() {
        return VeriteFilter.class;
    }

    public static Class<VeriteText> text() {
        return VeriteText.class;
    }
}
