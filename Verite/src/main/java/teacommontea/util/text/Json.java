package teacommontea.util.text;

import java.util.List;

public final class Json {

    private Json() {}

    public static String of(List<Span> spans) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("{\"text\":\"\",\"extra\":[");
        boolean first = true;
        for (Span s : spans) {
            if (!first) sb.append(',');
            first = false;
            span(sb, s);
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void span(StringBuilder sb, Span s) {
        sb.append('{');
        sb.append("\"text\":");
        quote(sb, s.text());
        if (s.colour() != null) {
            sb.append(",\"color\":");
            quote(sb, s.colour());
        }
        if (s.bold()) sb.append(",\"bold\":true");
        if (s.italic()) sb.append(",\"italic\":true");
        if (s.clickAction() != null) {
            sb.append(",\"click_event\":{\"action\":\"");
            sb.append(s.clickAction().id()).append("\",\"");
            sb.append(s.clickAction().field()).append("\":");
            if (s.clickAction() == Span.Click.CHANGE_PAGE) {
                sb.append(pageNumber(s.clickValue()));
            } else {
                quote(sb, s.clickValue());
            }
            sb.append('}');
        }
        if (s.hover() != null) {
            sb.append(",\"hover_event\":{\"action\":\"show_text\",\"value\":");
            sb.append(of(s.hover()));
            sb.append('}');
        }
        sb.append('}');
    }

    private static int pageNumber(String raw) {
        try {
            int n = Integer.parseInt(raw == null ? "" : raw.trim());
            return n < 1 ? 1 : n;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    static void quote(StringBuilder sb, String raw) {
        sb.append('"');
        String value = raw == null ? "" : raw;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20 || (c >= 0x7F && c <= 0x9F) || c == 0x2028 || c == 0x2029) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }
}
