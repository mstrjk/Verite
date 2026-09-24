package teacommontea.util;

import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ConfigLang {

    private ConfigLang() {}

    public static final String DEFAULT = "en";

    private static final List<String> SUPPORTED = List.of("en", "es", "fr", "it", "pt", "de");

    public static boolean supported(String code) {
        return code != null && SUPPORTED.contains(code.toLowerCase(Locale.ROOT));
    }

    public static List<String> supported() {
        return SUPPORTED;
    }

    public static String read(String configText) {
        String raw = readRaw(configText);
        return supported(raw) ? raw.toLowerCase(Locale.ROOT) : DEFAULT;
    }

    public static String read(Plugin plugin, String configText) {
        String raw = readRaw(configText);
        if (supported(raw)) return raw.toLowerCase(Locale.ROOT);
        if (raw != null && !raw.isEmpty()) {
            plugin.getLogger().warning(ConsoleColours.bad(Lang.of("config.language.unsupported",
                    "value", raw, "supported", String.join(", ", SUPPORTED))));
        }
        return DEFAULT;
    }

    static String readRaw(String configText) {
        if (configText == null) return DEFAULT;
        for (String line : Yaml.splitLines(configText)) {
            String t = line.trim();
            if (t.startsWith("#")) continue;
            int colon = t.indexOf(':');
            if (colon < 0) continue;
            if (!t.substring(0, colon).trim().equals("config.language")) continue;
            String v = Yaml.stripComment(t.substring(colon + 1)).trim();
            if (v.length() >= 2 && (v.charAt(0) == '"' || v.charAt(0) == '\'')
                    && v.charAt(v.length() - 1) == v.charAt(0)) {
                v = v.substring(1, v.length() - 1);
            }
            return v.trim();
        }
        return DEFAULT;
    }

    public static String localise(Plugin plugin, String bundledText, String code) {
        if (bundledText == null) return null;
        if (code == null || code.equalsIgnoreCase(DEFAULT) || !supported(code)) return bundledText;
        Bundle bundle = load(plugin, code);
        if (bundle == null) return bundledText;
        try {
            return apply(bundledText, bundle);
        } catch (Exception e) {
            plugin.getLogger().warning(ConsoleColours.faint(Lang.of("config.translate.skipped", "code", code)) + Trace.of(e));
            return bundledText;
        }
    }

    static final class Bundle {
        final Map<String, String> comments;
        final Map<String, String> values;
        Bundle(Map<String, String> comments, Map<String, String> values) {
            this.comments = comments;
            this.values = values;
        }
    }

    static Bundle load(Plugin plugin, String code) {
        String text = readResource(plugin, "lang/config_" + code.toLowerCase(Locale.ROOT) + ".txt");
        return text == null ? null : parse(text);
    }

    static Bundle parse(String text) {
        Map<String, String> comments = new LinkedHashMap<>();
        Map<String, String> values = new LinkedHashMap<>();
        for (String raw : Yaml.splitLines(text)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int eq = line.indexOf(" = ");
            if (eq < 0) continue;
            String key = line.substring(0, eq).trim();
            String body = line.substring(eq + 3);
            boolean isValue = key.startsWith("value:");
            if (isValue) key = key.substring("value:".length()).trim();
            String path = normalisePath(key);
            if (path.isEmpty()) continue;
            if (isValue) {
                values.put(path, body.trim());
            } else {
                comments.put(path, body);
            }
        }
        return new Bundle(comments, values);
    }

    private static String normalisePath(String key) {
        StringBuilder sb = new StringBuilder();
        for (String part : key.split(">")) {
            String p = part.trim();
            if (p.isEmpty()) continue;
            if (sb.length() > 0) sb.append(SEP);
            sb.append(p);
        }
        return sb.toString();
    }

    private static final char SEP = '';

    static String apply(String bundledText, Bundle bundle) {
        List<String> lines = Yaml.splitLines(bundledText);
        Map<Integer, String> pathByLine = pathsByLine(lines);

        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < lines.size()) {
            String path = pathByLine.get(i);
            if (path == null) {
                out.add(lines.get(i));
                i++;
                continue;
            }

            String translated = bundle.comments.get(path);
            int commentStart = commentStartFor(lines, i);
            if (translated != null) {
                trimTrailing(out, i - commentStart);
                out.addAll(renderComment(translated, Yaml.indentOf(lines.get(i))));
            }

            out.add(localiseValue(lines.get(i), bundle.values.get(path)));
            i++;
        }

        String eol = bundledText.contains("\r\n") ? "\r\n" : "\n";
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < out.size(); j++) {
            sb.append(out.get(j));
            if (j < out.size() - 1) sb.append(eol);
        }
        if (bundledText.endsWith("\n") || bundledText.endsWith("\r")) sb.append(eol);
        return sb.toString();
    }

    private static void trimTrailing(List<String> out, int count) {
        for (int n = 0; n < count && !out.isEmpty(); n++) {
            String last = out.get(out.size() - 1).trim();
            if (!last.startsWith("#") && !last.isEmpty()) break;
            out.remove(out.size() - 1);
        }
    }

    private static List<String> renderComment(String translated, int indent) {
        StringBuilder pad = new StringBuilder();
        for (int n = 0; n < indent; n++) pad.append(' ');
        List<String> rendered = new ArrayList<>();
        for (String part : splitCommentLines(translated)) {
            String body = part.trim();
            rendered.add(body.isEmpty() ? pad + "#" : pad + "# " + body);
        }
        return rendered;
    }

    static List<String> splitCommentLines(String translated) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < translated.length(); i++) {
            char c = translated.charAt(i);
            if (c == '\\' && i + 1 < translated.length() && translated.charAt(i + 1) == 'n') {
                boolean spaceBefore = i > 0 && translated.charAt(i - 1) == ' ';
                if (spaceBefore) {
                    cur.append("\\n");
                    i++;
                    continue;
                }
                parts.add(cur.toString());
                cur.setLength(0);
                i++;
                continue;
            }
            cur.append(c);
        }
        parts.add(cur.toString());
        return parts;
    }

    private static String localiseValue(String line, String translated) {
        if (translated == null) return line;
        int colon = Yaml.keyColon(line);
        if (colon < 0) return line;
        String after = line.substring(colon + 1);
        int hash = Yaml.commentStartOutsideQuotes(after);
        String trailing = hash >= 0 ? after.substring(hash) : "";
        String existing = (hash >= 0 ? after.substring(0, hash) : after).trim();
        if (existing.isEmpty()) return line;
        if (!SelfHeal.sameShape(existing, translated)) return line;
        int lead = 0;
        while (lead < after.length() && after.charAt(lead) == ' ') lead++;
        String leadSpace = after.substring(0, Math.max(1, lead));
        return line.substring(0, colon + 1) + leadSpace + translated
                + (trailing.isEmpty() ? "" : " ") + trailing;
    }

    private static Map<Integer, String> pathsByLine(List<String> lines) {
        Map<Integer, String> byLine = new LinkedHashMap<>();
        List<int[]> stack = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("-")) continue;

            int colon = Yaml.keyColon(line);
            if (colon < 0) continue;

            int indent = Yaml.indentOf(line);
            String name = line.substring(indent, colon).trim();
            if (name.isEmpty()) continue;

            while (!stack.isEmpty() && stack.get(stack.size() - 1)[0] >= indent) {
                stack.remove(stack.size() - 1);
                names.remove(names.size() - 1);
            }
            StringBuilder pathB = new StringBuilder();
            for (String anc : names) pathB.append(anc).append(SEP);
            pathB.append(name);

            byLine.put(i, pathB.toString());
            stack.add(new int[]{indent});
            names.add(name);
        }
        return byLine;
    }

    private static int commentStartFor(List<String> lines, int keyLine) {
        int start = keyLine;
        int j = keyLine - 1;
        while (j >= 0) {
            String t = lines.get(j).trim();
            if (t.startsWith("#")) { start = j; j--; continue; }
            if (t.isEmpty()) {
                int k = j;
                while (k >= 0 && lines.get(k).trim().isEmpty()) k--;
                if (k >= 0 && lines.get(k).trim().startsWith("#")) { j = k; continue; }
            }
            break;
        }
        return start;
    }

    private static String readResource(Plugin plugin, String name) {
        try (InputStream in = plugin.getResource(name)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
