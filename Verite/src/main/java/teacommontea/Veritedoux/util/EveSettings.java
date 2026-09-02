package teacommontea.veritedoux.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import teacommontea.util.Colours;

public final class EveSettings {

    public final Map<String, Boolean> languages = new HashMap<>();

    public final Map<String, Boolean> allowedLanguages = new HashMap<>();

    public final Map<String, Boolean> selfHarmByLang = new HashMap<>();
    public final Map<String, Boolean> abuseByLang = new HashMap<>();
    public final Map<String, Boolean> slurByLang = new HashMap<>();
    public final Map<String, Boolean> generalProfanityByLang = new HashMap<>();

    public final Map<String, Boolean> profanityDialects = new HashMap<>();

    public boolean homoglyphFold = true, entityStrip = true, deobfuscate = true,
            segmentation = true, fingerprint = true;
    public boolean componentEnabled = true;
    public boolean keepChatReadable = false;
    public boolean blockSpam = false;

    public boolean blockRepeat = false;
    public boolean blockRepeatExact = true;
    public boolean blockRepeatNear = true;
    public int repeatHistorySize = 5;
    public long repeatWindowMs = 5_000L;
    public double repeatSimilarityThreshold = 0.85;
    public String blockMessage = color(Colours.amp(Colours.HEX_BRAND_ACCENT) + "[" + Colours.amp(Colours.HEX_BRAND) + "Verité" + Colours.amp(Colours.HEX_BRAND_ACCENT) + "] " + Colours.amp(Colours.HEX_BRAND_ACCENT_SECONDARY) + "Your message wasn't sent. Please keep chat friendly.");
    public String repeatMessage = color(Colours.amp(Colours.HEX_BRAND_ACCENT) + "[" + Colours.amp(Colours.HEX_BRAND) + "Verité" + Colours.amp(Colours.HEX_BRAND_ACCENT) + "] " + Colours.amp(Colours.HEX_BRAND_ACCENT_SECONDARY) + "You've already sent that message. Please wait a moment before repeating yourself.");

    public double langKnownWeight = 50.0;
    public double langUnknownWeight = 0.1;

    public boolean profanityConfidenceEnabled = true;
    public double profanityConfidenceThreshold = 0.30;

    private EveSettings() {}

    public static EveSettings load(Plugin plugin) {
        EveSettings st = new EveSettings();
        File main = new File(plugin.getDataFolder(), "config.yml");
        if (!main.isFile()) {
            st.languages.put("english", true);
            return st;
        }
        try {
            YamlConfiguration y = teacommontea.util.Yaml.loadYaml(main);
            if (y.isConfigurationSection("chat.filter.general.lang")) {
                for (String lang : y.getConfigurationSection("chat.filter.general.lang").getKeys(false)) {
                    String key = lang.toLowerCase();
                    String base = "chat.filter.general.lang." + lang;
                    st.languages.put(key, true);
                    st.allowedLanguages.put(key, y.getBoolean(base + ".allowed", true));
                    st.selfHarmByLang.put(key, y.getBoolean(base + ".self.harm.detection", true));
                    st.abuseByLang.put(key, y.getBoolean(base + ".abuse.detection", true));
                    st.slurByLang.put(key, y.getBoolean(base + ".slur.detection", true));
                    readProfanity(y, st, key, base);
                }
            } else {
                st.languages.put("english", true);
            }
            st.componentEnabled = y.getBoolean("chat.filter.enabled", true);
            st.blockMessage = color(y.getString("chat.filter.block.message", st.blockMessage));
            st.repeatMessage = color(y.getString("chat.filter.repeat.message", st.repeatMessage));
            st.keepChatReadable = y.getBoolean("chat.filter.advanced.keep.chat.readable", false);
            st.blockSpam = y.getBoolean("chat.filter.advanced.block.possible.spam", false);
            st.blockRepeat = y.getBoolean("chat.filter.advanced.block.repeat.messages", false);
            st.blockRepeatExact = y.getBoolean("chat.filter.advanced.block.repeat.exact", true);
            st.blockRepeatNear = y.getBoolean("chat.filter.advanced.block.repeat.near", true);
            st.repeatHistorySize = y.getInt("chat.filter.advanced.block.repeat.history.size", st.repeatHistorySize);
            st.repeatWindowMs = y.getLong("chat.filter.advanced.block.repeat.window.ms", st.repeatWindowMs);
            st.repeatSimilarityThreshold = y.getDouble("chat.filter.advanced.block.repeat.similarity.threshold", st.repeatSimilarityThreshold);

            st.homoglyphFold = y.getBoolean("chat.filter.advanced.evasion.homoglyph", true);
            st.entityStrip = y.getBoolean("chat.filter.advanced.evasion.entity", true);
            st.deobfuscate = y.getBoolean("chat.filter.advanced.evasion.deobfuscate", true);
            st.segmentation = y.getBoolean("chat.filter.advanced.evasion.segmentation", true);
            st.fingerprint = y.getBoolean("chat.filter.advanced.evasion.fingerprint", true);
            st.langKnownWeight = y.getDouble("chat.filter.advanced.language.known.word.weight", st.langKnownWeight);
            st.langUnknownWeight = y.getDouble("chat.filter.advanced.language.unknown.word.weight", st.langUnknownWeight);
            st.profanityConfidenceEnabled = y.getBoolean("chat.filter.advanced.use.profanity.confidence.enabled", st.profanityConfidenceEnabled);
            st.profanityConfidenceThreshold = y.getDouble("chat.filter.advanced.use.profanity.confidence.profanity.confidence.threshold", st.profanityConfidenceThreshold);
        } catch (Exception e) {
            plugin.getLogger().warning("EVE settings failed to load, using defaults: " + e.getMessage());
        }
        return st;
    }

    public boolean languageEnabled(String code) {
        return languages.getOrDefault(code, false);
    }

    public boolean languageAllowed(String name) {
        return allowedLanguages.getOrDefault(name, true);
    }

    public boolean languageGateActive() {
        return !allowedLanguages.isEmpty();
    }

    public boolean selfHarmEnabled(String lang) {
        return selfHarmByLang.getOrDefault(lang, true);
    }

    public boolean abuseEnabled(String lang) {
        return abuseByLang.getOrDefault(lang, true);
    }

    public boolean slurEnabled(String lang) {
        return slurByLang.getOrDefault(lang, true);
    }

    public boolean generalProfanityEnabled(String lang) {
        return generalProfanityByLang.getOrDefault(lang, false);
    }

    private static final Map<String, Map<String, String>> DIALECT_CODES = Map.of(
            "spanish", Map.ofEntries(
                    Map.entry("spain", "es_es"), Map.entry("argentinian", "es_ar"),
                    Map.entry("chilean", "es_cl"), Map.entry("colombian", "es_co"),
                    Map.entry("mexican", "es_mx"), Map.entry("peruvian", "es_pe"),
                    Map.entry("venezuelan", "es_ve"), Map.entry("uruguayan", "es_uy"),
                    Map.entry("cuban", "es_cu"), Map.entry("dominican", "es_do"),
                    Map.entry("panamanian", "es_pa")),
            "french", Map.of("france", "fr_fr", "canadian", "fr_ca"),
            "portuguese", Map.of("european", "pt_pt", "brazilian", "pt_br"));

    private static void readProfanity(YamlConfiguration y, EveSettings st, String lang, String base) {
        String pbase = base + ".profanity";
        if (!y.isConfigurationSection(pbase)) return;
        Map<String, String> codes = DIALECT_CODES.get(lang);
        for (String region : y.getConfigurationSection(pbase).getKeys(false)) {
            String r = region.toLowerCase();
            boolean on = y.getBoolean(pbase + "." + region, false);
            if ("global".equals(r)) {
                st.generalProfanityByLang.put(lang, on);
                continue;
            }
            String code = codes == null ? null : codes.get(r);
            if (code != null) {
                st.profanityDialects.put(code, on);
            }
        }
    }

    public boolean profanityDialectEnabled(String code) {
        return code != null && profanityDialects.getOrDefault(code, false);
    }

    private static String color(String s) {
        return teacommontea.util.Colours.legacy(s);
    }
}
