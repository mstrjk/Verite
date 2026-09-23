package teacommontea.util;

public final class ConsoleColours {

    private static final boolean ENABLED = detect();
    private static final boolean TRUECOLOUR = detectTruecolour();

    public static final String RESET = wrap("\u001B[0m");
    public static final String BOLD = wrap("\u001B[1m");

    public static final String BLACK = wrap("\u001B[30m");
    public static final String RED = wrap("\u001B[31m");
    public static final String GREEN = wrap("\u001B[32m");
    public static final String YELLOW = wrap("\u001B[33m");
    public static final String BLUE = wrap("\u001B[34m");
    public static final String MAGENTA = wrap("\u001B[35m");
    public static final String CYAN = wrap("\u001B[36m");
    public static final String WHITE = wrap("\u001B[37m");
    public static final String GREY = wrap("\u001B[90m");

    public static final String SUCCESS = truecolour(Colours.HEX_SUCCESS, GREEN);
    public static final String DANGER = truecolour(Colours.HEX_DANGER, RED);
    public static final String WARNING = truecolour(Colours.HEX_WARNING, RED);
    public static final String WARN = truecolour(Colours.HEX_WARN, YELLOW);
    public static final String MUTE = truecolour(Colours.HEX_MUTE, YELLOW);
    public static final String BRAND = truecolour(Colours.HEX_BRAND, CYAN);
    public static final String BRAND_ACCENT = truecolour(Colours.HEX_BRAND_ACCENT, GREY);
    public static final String BRAND_ACCENT_SECONDARY = truecolour(Colours.HEX_BRAND_ACCENT_SECONDARY, WHITE);

    public static boolean enabled() {
        return ENABLED;
    }

    public static String strip(String s) {
        if (s == null) {
            return "";
        }
        return s.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    public static String paint(String colour, String text) {
        if (!ENABLED || colour == null || colour.isEmpty()) {
            return text == null ? "" : text;
        }
        return colour + (text == null ? "" : text) + RESET;
    }

    public static String ok(Object text) {
        return paint(SUCCESS, String.valueOf(text));
    }

    public static String bad(Object text) {
        return paint(WARNING, String.valueOf(text));
    }

    public static String fatal(Object text) {
        return paint(DANGER, String.valueOf(text));
    }

    public static String note(Object text) {
        return paint(BRAND, String.valueOf(text));
    }

    public static String value(Object text) {
        return paint(BRAND_ACCENT_SECONDARY, String.valueOf(text));
    }

    public static String faint(Object text) {
        return paint(BRAND_ACCENT, String.valueOf(text));
    }

    private static String wrap(String code) {
        return ENABLED ? code : "";
    }

    private static String truecolour(String hex, String fallback) {
        if (!ENABLED) {
            return "";
        }
        if (!TRUECOLOUR) {
            return fallback;
        }
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return "\u001B[38;2;" + r + ";" + g + ";" + b + "m";
    }

    private static boolean detect() {
        String override = System.getProperty("verite.console.colour");
        if (override != null) {
            return Boolean.parseBoolean(override);
        }
        if (System.getenv("NO_COLOR") != null) {
            return false;
        }
        String term = System.getenv("TERM");
        if ("dumb".equalsIgnoreCase(term)) {
            return false;
        }
        String os = System.getProperty("os.name", "");
        if (os.toLowerCase(java.util.Locale.ROOT).contains("win")) {
            return System.getenv("WT_SESSION") != null
                    || System.getenv("ANSICON") != null
                    || "xterm".equalsIgnoreCase(term)
                    || System.getenv("TERM_PROGRAM") != null;
        }
        return term != null && !term.isEmpty();
    }

    private static boolean detectTruecolour() {
        if (!ENABLED) {
            return false;
        }
        String override = System.getProperty("verite.console.truecolour");
        return override == null || Boolean.parseBoolean(override);
    }

    private ConsoleColours() {}
}
