package teacommontea.veritechasse.Reality;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class CheckRegistry {

    public static final String PACKAGE = "teacommontea.veritechasse.Reality.Checks";

    public static final String PACKAGE_PATH = PACKAGE.replace('.', '/');

    public static final String CLASS_SUFFIX = ".class";

    private static List<Check> cached;

    private CheckRegistry() {
    }

    public static List<Check> all() {
        List<Check> known = cached;
        if (known == null) {
            known = discover();
            cached = known;
        }
        return known;
    }

    public static int count() {
        return all().size();
    }

    public static List<String> keys() {
        List<String> keys = new ArrayList<>();
        for (Check check : all()) {
            keys.add(check.key());
        }
        Collections.sort(keys);
        return keys;
    }

    private static List<Check> discover() {
        List<Check> checks = new ArrayList<>();
        for (String simpleName : classNames()) {
            Check check = instantiate(simpleName);
            if (check != null) {
                checks.add(check);
            }
        }
        return Collections.unmodifiableList(checks);
    }

    private static Check instantiate(String simpleName) {
        try {
            Class<?> type = Class.forName(PACKAGE + "." + simpleName);
            if (!Check.class.isAssignableFrom(type) || type.isInterface()) {
                return null;
            }
            Object created = type.getDeclaredConstructor().newInstance();
            return (Check) created;
        } catch (ReflectiveOperationException | RuntimeException failure) {
            return null;
        }
    }

    private static Iterable<String> classNames() {
        TreeSet<String> names = new TreeSet<>();
        try {
            ClassLoader loader = CheckRegistry.class.getClassLoader();
            Enumeration<URL> resources = loader.getResources(PACKAGE_PATH);
            while (resources.hasMoreElements()) {
                collect(resources.nextElement(), names);
            }
        } catch (IOException failure) {
            return names;
        }
        return names;
    }

    private static void collect(URL resource, TreeSet<String> names) {
        String protocol = resource.getProtocol();
        if ("jar".equals(protocol)) {
            collectFromJar(resource, names);
            return;
        }
        if ("file".equals(protocol)) {
            collectFromDirectory(resource, names);
        }
    }

    private static void collectFromJar(URL resource, TreeSet<String> names) {
        String path = resource.getPath();
        int separator = path.indexOf('!');
        if (separator < 0) {
            return;
        }
        String jarPath = path.substring(0, separator);
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring("file:".length());
        }
        try (JarFile jar = new JarFile(java.net.URLDecoder.decode(jarPath, "UTF-8"))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.startsWith(PACKAGE_PATH) || !name.endsWith(CLASS_SUFFIX)) {
                    continue;
                }
                addSimpleName(name.substring(PACKAGE_PATH.length() + 1), names);
            }
        } catch (IOException failure) {
            return;
        }
    }

    private static void collectFromDirectory(URL resource, TreeSet<String> names) {
        Path directory;
        try {
            directory = Paths.get(resource.toURI());
        } catch (java.net.URISyntaxException failure) {
            return;
        }
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (Stream<Path> entries = Files.list(directory)) {
            entries.forEach(entry -> {
                String name = entry.getFileName().toString();
                if (name.endsWith(CLASS_SUFFIX)) {
                    addSimpleName(name, names);
                }
            });
        } catch (IOException failure) {
            return;
        }
    }

    private static void addSimpleName(String entryName, TreeSet<String> names) {
        if (entryName.indexOf('/') >= 0 || entryName.indexOf('$') >= 0) {
            return;
        }
        names.add(entryName.substring(0, entryName.length() - CLASS_SUFFIX.length()));
    }
}
