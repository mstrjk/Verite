package teacommontea.api;


import teacommontea.veritesauver.util.SauverFormat;
import teacommontea.veritesauver.util.SauverProtocol;

public final class VeriteFormat {

    private VeriteFormat() {}

    public static String fancyTime(long millis) {
        return SauverFormat.fancyTime(millis);
    }

    public static String plural(long count, String noun) {
        return SauverFormat.plural(count, noun);
    }

    public static String pluralize(long count, String noun) {
        return SauverFormat.pluralize(count, noun);
    }

    public static String versionName(int protocol) {
        return SauverProtocol.versionName(protocol);
    }
}
