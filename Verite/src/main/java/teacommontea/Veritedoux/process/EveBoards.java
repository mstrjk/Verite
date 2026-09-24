package teacommontea.veritedoux.process;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.Plugin;
import teacommontea.veritedoux.preprocess.EveSegment;
import teacommontea.veritedoux.preprocess.EveDialect;
import teacommontea.veritedoux.process.SymbolBoard;
import teacommontea.veritedoux.EveEntry;
import teacommontea.veritedoux.util.EveSettings;

public final class EveBoards {

    private record Config(String file, String lang) {}
    private static final Config[] CONFIGS = {
            new Config("filter/English/main_en.eve", "english"),
            new Config("filter/Spanish/main_es.eve", "spanish"),
            new Config("filter/French/main_fr.eve", "french"),
            new Config("filter/Italian/main_it.eve", "italian"),
            new Config("filter/Portuguese/main_pt.eve", "portuguese"),
            new Config("filter/German/main_de.eve", "german"),
            new Config("filter/English/profanity_en.eve", "english"),
            new Config("filter/German/profanity_de.eve", "german"),
            new Config("filter/Italian/profanity_it.eve", "italian"),
            new Config("filter/Spanish/profanity_es.eve", "spanish"),
    };

    private record DialectBoard(String lang, String code, String file) {}
    private static final DialectBoard[] DIALECT_BOARDS_SRC = {
            new DialectBoard("spanish", "es_es", "filter/Spanish/profanity_es_ES.eve"),
            new DialectBoard("spanish", "es_ar", "filter/Spanish/profanity_es_AR.eve"),
            new DialectBoard("spanish", "es_cl", "filter/Spanish/profanity_es_CL.eve"),
            new DialectBoard("spanish", "es_co", "filter/Spanish/profanity_es_CO.eve"),
            new DialectBoard("spanish", "es_mx", "filter/Spanish/profanity_es_MX.eve"),
            new DialectBoard("spanish", "es_pe", "filter/Spanish/profanity_es_PE.eve"),
            new DialectBoard("spanish", "es_ve", "filter/Spanish/profanity_es_VE.eve"),
            new DialectBoard("spanish", "es_uy", "filter/Spanish/profanity_es_UY.eve"),
            new DialectBoard("spanish", "es_cu", "filter/Spanish/profanity_es_CU.eve"),
            new DialectBoard("spanish", "es_do", "filter/Spanish/profanity_es_DO.eve"),
            new DialectBoard("spanish", "es_pa", "filter/Spanish/profanity_es_PA.eve"),
            new DialectBoard("french", "fr_fr", "filter/French/profanity_fr_FR.eve"),
            new DialectBoard("french", "fr_ca", "filter/French/profanity_fr_CA.eve"),
            new DialectBoard("portuguese", "pt_pt", "filter/Portuguese/profanity_pt_PT.eve"),
            new DialectBoard("portuguese", "pt_br", "filter/Portuguese/profanity_pt_BR.eve"),
    };

    private static final String[] DIALECT_LANGS = {"spanish", "french", "portuguese"};

    private static teacommontea.veritedoux.util.Eve EVE;
    private static final Map<String, teacommontea.veritedoux.util.Eve> MAIN_BOARDS = new HashMap<>();
    private static final Map<String, teacommontea.veritedoux.util.Eve> GENERAL_PROFANITY = new HashMap<>();
    private static final Map<String, teacommontea.veritedoux.util.Eve> DIALECT_BOARDS = new HashMap<>();
    private static final Map<String, String> DIALECT_TO_LANG = new HashMap<>();
    private static SymbolBoard SYMBOLS;
    private static int maxWords = 1;

    private EveBoards() {}

    public static teacommontea.veritedoux.util.Eve eve() { return EVE; }
    public static Map<String, teacommontea.veritedoux.util.Eve> mainBoards() { return MAIN_BOARDS; }
    public static Map<String, teacommontea.veritedoux.util.Eve> generalProfanity() { return GENERAL_PROFANITY; }
    public static Map<String, teacommontea.veritedoux.util.Eve> dialectBoards() { return DIALECT_BOARDS; }
    public static Map<String, String> dialectToLang() { return DIALECT_TO_LANG; }
    public static SymbolBoard symbols() { return SYMBOLS; }
    public static int maxWords() { return maxWords; }

    public static void clear() {
        EVE = null;
        SYMBOLS = null;
        MAIN_BOARDS.clear();
        GENERAL_PROFANITY.clear();
        DIALECT_BOARDS.clear();
        DIALECT_TO_LANG.clear();
        EveDialect.clear();
        maxWords = 1;
    }

    public static void load(Plugin plugin, EveSettings settings) {
        maxWords = 1;
        for (Config config : CONFIGS) {
            String body = readConfigText(plugin, config.file());
            if (body == null) continue;
            EveSegment.setVowels(config.lang(), eveVowels(config.lang()));
            maxWords = Math.max(maxWords, eveMaxWords(body));
        }
        EVE = loadEve(plugin, settings);
        loadDialectBoards(plugin, settings);
        loadSymbolBoard(plugin);
    }

    private static teacommontea.veritedoux.util.Eve loadEve(Plugin plugin, EveSettings settings) {
        if (!teacommontea.veritedoux.util.Eve.nativeAvailable()) {
            plugin.getLogger().warning(teacommontea.util.ConsoleColours.bad(teacommontea.util.Lang.of("filter.library.missing"))
                    + teacommontea.util.Trace.of(teacommontea.veritedoux.util.Eve.nativeError()));
            return null;
        }
        MAIN_BOARDS.clear();
        GENERAL_PROFANITY.clear();
        StringBuilder merged = new StringBuilder();
        for (Config config : CONFIGS) {
            boolean isProfanity = config.file().contains("profanity");
            String lang = config.lang();
            if (isProfanity && !settings.generalProfanityEnabled(lang)) {
                continue;
            }
            String body = readConfigText(plugin, config.file());
            if (body == null || body.isBlank()) continue;
            try {
                teacommontea.veritedoux.util.Eve board = teacommontea.veritedoux.util.Eve.parse(body);
                if (isProfanity) {
                    GENERAL_PROFANITY.put(lang, board);
                } else {
                    MAIN_BOARDS.put(lang, board);
                }
                maxWords = Math.max(maxWords, eveMaxWords(body));
                merged.append(body).append('\n');
            } catch (Exception e) {
                plugin.getLogger().warning(teacommontea.util.ConsoleColours.bad(teacommontea.util.Lang.of("filter.board.parse.failed", "file", config.file())) + teacommontea.util.Trace.of(e));
            }
        }
        if (merged.length() == 0) {
            plugin.getLogger().warning(teacommontea.util.ConsoleColours.bad(teacommontea.util.Lang.of("filter.boards.none")));
            return null;
        }
        try {
            teacommontea.veritedoux.util.Eve eve = teacommontea.veritedoux.util.Eve.parse(merged.toString());
            plugin.getLogger().info(teacommontea.util.Lang.of((MAIN_BOARDS.size() == 1 ? "filter.boards.loaded.one.language" : "filter.boards.loaded.many.languages")
                    + (eve.ruleCount() == 1 ? ".one" : ""),
                    "count", teacommontea.util.ConsoleColours.note(eve.ruleCount()),
                    "languages", MAIN_BOARDS.size()));
            return eve;
        } catch (Exception e) {
            plugin.getLogger().warning(teacommontea.util.ConsoleColours.bad(teacommontea.util.Lang.of("filter.boards.read.failed")) + teacommontea.util.Trace.of(e));
            return null;
        }
    }

    private static void loadDialectBoards(Plugin plugin, EveSettings settings) {
        DIALECT_BOARDS.clear();
        DIALECT_TO_LANG.clear();
        EveDialect.clear();
        if (settings == null || !teacommontea.veritedoux.util.Eve.nativeAvailable()) {
            return;
        }
        double floor = settings.profanityConfidenceEnabled ? settings.profanityConfidenceThreshold : 0.0;
        EveDialect.configure(floor);

        for (String lang : DIALECT_LANGS) {
            if (!EveDialect.load(plugin, lang)) continue;

            int loaded = 0;
            for (DialectBoard db : DIALECT_BOARDS_SRC) {
                if (!db.lang().equals(lang)) continue;
                if (!settings.profanityDialectEnabled(db.code())) continue;
                String body = readConfigText(plugin, db.file());
                if (body == null || body.isBlank()) continue;
                try {
                    teacommontea.veritedoux.util.Eve board = teacommontea.veritedoux.util.Eve.parse(body);
                    DIALECT_BOARDS.put(db.code(), board);
                    DIALECT_TO_LANG.put(db.code(), lang);
                    maxWords = Math.max(maxWords, eveMaxWords(body));
                    loaded++;
                } catch (Exception e) {
                    plugin.getLogger().warning(teacommontea.util.ConsoleColours.bad(teacommontea.util.Lang.of("filter.dialect.parse.failed",
                            "code", db.code())) + teacommontea.util.Trace.of(e));
                }
            }
            if (loaded > 0) {
                plugin.getLogger().info(teacommontea.util.Lang.counted("filter.dialect.loaded", loaded,
                        "count", teacommontea.util.ConsoleColours.note(loaded),
                        "language", teacommontea.util.Lang.of("language." + lang)));
            }
        }
    }

    private static void loadSymbolBoard(Plugin plugin) {
        SYMBOLS = null;
        if (!teacommontea.veritedoux.util.Eve.nativeAvailable()) {
            return;
        }
        String body = readConfigText(plugin, "filter/symbols.eve");
        if (body == null || body.isBlank()) {
            return;
        }
        SymbolBoard board = SymbolBoard.parse(body);
        if (board == null) {
            plugin.getLogger().warning(teacommontea.util.ConsoleColours.bad(teacommontea.util.Lang.of("filter.symbols.empty")));
            return;
        }
        SYMBOLS = board;
        plugin.getLogger().info(teacommontea.util.Lang.counted("filter.symbols.loaded", board.ruleCount(),
                "count", teacommontea.util.ConsoleColours.note(board.ruleCount())));
    }

    private static String eveVowels(String lang) {
        return switch (lang) {
            case "english", "french", "german" -> "aeiouy";
            default -> "aeiou";
        };
    }

    private static int eveMaxWords(String body) {
        int max = 1, gaps = 1;
        for (String line : body.split("\n")) {
            String t = line.strip();
            if (t.equalsIgnoreCase("GAP")) gaps++;
            else if (t.equalsIgnoreCase("HEAR rule") || t.equalsIgnoreCase("MATCH rule")) {
                max = Math.max(max, gaps); gaps = 1;
            } else {
                for (String op : t.split("\\s+OR\\s+|\\s+or\\s+|[()]")) {
                    int words = op.trim().isEmpty() ? 0 : op.trim().split("\\s+").length;
                    max = Math.max(max, words);
                }
            }
        }
        return Math.max(max, gaps);
    }

    private static String readConfigText(Plugin plugin, String name) {
        try {
            java.io.File f = new java.io.File(plugin.getDataFolder(), name);
            if (!f.isFile()) return null;
            return new String(java.nio.file.Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
