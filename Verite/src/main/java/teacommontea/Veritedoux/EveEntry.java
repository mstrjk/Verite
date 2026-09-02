package teacommontea.veritedoux;

import java.util.UUID;

import org.bukkit.plugin.Plugin;
import teacommontea.veritedoux.postprocess.EveStore;
import teacommontea.veritedoux.util.EveSettings;
import teacommontea.veritedoux.postprocess.SelfHarmMessages;
import teacommontea.veritedoux.util.EveText;
import teacommontea.veritedoux.preprocess.EveLang;
import teacommontea.veritedoux.preprocess.EveSegment;
import teacommontea.veritedoux.process.EveRepeat;
import teacommontea.veritedoux.process.EveBoards;
import teacommontea.veritedoux.process.EveGate;
import teacommontea.veritedoux.process.EveMatcher;
import teacommontea.veritedoux.process.EveRoute;

public final class EveEntry {

    public enum Result { CLEAN, BLOCK, SELF_HARM, ABUSE, PROFANITY, REPEAT }

    private static EveSettings settings = null;

    private EveEntry() {}

    public static void load(Plugin plugin) {
        EveSegment.clearVowels();
        settings = EveSettings.load(plugin);
        EveLang.configure(settings.langKnownWeight, settings.langUnknownWeight);
        EveRepeat.configure(settings.blockRepeat, settings.blockRepeatExact, settings.blockRepeatNear,
                settings.repeatHistorySize, settings.repeatWindowMs, settings.repeatSimilarityThreshold);
        EveText.loadConfusables(plugin);

        EveBoards.load(plugin, settings);
        EveGate.configure(settings);
        EveMatcher.configure(settings);
        EveRoute.configure(settings);

        teacommontea.veritedoux.util.Eve eve = EveBoards.eve();
        int wordCount = EveMatcher.buildFingerprints(eve);
        int ruleCount = eve == null ? 0 : eve.ruleCount();
        plugin.getLogger().info("EVE loaded " + ruleCount + " rules (window " + EveBoards.maxWords()
                + ", " + EveMatcher.fingerprintCount() + " fingerprints from " + wordCount + " words)");
    }

    public static void enableStore(EveStore s) {
        EveRoute.enableStore(s);
    }

    public static void shutdown() {
        EveRoute.shutdown();
    }

    public static Result check(String message) {
        return EveRoute.route(message);
    }

    public static Result check(UUID player, String message) {
        return EveRoute.route(player, message);
    }

    public static EveRoute.Explain explain(String message) {
        return EveRoute.explain(message);
    }

    public static void recordSend(UUID player, String message) {
        EveRoute.recordSend(player, message);
    }

    public static int count(UUID player) {
        return EveRoute.count(player);
    }

    public static boolean testEveReady() {
        return EveBoards.eve() != null;
    }

    public static String blockMessage() {
        return settings == null ? "" : applyPrefixToken(settings.blockMessage);
    }

    public static String repeatMessage() {
        return settings == null ? "" : applyPrefixToken(settings.repeatMessage);
    }

    private static String applyPrefixToken(String message) {
        if (message == null) return "";
        return message.contains("@prefix") ? message.replace("@prefix", legacyPrefix()) : message;
    }

    private static String legacyPrefix() {
        try {
            String legacy = teacommontea.util.text.Text.toLegacy(teacommontea.util.Messages.prefix());
            return legacy.endsWith("§r") ? legacy : legacy + "§r";
        } catch (Throwable t) {
            return teacommontea.util.Messages.prefix();
        }
    }

    public static String selfHarmMessage() {
        return SelfHarmMessages.message();
    }

    public static net.md_5.bungee.api.chat.BaseComponent[] blockNotice(Result r, String message) {
        if (r == Result.SELF_HARM) {
            String body = SelfHarmMessages.message().replace("\\n", "\n");
            return net.md_5.bungee.api.chat.TextComponent.fromLegacyText(body);
        }
        if (r == Result.REPEAT) {
            String repeat = repeatMessage();
            if (repeat == null || repeat.isEmpty()) {
                return new net.md_5.bungee.api.chat.BaseComponent[] { new net.md_5.bungee.api.chat.TextComponent("") };
            }
            return net.md_5.bungee.api.chat.TextComponent.fromLegacyText(repeat.replace("\\n", "\n"));
        }
        String legacy = blockMessage();
        if (legacy == null || legacy.isEmpty()) {
            return new net.md_5.bungee.api.chat.BaseComponent[] { new net.md_5.bungee.api.chat.TextComponent("") };
        }
        return net.md_5.bungee.api.chat.TextComponent.fromLegacyText(legacy.replace("\\n", "\n"));
    }

    public static String fingerprint(String w) {
        return EveText.fingerprint(w);
    }

    public static String stripEntities(String s) {
        return EveText.stripEntities(s);
    }

    public static String stripDeletes(String s) {
        return EveText.stripDeletes(s);
    }

    public static String foldAccents(String s) {
        return EveText.foldAccents(s);
    }

    public static String reduceRuns(String s, int max) {
        return EveText.reduceRuns(s, max);
    }

    public static String trimEdges(String w) {
        return EveText.trimEdges(w);
    }
}
