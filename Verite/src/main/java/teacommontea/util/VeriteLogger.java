package teacommontea.util;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class VeriteLogger extends Logger {

    private static final String PREFIX =
            ConsoleColours.paint(ConsoleColours.BRAND_ACCENT, "[")
            + ConsoleColours.paint(ConsoleColours.BRAND, "Verité")
            + ConsoleColours.paint(ConsoleColours.BRAND_ACCENT, "]")
            + " ";

    private VeriteLogger(org.bukkit.plugin.Plugin plugin) {
        super(plugin.getServer().getLogger().getName(), null);
        setParent(plugin.getServer().getLogger());
        setLevel(Level.ALL);
    }

    public static Logger of(org.bukkit.plugin.Plugin plugin) {
        return new VeriteLogger(plugin);
    }

    public static String prefix() {
        return PREFIX;
    }

    @Override
    public void log(LogRecord record) {
        record.setMessage(PREFIX + record.getMessage());
        super.log(record);
    }
}
