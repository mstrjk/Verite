package teacommontea.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

public final class Yaml {

    private Yaml() {}

    public static YamlConfiguration loadYaml(File file) {
        if (file == null || !file.exists()) {
            return new YamlConfiguration();
        }
        try {
            String raw = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            YamlConfiguration y = new YamlConfiguration();
            y.loadFromString(protectBraceValues(raw));
            return y;
        } catch (Exception e) {
            return YamlConfiguration.loadConfiguration(file);
        }
    }

    private static final java.util.regex.Pattern BRACE_VALUE =
            java.util.regex.Pattern.compile("^(\\s*[^:#\\s][^:]*:\\s*)(\\{.*\\})(\\s*)$");

    public static String protectForValidation(String raw) {
        return protectBraceValues(raw);
    }

    private static String protectBraceValues(String raw) {
        StringBuilder out = new StringBuilder(raw.length() + 64);
        for (String line : raw.split("\n", -1)) {
            java.util.regex.Matcher m = BRACE_VALUE.matcher(line);
            if (m.matches() && !m.group(2).contains("'")) {
                out.append(m.group(1)).append('\'').append(m.group(2)).append('\'').append(m.group(3));
            } else {
                out.append(line);
            }
            out.append('\n');
        }
        if (out.length() > 0 && !raw.endsWith("\n")) out.setLength(out.length() - 1);
        return out.toString();
    }

    public static int indentOf(String line) {
        int n = 0;
        while (n < line.length() && line.charAt(n) == ' ') {
            n++;
        }
        return n;
    }

    public static int keyColon(String line) {
        boolean inq = false;
        char q = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inq) { if (c == q) inq = false; continue; }
            if (c == '"' || c == '\'') { inq = true; q = c; continue; }
            if (c == '#') return -1;
            if (c == ':') {
                if (i + 1 >= line.length() || line.charAt(i + 1) == ' ') return i;
            }
        }
        return -1;
    }

    public static int commentStartOutsideQuotes(String s) {
        boolean inq = false;
        char q = 0;
        boolean brace = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inq) { if (c == q) inq = false; continue; }
            if (c == '"' || c == '\'') { inq = true; q = c; continue; }
            if (c == '{') brace = true;
            if (c == '}') brace = false;
            if (c == '#' && !brace) return i;
        }
        return -1;
    }

    public static String stripComment(String line) {
        boolean inQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == '#' && !inQuote) {
                return line.substring(0, i);
            }
        }
        return line;
    }

    public static String trailingComment(String line, int from) {
        boolean inQuote = false;
        for (int i = from; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == '#' && !inQuote) {
                return line.substring(i);
            }
        }
        return "";
    }

    public static String valueOf(String line) {
        int colon = line.indexOf(':');
        if (colon < 0) {
            return null;
        }
        String rest = stripComment(line.substring(colon + 1)).trim();
        if (rest.length() >= 2 && rest.charAt(0) == '"' && rest.charAt(rest.length() - 1) == '"') {
            rest = rest.substring(1, rest.length() - 1);
        }
        return rest;
    }

    public static List<String> splitLines(String text) {
        List<String> out = new ArrayList<>();
        for (String s : text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1)) out.add(s);
        if (!out.isEmpty() && out.get(out.size() - 1).isEmpty()) out.remove(out.size() - 1);
        return out;
    }
}

