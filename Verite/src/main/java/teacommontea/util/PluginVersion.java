package teacommontea.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public final class PluginVersion {

    private static final String VERSION = read();

    private PluginVersion() {}

    public static String get() {
        return VERSION;
    }

    private static String read() {
        try (InputStream in = PluginVersion.class.getClassLoader().getResourceAsStream("plugin.yml")) {
            if (in == null) return "unknown";
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return YamlConfiguration.loadConfiguration(reader).getString("version", "unknown");
            }
        } catch (java.io.IOException e) {
            return "unknown";
        }
    }
}
