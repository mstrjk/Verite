package teacommontea.veritechasse.Vanilla.Enchantments.Support;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Catalogue {

    private static final String PACKAGE = "teacommontea.veritechasse.Vanilla.Enchantments";
    private static final String PACKAGE_PATH = PACKAGE.replace('.', '/');

    private static Set<String> cached;

    private Catalogue() {
    }

    public static Set<String> keys() {
        Set<String> known = cached;
        if (known == null) {
            known = discover();
            cached = known;
        }
        return known;
    }

    private static Set<String> discover() {
        Set<String> keys = new TreeSet<>();

        for (String simpleName : classNames()) {
            String key = keyOf(simpleName);
            if (key != null) {
                keys.add(key);
            }
        }
        return Set.copyOf(keys);
    }

    private static Set<String> classNames() {
        Set<String> names = new TreeSet<>();

        URL location = Catalogue.class.getProtectionDomain().getCodeSource() == null
            ? null
            : Catalogue.class.getProtectionDomain().getCodeSource().getLocation();
        if (location == null) {
            return names;
        }

        Path path;
        try {
            path = Paths.get(location.toURI());
        } catch (Exception failed) {
            return names;
        }

        if (!path.toString().endsWith(".jar")) {
            return names;
        }

        try (JarFile jar = new JarFile(path.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.startsWith(PACKAGE_PATH + "/") || !name.endsWith(".class")) {
                    continue;
                }

                String tail = name.substring(PACKAGE_PATH.length() + 1, name.length() - ".class".length());
                if (tail.indexOf('/') >= 0 || tail.indexOf('$') >= 0) {
                    continue;
                }
                names.add(tail);
            }
        } catch (IOException ignored) {
            return names;
        }

        return names;
    }

    private static String keyOf(String simpleName) {
        try {
            Class<?> type = Class.forName(PACKAGE + "." + simpleName);
            Object value = type.getField("KEY").get(null);
            return value instanceof String text ? text : null;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}
