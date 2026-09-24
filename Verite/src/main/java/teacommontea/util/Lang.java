package teacommontea.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class Lang {

    private Lang() {}

    private static volatile Map<String, String> entries = Collections.emptyMap();
    private static volatile Map<String, String> fallback = Collections.emptyMap();

    public static void load(org.bukkit.plugin.Plugin plugin, String code) {
        fallback = read(plugin, ConfigLang.DEFAULT);
        entries = code == null || code.equalsIgnoreCase(ConfigLang.DEFAULT)
                ? fallback
                : read(plugin, code);
    }

    public static String of(String key) {
        String v = entries.get(key);
        if (v == null) v = fallback.get(key);
        return v == null ? key : colours(v.replace("\\n", "\n"));
    }

    private static String colours(String body) {
        int open = body.indexOf('{');
        if (open < 0) return body;
        StringBuilder sb = new StringBuilder(body.length() + 16);
        int i = 0;
        while (i < body.length()) {
            char c = body.charAt(i);
            if (c != '{') {
                sb.append(c);
                i++;
                continue;
            }
            int close = body.indexOf('}', i);
            if (close < 0) {
                sb.append(body, i, body.length());
                break;
            }
            String name = body.substring(i + 1, close);
            String tag = Colours.byName(name);
            if (tag == null) {
                sb.append(body, i, close + 1);
            } else {
                sb.append(tag);
            }
            i = close + 1;
        }
        return sb.toString();
    }

    public static String of(String key, Object... pairs) {
        String body = of(key);
        if (pairs == null || pairs.length == 0) return body;
        StringBuilder sb = new StringBuilder(body);
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            replaceAll(sb, "%" + pairs[i] + "%", String.valueOf(pairs[i + 1]));
        }
        return sb.toString();
    }

    public static String counted(String key, long count, Object... pairs) {
        return of(count == 1 ? key + ".one" : key, pairs);
    }

    private static void replaceAll(StringBuilder sb, String token, String value) {
        int at = sb.indexOf(token);
        while (at >= 0) {
            sb.replace(at, at + token.length(), value);
            at = sb.indexOf(token, at + value.length());
        }
    }

    static Map<String, String> read(org.bukkit.plugin.Plugin plugin, String code) {
        String text = resource(plugin, "lang/global_" + code.toLowerCase(Locale.ROOT) + ".txt");
        return text == null ? Collections.emptyMap() : parse(text);
    }

    static Map<String, String> parse(String text) {
        Map<String, String> out = new LinkedHashMap<>();
        for (String raw : Yaml.splitLines(text)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int eq = line.indexOf('=');
            if (eq < 0) continue;
            String key = line.substring(0, eq).trim();
            if (key.isEmpty()) continue;
            out.put(key, line.substring(eq + 1).trim());
        }
        return out;
    }

    private static String resource(org.bukkit.plugin.Plugin plugin, String name) {
        try (InputStream in = plugin.getResource(name)) {
            if (in == null) return null;
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
