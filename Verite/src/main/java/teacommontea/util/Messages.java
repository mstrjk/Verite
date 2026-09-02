package teacommontea.util;

import net.md_5.bungee.api.chat.BaseComponent;
import teacommontea.util.text.Text;

public final class Messages {

    public static final String DEFAULT_PREFIX = Colours.BRAND_ACCENT + "[" + Colours.BRAND + "Verité" + Colours.BRAND_ACCENT + "]<reset>";

    public static final String DENY_PERMISSION = Colours.WARNING + "You do not have permission.";

    private static volatile String prefix = DEFAULT_PREFIX;

    public static void setPrefix(String p) {
        prefix = (p == null || p.isBlank()) ? DEFAULT_PREFIX : p;
    }

    public static String prefix() {
        return prefix;
    }

    public Messages(String ignored) {}
    public Messages() {}

    public BaseComponent[] parse(String miniMessage) {
        return Text.parse(miniMessage);
    }

    public BaseComponent[] prefixed(String miniMessage) {
        return Text.parse(prefix + " " + miniMessage);
    }
}
