package teacommontea.dashboard;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Json {

    private Json() {}

    public static String escape(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }

    public static String object(Object... pairs) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(String.valueOf(pairs[i]))).append(':').append(value(pairs[i + 1]));
        }
        return sb.append('}').toString();
    }

    public static String array(List<String> encoded) {
        return "[" + String.join(",", encoded) + "]";
    }

    private static String value(Object v) {
        if (v == null) {
            return "null";
        }
        if (v instanceof Raw raw) {
            return raw.text;
        }
        if (v instanceof Number || v instanceof Boolean) {
            return String.valueOf(v);
        }
        return escape(String.valueOf(v));
    }

    public static Raw raw(String text) {
        return new Raw(text);
    }

    public static final class Raw {
        private final String text;
        private Raw(String text) {
            this.text = text == null ? "null" : text;
        }
    }

    public static Map<String, String> flat(String json) {
        Map<String, String> out = new LinkedHashMap<>();
        if (json == null) {
            return out;
        }
        int i = 0;
        int depth = 0;
        boolean inString = false;
        String key = null;
        StringBuilder token = new StringBuilder();

        while (i < json.length()) {
            char c = json.charAt(i);

            if (inString) {
                if (c == '\\' && i + 1 < json.length()) {
                    token.append(unescape(json.charAt(i + 1)));
                    i += 2;
                    continue;
                }
                if (c == '"') {
                    inString = false;
                    if (key == null) {
                        key = token.toString();
                    } else {
                        out.putIfAbsent(key, token.toString());
                        key = null;
                    }
                    token.setLength(0);
                    i++;
                    continue;
                }
                token.append(c);
                i++;
                continue;
            }

            switch (c) {
                case '"' -> inString = true;
                case '{', '[' -> { depth++; key = null; }
                case '}', ']' -> { depth--; key = null; }
                case ',' -> key = null;
                default -> {
                    if (key != null && !Character.isWhitespace(c) && c != ':') {
                        int end = i;
                        while (end < json.length() && ",}] \n\r\t".indexOf(json.charAt(end)) < 0) {
                            end++;
                        }
                        out.putIfAbsent(key, json.substring(i, end).trim());
                        key = null;
                        i = end;
                        continue;
                    }
                }
            }
            i++;
        }
        return out;
    }

    private static char unescape(char c) {
        return switch (c) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            default -> c;
        };
    }

    public static List<String> objects(String jsonArray) {
        List<String> out = new ArrayList<>();
        if (jsonArray == null) {
            return out;
        }
        int depth = 0;
        int start = -1;
        boolean inString = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    out.add(jsonArray.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return out;
    }
}
