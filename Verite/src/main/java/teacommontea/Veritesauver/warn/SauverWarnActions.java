package teacommontea.veritesauver.warn;

import teacommontea.util.Colours;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;
import teacommontea.veritesauver.Sauver;

public final class SauverWarnActions {

    private record Step(int count, List<String> commands) {}

    private static final List<Step> LADDER = List.of(
        new Step(3, List.of("tempmute $player 1d Reached 3 warnings")),
        new Step(5, List.of("tempban $player 1d Reached 5 warnings"))
    );

    private SauverWarnActions() {}

    public static void onWarn(UUID targetUuid, String targetName, int activeCount) {
        for (Step step : LADDER) {
            if (step.count() == activeCount) {
                runStep(step, targetName);
            }
        }
    }

    private static void runStep(Step step, String targetName) {
        for (String raw : step.commands()) {
            String cmd = raw.replace("$player", targetName);
            teacommontea.util.sched.Sched.executeGlobal(() ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }
        Sauver.instance().messages().notify("veritesauver.notify.broadcast",
                Colours.BRAND_ACCENT_SECONDARY + "Auto-action: " + Colours.BRAND_ACCENT_SECONDARY + targetName
                        + " " + Colours.BRAND_ACCENT_SECONDARY + "hit " + Colours.WARNING + step.count() + " " + Colours.BRAND_ACCENT_SECONDARY + "warnings.");
    }
}
