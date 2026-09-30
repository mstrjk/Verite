package teacommontea.veritesauver.client;

public record Signal(Source source, String key, String value, int weight) {

    public enum Source {
        BRAND,
        CHANNEL,
        UNKNOWN_CHANNEL,
        CLIENT_INFO,
        SIGN,
        COOKIE,
        NORMALISED
    }

    public static Signal of(Source source, String key, String value, int weight) {
        return new Signal(source, key, value, weight);
    }

    public String describe() {
        return value == null || value.isBlank() ? key : key + "=" + value;
    }
}
