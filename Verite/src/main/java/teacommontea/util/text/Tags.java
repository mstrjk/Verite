package teacommontea.util.text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class Tags {

    private Tags() {}

    private static final class Style {
        String colour = "white";
        boolean bold;
        boolean italic;
        Span.Click click;
        String clickValue;
        List<Span> hover;

        Style copy() {
            Style s = new Style();
            s.colour = colour;
            s.bold = bold;
            s.italic = italic;
            s.click = click;
            s.clickValue = clickValue;
            s.hover = hover;
            return s;
        }
    }

    public static List<Span> parse(String tagged) {
        List<Span> out = new ArrayList<>();
        Deque<Style> stack = new ArrayDeque<>();
        stack.push(new Style());

        StringBuilder run = new StringBuilder();
        int i = 0;
        int n = tagged == null ? 0 : tagged.length();
        while (i < n) {
            char c = tagged.charAt(i);
            if (c == '\\' && i + 1 < n && tagged.charAt(i + 1) == 'n') {
                flush(out, run, stack.peek());
                out.add(new Span("\n"));
                i += 2;
                continue;
            }
            if (c != '<') {
                run.append(c);
                i++;
                continue;
            }
            int close = closingBracket(tagged, i);
            if (close < 0) {
                run.append(c);
                i++;
                continue;
            }
            if (applyTag(tagged.substring(i + 1, close), out, run, stack)) {
                i = close + 1;
            } else {
                run.append('<');
                i++;
            }
        }
        flush(out, run, stack.peek());
        if (out.isEmpty()) out.add(new Span(""));
        return out;
    }

    private static int closingBracket(String tagged, int open) {
        boolean quoted = false;
        for (int i = open + 1; i < tagged.length(); i++) {
            char c = tagged.charAt(i);
            if (c == '\\' && i + 1 < tagged.length()) {
                i++;
            } else if (c == '\'') {
                quoted = !quoted;
            } else if (c == '>' && !quoted) {
                return i;
            }
        }
        return -1;
    }

    private static boolean applyTag(String tag, List<Span> out, StringBuilder run, Deque<Style> stack) {
        String lower = tag.toLowerCase(java.util.Locale.ROOT);
        if (lower.startsWith("#") && tag.length() == 7) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            s.colour = "#" + tag.substring(1).toLowerCase(java.util.Locale.ROOT);
            stack.push(s);
            return true;
        }
        if (lower.equals("reset")) {
            flush(out, run, stack.peek());
            stack.push(new Style());
            return true;
        }
        if (lower.equals("bold") || lower.equals("b")) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            s.bold = true;
            stack.push(s);
            return true;
        }
        if (lower.equals("italic") || lower.equals("i")) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            s.italic = true;
            stack.push(s);
            return true;
        }
        if (lower.equals("/italic") || lower.equals("/i")
                || lower.equals("/bold") || lower.equals("/b")
                || lower.equals("/click") || lower.equals("/hover")) {
            flush(out, run, stack.peek());
            if (stack.size() > 1) stack.pop();
            return true;
        }
        if (lower.equals("newline") || lower.equals("br")) {
            flush(out, run, stack.peek());
            out.add(new Span("\n"));
            return true;
        }
        if (lower.startsWith("click:")) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            String spec = tag.substring("click:".length());
            int colon = spec.indexOf(':');
            s.click = Span.Click.of(colon < 0 ? spec : spec.substring(0, colon));
            s.clickValue = colon < 0 ? "" : unquote(spec.substring(colon + 1));
            stack.push(s);
            return true;
        }
        if (lower.startsWith("hover:show_text:")) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            s.hover = parse(unquote(tag.substring("hover:show_text:".length())));
            stack.push(s);
            return true;
        }
        return false;
    }

    private static String unquote(String s) {
        String t = s.trim();
        if (t.length() >= 2 && t.charAt(0) == '\'' && t.charAt(t.length() - 1) == '\'') {
            t = t.substring(1, t.length() - 1);
        }
        return unescape(t);
    }

    private static String unescape(String s) {
        if (s.indexOf('\\') < 0) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                sb.append(s.charAt(++i));
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static void flush(List<Span> out, StringBuilder run, Style style) {
        if (run.length() == 0) return;
        Span span = new Span(run.toString())
                .colour(style.colour)
                .bold(style.bold)
                .italic(style.italic);
        if (style.click != null) span.click(style.click, style.clickValue);
        if (style.hover != null) span.hover(style.hover);
        out.add(span);
        run.setLength(0);
    }
}
