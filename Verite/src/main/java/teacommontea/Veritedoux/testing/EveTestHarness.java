package teacommontea.veritedoux.testing;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import teacommontea.veritedoux.EveEntry;
import teacommontea.veritedoux.EveCommands;
import teacommontea.veritedoux.util.EveDebug;
import teacommontea.veritedoux.preprocess.EveSegment;
import teacommontea.veritedoux.preprocess.EveDialect;
import teacommontea.veritedoux.preprocess.EveRegister;

public final class EveTestHarness implements AutoCloseable {

    private final Path dataFolder;
    private final Plugin plugin;

    private EveTestHarness(Path dataFolder, Plugin plugin) {
        this.dataFolder = dataFolder;
        this.plugin = plugin;
    }

    public static EveTestHarness boot(Path dataFolder, Path tokenizersDir) throws Exception {
        return boot(dataFolder, tokenizersDir, false);
    }

    public static EveTestHarness bootAllEnabled(Path dataFolder, Path tokenizersDir) throws Exception {
        return boot(dataFolder, tokenizersDir, true);
    }

    private static EveTestHarness boot(Path dataFolder, Path tokenizersDir, boolean enableAll) throws Exception {
        Files.createDirectories(dataFolder);
        stageConfig(dataFolder);
        if (enableAll) enableEveryProfanityFlag(dataFolder.resolve("config.yml"));
        copyDir(tokenizersDir, dataFolder.resolve("filter").resolve(".tokenizers"));
        stageConfusables(dataFolder);
        Plugin plugin = mockPlugin(dataFolder.toFile());
        EveTestHarness harness = new EveTestHarness(dataFolder, plugin);
        EveCommands.loadAll(plugin);
        return harness;
    }

    private static void stageConfusables(Path dataFolder) throws Exception {
        java.io.File nativeDir = teacommontea.veritedoux.util.Eve.nativeDir();
        if (nativeDir == null) return;
        java.io.File src = new java.io.File(nativeDir, "confusables.evefold");
        if (!src.isFile()) return;
        Path dst = dataFolder.resolve("filter").resolve(".native").resolve("confusables.evefold");
        Files.createDirectories(dst.getParent());
        Files.copy(src.toPath(), dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private static void enableEveryProfanityFlag(Path settings) throws Exception {
        if (!Files.exists(settings)) return;
        java.util.List<String> lines = Files.readAllLines(settings, java.nio.charset.StandardCharsets.UTF_8);
        java.util.List<String> out = new java.util.ArrayList<>(lines.size());
        for (String line : lines) {
            if (line.contains("profanity") && line.trim().endsWith("false")) {
                out.add(line.substring(0, line.lastIndexOf("false")) + "true");
            } else {
                out.add(line);
            }
        }
        Files.write(settings, out, java.nio.charset.StandardCharsets.UTF_8);
    }

    private static void copyDir(Path src, Path dst) throws Exception {
        if (src == null || !Files.isDirectory(src)) return;
        Files.createDirectories(dst);
        try (java.util.stream.Stream<Path> s = Files.list(src)) {
            for (Path p : s.toList()) {
                if (Files.isRegularFile(p)) {
                    Files.copy(p, dst.resolve(p.getFileName().toString()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public Inspection inspect(String message) {
        String lang = EveSegment.ready() ? EveSegment.owner(message) : "";
        String dialect = EveDialect.ready(lang) ? EveDialect.dominant(lang, message) : null;
        EveEntry.Result verdict = EveEntry.check(message);
        double adLean = EveRegister.ready() ? EveRegister.adLean(message) : Double.NaN;
        return new Inspection(message, lang, dialect, verdict, adLean);
    }

    public teacommontea.veritedoux.process.EveRoute.Explain explain(String message) {
        return teacommontea.veritedoux.process.EveRoute.explain(message);
    }

    public teacommontea.veritedoux.process.EveMatcher.FingerprintReport fingerprintReport(String word, String lang) {
        return teacommontea.veritedoux.process.EveMatcher.fingerprintReport(word, lang);
    }

    public java.util.List<teacommontea.veritedoux.process.EveMatcher.MatchTrace> traceMatches(String message) {
        teacommontea.veritedoux.process.EveMatcher.traceBegin();
        EveEntry.check(message);
        return teacommontea.veritedoux.process.EveMatcher.traceEnd();
    }


    public boolean eveReady() {
        return EveEntry.testEveReady();
    }

    public void debugEnable() {
        EveDebug.reset();
        EveDebug.enable();
    }

    public void debugDisable() {
        EveDebug.disable();
    }

    public void debugReset() {
        EveDebug.reset();
    }

    public String debugReport() {
        return EveDebug.report();
    }

    public Path dataFolder() {
        return dataFolder;
    }

    @Override public void close() {
    }

    private static void stageConfig(Path dataFolder) throws Exception {
        Files.createDirectories(dataFolder);
        for (String name : CONFIG_RESOURCES) {
            copyResource(name, dataFolder.resolve(name));
        }
        for (String board : BOARD_RESOURCES) {
            copyResource("filter/" + board, dataFolder.resolve("filter").resolve(board));
        }
    }

    private static void copyResource(String resource, Path dest) throws Exception {
        try (InputStream in = EveTestHarness.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) return;
            Files.createDirectories(dest.getParent());
            Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Plugin mockPlugin(File dataFolder) {
        Logger logger = Logger.getLogger("VeriteTest");
        return (Plugin) Proxy.newProxyInstance(
                EveTestHarness.class.getClassLoader(),
                new Class<?>[]{Plugin.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getDataFolder" -> dataFolder;
                    case "getLogger" -> logger;
                    case "toString" -> "VeriteTestPlugin";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == (args == null ? null : args[0]);
                    default -> defaultReturn(method.getReturnType());
                });
    }

    private static Object defaultReturn(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == void.class) return null;
        if (type == char.class) return (char) 0;
        return 0;
    }

    private static final String[] CONFIG_RESOURCES = {
            "config.yml"
    };

    private static final String[] BOARD_RESOURCES = {
            "English/main_en.eve", "English/profanity_en.eve",
            "Spanish/main_es.eve", "Spanish/profanity_es.eve",
            "French/main_fr.eve", "French/profanity_fr_FR.eve", "French/profanity_fr_CA.eve",
            "Italian/main_it.eve", "Italian/profanity_it.eve",
            "Portuguese/main_pt.eve", "Portuguese/profanity_pt.eve",
            "German/main_de.eve", "German/profanity_de.eve",
            "Spanish/profanity_es_ES.eve", "Spanish/profanity_es_AR.eve", "Spanish/profanity_es_CL.eve",
            "Spanish/profanity_es_CO.eve", "Spanish/profanity_es_MX.eve"
    };

    public record Inspection(String message, String language, String dialect,
                      EveEntry.Result verdict, double adLean) {}
}
