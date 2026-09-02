package teacommontea.veritesauver.util;


import java.util.List;
import java.util.Locale;
import teacommontea.veritesauver.core.Entry;

public final class SauverDuration {

    public static final long SECOND = 1000L;
    public static final long MINUTE = 60L * SECOND;
    public static final long HOUR   = 60L * MINUTE;
    public static final long DAY    = 24L * HOUR;
    public static final long WEEK   = 7L * DAY;
    public static final long MONTH  = 30L * DAY;
    public static final long YEAR   = 365L * DAY;

    public static final List<String> SUGGESTIONS = List.of(
        "perm", "30m", "1h", "6h", "12h", "1d", "3d", "7d", "14d", "30d", "1mo", "1y");

    private SauverDuration() {}

    public static long parse(String token) {
        if (token == null) {
            return -1;
        }
        String s = token.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) {
            return -1;
        }
        if (s.equals("perm") || s.equals("permanent") || s.equals("forever")) {
            return Entry.PERMANENT;
        }
        int i = 0;
        while (i < s.length() && (Character.isDigit(s.charAt(i)) || s.charAt(i) == '.')) {
            i++;
        }
        if (i == 0 || i == s.length()) {
            return -1;
        }
        double n;
        try {
            n = Double.parseDouble(s.substring(0, i));
        } catch (NumberFormatException e) {
            return -1;
        }
        long unit = unitMillis(s.substring(i));
        return unit < 0 ? -1 : (long) (n * unit);
    }

    public static long parseTwoToken(String number, String unitWord) {
        double n;
        try {
            n = Double.parseDouble(number.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
        long unit = unitMillis(unitWord.trim().toLowerCase(Locale.ROOT));
        return unit < 0 ? -1 : (long) (n * unit);
    }

    private static long unitMillis(String unit) {
        return switch (unit) {
            case "s", "sec", "secs", "second", "seconds" -> SECOND;
            case "m", "min", "mins", "minute", "minutes" -> MINUTE;
            case "h", "hr", "hrs", "hour", "hours"       -> HOUR;
            case "d", "day", "days"                      -> DAY;
            case "w", "wk", "wks", "week", "weeks"       -> WEEK;
            case "mo", "month", "months"                 -> MONTH;
            case "y", "yr", "yrs", "year", "years"       -> YEAR;
            default -> -1L;
        };
    }

    public static long parseShort(String token) {
        if (token == null || token.length() < 2) {
            return -1;
        }
        long unit = shortUnitMillis(token.charAt(token.length() - 1));
        if (unit < 0) {
            return -1;
        }
        double n;
        try {
            n = Double.parseDouble(token.substring(0, token.length() - 1));
        } catch (NumberFormatException e) {
            return -1;
        }
        return (long) (n * unit);
    }

    private static long shortUnitMillis(char unit) {
        return switch (unit) {
            case 's' -> SECOND;
            case 'm' -> MINUTE;
            case 'h' -> HOUR;
            case 'd' -> DAY;
            default -> -1L;
        };
    }

    public static String format(long millis) {
        return SauverFormat.fancyTime(millis);
    }
}
