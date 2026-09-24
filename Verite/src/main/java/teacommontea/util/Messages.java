package teacommontea.util;

import teacommontea.util.text.Span;
import teacommontea.util.text.Text;

import java.util.List;

public final class Messages {

    public static final String DEFAULT_PREFIX = Colours.BRAND_ACCENT + "[" + Colours.BRAND + "Verité" + Colours.BRAND_ACCENT + "]<reset>";

    private static volatile String prefix = DEFAULT_PREFIX;

    public static void setPrefix(String p) {
        prefix = (p == null || p.isBlank()) ? DEFAULT_PREFIX : p;
    }

    public static String prefix() {
        return prefix;
    }

    public Messages(String ignored) {}
    public Messages() {}

    public List<Span> parse(String tagged) {
        return Text.parse(tagged);
    }

    public List<Span> prefixed(String tagged) {
        return Text.parse(prefix + " " + tagged);
    }
}
