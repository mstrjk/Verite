package teacommontea.api;

import java.util.UUID;


import teacommontea.veritedoux.EveEntry;

public final class VeriteFilter {

    private VeriteFilter() {}

    public static final UUID ANONYMOUS = new UUID(0L, 0L);

    public static FilterResult check(UUID player, String message) {
        return toResult(EveEntry.check(player, message));
    }

    public static FilterOutcome check(UUID player, String message, boolean repeatAware) {
        EveEntry.Result r = EveEntry.check(player, message);
        if (!repeatAware) {
            return new FilterOutcome(toResult(r), false);
        }
        return new FilterOutcome(toResult(r), r == EveEntry.Result.REPEAT);
    }

    public static int count(UUID player) {
        return EveEntry.count(player);
    }

    public static String blockMessage() {
        return EveEntry.blockMessage();
    }

    public static String repeatMessage() {
        return EveEntry.repeatMessage();
    }

    public static String selfHarmMessage() {
        return EveEntry.selfHarmMessage();
    }

    public static net.md_5.bungee.api.chat.BaseComponent[] blockNotice(FilterResult result, String message) {
        return EveEntry.blockNotice(toEve(result), message);
    }

    public static String foldAccents(String s) {
        return EveEntry.foldAccents(s);
    }

    public static String stripDeletes(String s) {
        return EveEntry.stripDeletes(s);
    }

    public static String stripEntities(String s) {
        return EveEntry.stripEntities(s);
    }

    public static String reduceRuns(String s, int max) {
        return EveEntry.reduceRuns(s, max);
    }

    public static String fingerprint(String word) {
        return EveEntry.fingerprint(word);
    }

    public static String trimEdges(String word) {
        return EveEntry.trimEdges(word);
    }

    private static FilterResult toResult(EveEntry.Result r) {
        if (r == null) {
            return FilterResult.CLEAN;
        }
        switch (r) {
            case CLEAN:     return FilterResult.CLEAN;
            case BLOCK:     return FilterResult.BLOCK;
            case SELF_HARM: return FilterResult.SELF_HARM;
            case ABUSE:     return FilterResult.ABUSE;
            case PROFANITY: return FilterResult.PROFANITY;
            case REPEAT:    return FilterResult.BLOCK;
            default:        return FilterResult.CLEAN;
        }
    }

    private static EveEntry.Result toEve(FilterResult r) {
        if (r == null) {
            return EveEntry.Result.CLEAN;
        }
        switch (r) {
            case CLEAN:     return EveEntry.Result.CLEAN;
            case BLOCK:     return EveEntry.Result.BLOCK;
            case SELF_HARM: return EveEntry.Result.SELF_HARM;
            case ABUSE:     return EveEntry.Result.ABUSE;
            case PROFANITY: return EveEntry.Result.PROFANITY;
            default:        return EveEntry.Result.CLEAN;
        }
    }
}
