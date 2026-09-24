package teacommontea.util.text;

import java.util.ArrayList;
import java.util.List;

public final class Span {

    public enum Click {
        RUN_COMMAND("run_command"),
        SUGGEST_COMMAND("suggest_command"),
        OPEN_URL("open_url"),
        COPY_TO_CLIPBOARD("copy_to_clipboard"),
        CHANGE_PAGE("change_page");

        private final String id;

        Click(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }

        public static Click of(String action) {
            if (action == null) return SUGGEST_COMMAND;
            String want = action.trim().toLowerCase(java.util.Locale.ROOT);
            for (Click c : values()) {
                if (c.id.equals(want)) return c;
            }
            return SUGGEST_COMMAND;
        }
    }

    private String text = "";
    private String colour;
    private boolean bold;
    private boolean italic;
    private Click clickAction;
    private String clickValue;
    private List<Span> hover;

    public Span(String text) {
        this.text = text == null ? "" : text;
    }

    public String text() {
        return text;
    }

    public String colour() {
        return colour;
    }

    public boolean bold() {
        return bold;
    }

    public boolean italic() {
        return italic;
    }

    public Click clickAction() {
        return clickAction;
    }

    public String clickValue() {
        return clickValue;
    }

    public List<Span> hover() {
        return hover;
    }

    public Span colour(String value) {
        this.colour = value;
        return this;
    }

    public Span bold(boolean value) {
        this.bold = value;
        return this;
    }

    public Span italic(boolean value) {
        this.italic = value;
        return this;
    }

    public Span click(Click action, String value) {
        this.clickAction = action;
        this.clickValue = value == null ? "" : value;
        return this;
    }

    public Span hover(List<Span> label) {
        this.hover = label == null || label.isEmpty() ? null : new ArrayList<>(label);
        return this;
    }

    public Span copyStyle(String newText) {
        Span s = new Span(newText);
        s.colour = colour;
        s.bold = bold;
        s.italic = italic;
        s.clickAction = clickAction;
        s.clickValue = clickValue;
        s.hover = hover == null ? null : new ArrayList<>(hover);
        return s;
    }
}
