package teacommontea.util.text;

import java.util.List;

public final class Legacy {

    private Legacy() {}

    public static final char SECTION = '§';

    public static String of(List<Span> spans) {
        StringBuilder sb = new StringBuilder(64);
        for (Span s : spans) {
            append(sb, s);
        }
        return sb.toString();
    }

    private static void append(StringBuilder sb, Span s) {
        String colour = s.colour();
        if (colour != null) {
            sb.append(codesFor(colour));
        }
        if (s.bold()) sb.append(SECTION).append('l');
        if (s.italic()) sb.append(SECTION).append('o');
        sb.append(s.text());
    }

    private static String codesFor(String colour) {
        if (colour.length() == 7 && colour.charAt(0) == '#') {
            StringBuilder sb = new StringBuilder(14);
            sb.append(SECTION).append('x');
            for (int i = 1; i < 7; i++) {
                sb.append(SECTION).append(Character.toLowerCase(colour.charAt(i)));
            }
            return sb.toString();
        }
        char code = named(colour);
        return code == 0 ? "" : String.valueOf(SECTION) + code;
    }

    private static char named(String colour) {
        return switch (colour.toLowerCase(java.util.Locale.ROOT)) {
            case "black" -> '0';
            case "dark_blue" -> '1';
            case "dark_green" -> '2';
            case "dark_aqua" -> '3';
            case "dark_red" -> '4';
            case "dark_purple" -> '5';
            case "gold" -> '6';
            case "gray" -> '7';
            case "dark_gray" -> '8';
            case "blue" -> '9';
            case "green" -> 'a';
            case "aqua" -> 'b';
            case "red" -> 'c';
            case "light_purple" -> 'd';
            case "yellow" -> 'e';
            case "white" -> 'f';
            default -> 0;
        };
    }

    public static List<Span> parse(String legacy) {
        List<Span> out = new java.util.ArrayList<>();
        if (legacy == null || legacy.isEmpty()) {
            out.add(new Span(""));
            return out;
        }
        String colour = "white";
        boolean bold = false;
        boolean italic = false;
        StringBuilder run = new StringBuilder();
        int i = 0;
        while (i < legacy.length()) {
            char c = legacy.charAt(i);
            if (c != SECTION || i + 1 >= legacy.length()) {
                run.append(c);
                i++;
                continue;
            }
            char code = Character.toLowerCase(legacy.charAt(i + 1));
            String hex = hexAt(legacy, i);
            if (hex != null) {
                flush(out, run, colour, bold, italic);
                colour = hex;
                bold = false;
                italic = false;
                i += 14;
                continue;
            }
            flush(out, run, colour, bold, italic);
            if (code == 'l') {
                bold = true;
            } else if (code == 'o') {
                italic = true;
            } else if (code == 'r') {
                colour = "white";
                bold = false;
                italic = false;
            } else {
                String named = nameOf(code);
                if (named != null) {
                    colour = named;
                    bold = false;
                    italic = false;
                }
            }
            i += 2;
        }
        flush(out, run, colour, bold, italic);
        if (out.isEmpty()) out.add(new Span(""));
        return out;
    }

    private static String hexAt(String legacy, int i) {
        if (i + 13 >= legacy.length()) return null;
        if (Character.toLowerCase(legacy.charAt(i + 1)) != 'x') return null;
        StringBuilder sb = new StringBuilder(7);
        sb.append('#');
        for (int k = 0; k < 6; k++) {
            int at = i + 2 + k * 2;
            if (legacy.charAt(at) != SECTION) return null;
            char d = Character.toLowerCase(legacy.charAt(at + 1));
            if (Character.digit(d, 16) < 0) return null;
            sb.append(d);
        }
        return sb.toString();
    }

    private static void flush(List<Span> out, StringBuilder run, String colour, boolean bold, boolean italic) {
        if (run.length() == 0) return;
        out.add(new Span(run.toString()).colour(colour).bold(bold).italic(italic));
        run.setLength(0);
    }

    private static String nameOf(char code) {
        return switch (code) {
            case '0' -> "black";
            case '1' -> "dark_blue";
            case '2' -> "dark_green";
            case '3' -> "dark_aqua";
            case '4' -> "dark_red";
            case '5' -> "dark_purple";
            case '6' -> "gold";
            case '7' -> "gray";
            case '8' -> "dark_gray";
            case '9' -> "blue";
            case 'a' -> "green";
            case 'b' -> "aqua";
            case 'c' -> "red";
            case 'd' -> "light_purple";
            case 'e' -> "yellow";
            case 'f' -> "white";
            default -> null;
        };
    }

    public static String strip(String legacy) {
        if (legacy == null || legacy.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(legacy.length());
        for (int i = 0; i < legacy.length(); i++) {
            char c = legacy.charAt(i);
            if (c == SECTION && i + 1 < legacy.length()) {
                i++;
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
