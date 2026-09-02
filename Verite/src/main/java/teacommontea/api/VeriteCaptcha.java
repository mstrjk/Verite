package teacommontea.api;

import java.util.UUID;

import org.bukkit.entity.Player;


import teacommontea.veritesauver.captcha.CaptchaKind;
import teacommontea.veritesauver.captcha.CaptchaManager;

public final class VeriteCaptcha {

    private VeriteCaptcha() {}

    public static final String BYPASS_PERMISSION = CaptchaManager.BYPASS_PERMISSION;
    public static final String NOTIFY_PERMISSION = CaptchaManager.NOTIFY_PERMISSION;

    private static CaptchaManager captcha() {
        return CaptchaManager.instance();
    }

    public static boolean enabled() {
        return captcha() != null;
    }

    public static boolean isActive(UUID player) {
        return enabled() && captcha().isActive(player);
    }

    public static boolean challengeStandard(Player player, String source) {
        return enabled() && captcha().challenge(player, CaptchaKind.STANDARD, source);
    }

    public static boolean challengeDetailed(Player player, String source) {
        return enabled() && captcha().challenge(player, CaptchaKind.DETAILED, source);
    }

    public static boolean challenge(Player player, CaptchaType type, String source) {
        return enabled() && captcha().challenge(player, fromType(type), source);
    }

    private static CaptchaKind fromType(CaptchaType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case STANDARD: return CaptchaKind.STANDARD;
            case DETAILED: return CaptchaKind.DETAILED;
            default:       return null;
        }
    }
}
