package teacommontea.api;

import teacommontea.util.text.Span;

import java.util.List;


import teacommontea.util.Messages;

public final class VeriteText {

    private VeriteText() {}

    private static final Messages MESSAGES = new Messages();

    public static List<Span> parse(String tagged) {
        return MESSAGES.parse(tagged);
    }

    public static List<Span> prefixed(String tagged) {
        return MESSAGES.prefixed(tagged);
    }

    public static String prefix() {
        return Messages.prefix();
    }

    public static void setPrefix(String prefix) {
        Messages.setPrefix(prefix);
    }
}
