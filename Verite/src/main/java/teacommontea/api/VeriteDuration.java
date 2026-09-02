package teacommontea.api;

import java.util.List;


import teacommontea.veritesauver.util.SauverDuration;

public final class VeriteDuration {

    private VeriteDuration() {}

    public static final long SECOND = SauverDuration.SECOND;
    public static final long MINUTE = SauverDuration.MINUTE;
    public static final long HOUR   = SauverDuration.HOUR;
    public static final long DAY    = SauverDuration.DAY;
    public static final long WEEK   = SauverDuration.WEEK;
    public static final long MONTH  = SauverDuration.MONTH;
    public static final long YEAR   = SauverDuration.YEAR;

    public static List<String> suggestions() {
        return SauverDuration.SUGGESTIONS;
    }

    public static long parse(String token) {
        return SauverDuration.parse(token);
    }

    public static long parseTwoToken(String number, String unitWord) {
        return SauverDuration.parseTwoToken(number, unitWord);
    }

    public static String format(long millis) {
        return SauverDuration.format(millis);
    }
}
