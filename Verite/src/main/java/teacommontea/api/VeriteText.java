package teacommontea.api;

import net.md_5.bungee.api.chat.BaseComponent;


import teacommontea.util.Messages;

public final class VeriteText {

    private VeriteText() {}

    private static final Messages MESSAGES = new Messages();

    public static BaseComponent[] parse(String miniMessage) {
        return MESSAGES.parse(miniMessage);
    }

    public static BaseComponent[] prefixed(String miniMessage) {
        return MESSAGES.prefixed(miniMessage);
    }

    public static String prefix() {
        return Messages.prefix();
    }

    public static void setPrefix(String prefix) {
        Messages.setPrefix(prefix);
    }
}
