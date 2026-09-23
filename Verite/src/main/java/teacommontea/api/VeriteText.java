package teacommontea.api;

import net.md_5.bungee.api.chat.BaseComponent;


import teacommontea.util.Messages;

public final class VeriteText {

    private VeriteText() {}

    private static final Messages MESSAGES = new Messages();

    public static BaseComponent[] parse(String tagged) {
        return MESSAGES.parse(tagged);
    }

    public static BaseComponent[] prefixed(String tagged) {
        return MESSAGES.prefixed(tagged);
    }

    public static String prefix() {
        return Messages.prefix();
    }

    public static void setPrefix(String prefix) {
        Messages.setPrefix(prefix);
    }
}
