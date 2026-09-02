package teacommontea.veritesauver.captcha;

import teacommontea.util.Colours;
import teacommontea.veritesauver.captcha.CaptchaCompleteEvent;
import teacommontea.veritesauver.captcha.CaptchaKind;
import teacommontea.veritesauver.captcha.CaptchaOutcome;
import teacommontea.veritesauver.captcha.PunishmentRequestedEvent;
import teacommontea.util.Messages;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public final class CaptchaManager {

    public static final String BYPASS_PERMISSION = "verite.bypass";

    public static final String NOTIFY_PERMISSION = "veritesauver.notify.captcha";

    private static volatile CaptchaManager instance;

    private final JavaPlugin plugin;
    private final Messages messages;
    private final CaptchaStandard standard;
    private final CaptchaMap detailed;

    public CaptchaManager(JavaPlugin plugin, Messages messages) {
        this.plugin = plugin;
        this.messages = messages;
        this.standard = new CaptchaStandard(plugin, messages, this);
        this.detailed = new CaptchaMap(plugin, messages, this);
        instance = this;
    }

    public static CaptchaManager instance() {
        return instance;
    }

    public void shutdown() {
        if (instance == this) {
            instance = null;
        }
    }

    public CaptchaStandard standardListener() {
        return standard;
    }

    public CaptchaMap detailedListener() {
        return detailed;
    }

    public boolean challenge(Player player, CaptchaKind kind, String source) {
        if (player == null || !player.isOnline()) {
            return false;
        }
        if (player.hasPermission(BYPASS_PERMISSION)) {
            return false;
        }
        UUID u = player.getUniqueId();
        if (standard.isActive(u) || detailed.isActive(u)) {
            return false;
        }
        if (kind == CaptchaKind.DETAILED) {
            detailed.open(player, source);
        } else {
            standard.open(player, source);
        }
        return true;
    }

    public boolean isActive(UUID player) {
        return standard.isActive(player) || detailed.isActive(player);
    }

    public CaptchaStandard standard() {
        return standard;
    }

    public CaptchaMap detailed() {
        return detailed;
    }

    void resolved(Player player, CaptchaKind kind, CaptchaOutcome outcome, long durationMs,
                  List<String> failedChoices, String source) {
        boolean punish = outcome != CaptchaOutcome.PASS;
        CaptchaCompleteEvent complete = new CaptchaCompleteEvent(
                player, kind, outcome, durationMs, failedChoices, punish, source);
        Bukkit.getPluginManager().callEvent(complete);

        announce(player, kind, outcome, durationMs, failedChoices.size());

        if (!punish) {
            return;
        }
        PunishmentRequestedEvent.Cause cause = outcome == CaptchaOutcome.TIMEOUT
                ? PunishmentRequestedEvent.Cause.CAPTCHA_TIMEOUT
                : PunishmentRequestedEvent.Cause.CAPTCHA_FAILED;
        String kickMessage = Colours.WARNING + "Failed verification.";
        PunishmentRequestedEvent request = new PunishmentRequestedEvent(player, cause, source, kickMessage);
        Bukkit.getPluginManager().callEvent(request);

        if (request.isCancelled() || !player.isOnline()) {
            return;
        }
        player.kickPlayer(teacommontea.util.text.Text.toLegacy(request.getKickMessage()));
    }

    private void announce(Player player, CaptchaKind kind, CaptchaOutcome outcome,
                          long durationMs, int fails) {
        String word = switch (outcome) {
            case PASS -> Colours.BRAND + "passed";
            case FAIL -> Colours.WARNING + "failed";
            case TIMEOUT -> Colours.WARNING + "timed out on";
        };
        String body = Colours.BRAND_ACCENT_SECONDARY + player.getName() + " " + Colours.BRAND_ACCENT_SECONDARY + word + " " + Colours.BRAND_ACCENT_SECONDARY + "the "
                + kind.name().toLowerCase() + " captcha " + Colours.BRAND_ACCENT_SECONDARY + "(" + Colours.BRAND_ACCENT_SECONDARY
                + formatTime(durationMs) + Colours.BRAND_ACCENT_SECONDARY + ", " + Colours.BRAND_ACCENT_SECONDARY + fails + " " + Colours.BRAND_ACCENT_SECONDARY + "wrong).";
        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (!staff.hasPermission(NOTIFY_PERMISSION)) {
                continue;
            }
            staff.spigot().sendMessage(messages.prefixed(body));
        }
    }

    static String formatTime(long millis) {
        long secs = millis / 1000;
        long tenths = (millis - secs * 1000) / 100;
        return secs + "." + tenths + "s";
    }
}
