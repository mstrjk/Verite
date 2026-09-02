package teacommontea.util;


public final class Colours {

    public static final String HEX_SUCCESS = "00FF00";
    public static final String HEX_BRAND_ACCENT = "808080";
    public static final String HEX_DANGER = "AA0000";
    public static final String HEX_BRAND = "AACCEE";
    public static final String HEX_WARNING = "FF5555";
    public static final String HEX_MUTE = "FFAA00";
    public static final String HEX_WARN = "FFFF00";
    public static final String HEX_BRAND_ACCENT_SECONDARY = "FFFFFF";
    public static final String HEX_INVSEE_BLUE = "33BBDD";
    public static final String HEX_INVENTORY_NAME = "404040";

    public static final String SUCCESS = "<#" + HEX_SUCCESS + ">";
    public static final String BRAND_ACCENT = "<#" + HEX_BRAND_ACCENT + ">";
    public static final String DANGER = "<#" + HEX_DANGER + ">";
    public static final String BRAND = "<#" + HEX_BRAND + ">";
    public static final String WARNING = "<#" + HEX_WARNING + ">";
    public static final String MUTE = "<#" + HEX_MUTE + ">";
    public static final String WARN = "<#" + HEX_WARN + ">";
    public static final String BRAND_ACCENT_SECONDARY = "<#" + HEX_BRAND_ACCENT_SECONDARY + ">";
    public static final String INVSEE_BLUE = "<#" + HEX_INVSEE_BLUE + ">";
    public static final String INVENTORY_NAME = "<#" + HEX_INVENTORY_NAME + ">";

    public static String amp(String hex) {
        return "&#" + hex;
    }

    private static final java.util.regex.Pattern LEGACY_HEX = java.util.regex.Pattern.compile("&#([0-9a-fA-F]{6})");

    public static String legacy(String s) {
        if (s == null) return "";
        java.util.regex.Matcher m = LEGACY_HEX.matcher(s);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            StringBuilder rep = new StringBuilder("§x");
            for (char c : m.group(1).toCharArray()) rep.append('§').append(c);
            m.appendReplacement(out, rep.toString());
        }
        m.appendTail(out);
        return out.toString().replace('&', '§');
    }

    private Colours() {}
}
