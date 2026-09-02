package teacommontea.util.text;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public final class MiniToBungee {

    private MiniToBungee() {}

    private static final class Style {
        ChatColor color;
        boolean bold;
        ClickEvent click;
        HoverEvent hover;
        Style copy() {
            Style s = new Style();
            s.color = color; s.bold = bold; s.click = click; s.hover = hover;
            return s;
        }
    }

    public static BaseComponent[] parse(String mini) {
        List<BaseComponent> out = new ArrayList<>();
        Deque<Style> stack = new ArrayDeque<>();
        Style base = new Style();
        base.color = ChatColor.WHITE;
        stack.push(base);

        StringBuilder run = new StringBuilder();
        int i = 0;
        int n = mini.length();
        while (i < n) {
            char c = mini.charAt(i);
            if (c == '\\' && i + 1 < n && mini.charAt(i + 1) == 'n') {
                flush(out, run, stack.peek());
                out.add(new TextComponent("\n"));
                i += 2;
                continue;
            }
            if (c != '<') {
                run.append(c);
                i++;
                continue;
            }
            int close = mini.indexOf('>', i);
            if (close < 0) { run.append(c); i++; continue; }
            String tag = mini.substring(i + 1, close);
            String handled = applyTag(tag, out, run, stack);
            if (handled == null) {
                // not a recognised tag: keep the literal text
                run.append('<');
                i++;
            } else {
                i = close + 1;
            }
        }
        flush(out, run, stack.peek());
        if (out.isEmpty()) out.add(new TextComponent(""));
        return out.toArray(new BaseComponent[0]);
    }

    private static String applyTag(String tag, List<BaseComponent> out, StringBuilder run, Deque<Style> stack) {
        String lower = tag.toLowerCase();
        if (lower.startsWith("#") && tag.length() == 7) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            s.color = ChatColor.of("#" + tag.substring(1));
            stack.push(s);
            return tag;
        }
        if (lower.equals("reset")) {
            flush(out, run, stack.peek());
            Style s = new Style();
            s.color = ChatColor.WHITE;
            stack.push(s);
            return tag;
        }
        if (lower.equals("bold") || lower.equals("b")) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            s.bold = true;
            stack.push(s);
            return tag;
        }
        if (lower.equals("/bold") || lower.equals("/b")) {
            flush(out, run, stack.peek());
            if (stack.size() > 1) stack.pop();
            return tag;
        }
        if (lower.equals("newline") || lower.equals("br")) {
            flush(out, run, stack.peek());
            out.add(new TextComponent("\n"));
            return tag;
        }
        if (lower.startsWith("click:")) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            s.click = parseClick(tag.substring("click:".length()));
            stack.push(s);
            return tag;
        }
        if (lower.startsWith("hover:show_text:")) {
            flush(out, run, stack.peek());
            Style s = stack.peek().copy();
            s.hover = parseHover(tag.substring("hover:show_text:".length()));
            stack.push(s);
            return tag;
        }
        if (lower.equals("/click") || lower.equals("/hover")) {
            flush(out, run, stack.peek());
            if (stack.size() > 1) stack.pop();
            return tag;
        }
        return null;
    }

    private static ClickEvent parseClick(String spec) {
        int colon = spec.indexOf(':');
        String action = colon < 0 ? spec : spec.substring(0, colon);
        String value = colon < 0 ? "" : unquote(spec.substring(colon + 1));
        ClickEvent.Action a;
        switch (action.toLowerCase()) {
            case "run_command" -> a = ClickEvent.Action.RUN_COMMAND;
            case "suggest_command" -> a = ClickEvent.Action.SUGGEST_COMMAND;
            case "open_url" -> a = ClickEvent.Action.OPEN_URL;
            case "copy_to_clipboard" -> a = ClickEvent.Action.COPY_TO_CLIPBOARD;
            case "change_page" -> a = ClickEvent.Action.CHANGE_PAGE;
            default -> a = ClickEvent.Action.SUGGEST_COMMAND;
        }
        return new ClickEvent(a, value);
    }

    @SuppressWarnings("deprecation")
    private static HoverEvent parseHover(String spec) {
        String text = unquote(spec);
        BaseComponent[] label = parse(text);
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(label));
    }

    private static String unquote(String s) {
        String t = s.trim();
        if (t.length() >= 2 && t.charAt(0) == '\'' && t.charAt(t.length() - 1) == '\'') {
            return t.substring(1, t.length() - 1);
        }
        return t;
    }

    private static void flush(List<BaseComponent> out, StringBuilder run, Style style) {
        if (run.length() == 0) return;
        TextComponent t = new TextComponent(run.toString());
        t.setColor(style.color);
        t.setBold(style.bold);
        t.setItalic(false);
        if (style.click != null) t.setClickEvent(style.click);
        if (style.hover != null) t.setHoverEvent(style.hover);
        out.add(t);
        run.setLength(0);
    }
}
