package teacommontea.veritedoux.preprocess;

import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import teacommontea.veritedoux.util.EveVlex9;

public final class EveLexicon {

    static final String[] REGISTERS = {"MT", "LY", "SP", "ID", "NA", "HI", "IN", "OP", "IP"};
    private static final int R = REGISTERS.length;

    private static EveVlex9 reader;
    private static String[] langs = new String[0];

    private static final int CACHE_CAP = 1 << 16;
    private static final Object ABSENT = new Object();
    private static final Map<String, Object> POS_CACHE = new java.util.LinkedHashMap<>(CACHE_CAP, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Object> e) {
            return size() > CACHE_CAP;
        }
    };
    private static final Map<String, Object> REG_CACHE = new java.util.LinkedHashMap<>(CACHE_CAP, 0.75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<String, Object> e) {
            return size() > CACHE_CAP;
        }
    };

    private EveLexicon() {}

    public static boolean ready() {
        return reader != null && langs.length > 0;
    }

    public static String[] languages() {
        return langs.clone();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Double> position(String word) {
        Object cached;
        synchronized (POS_CACHE) {
            cached = POS_CACHE.get(word);
        }
        if (cached == ABSENT) return null;
        if (cached != null) return (Map<String, Double>) cached;
        Map<String, Double> out = decodePosition(word);
        synchronized (POS_CACHE) {
            POS_CACHE.put(word, out == null ? ABSENT : out);
        }
        return out;
    }

    private static Map<String, Double> decodePosition(String word) {
        EveVlex9 r = reader;
        if (r == null) return null;
        try {
            EveVlex9.LookupResult res = r.lookup(word.getBytes(StandardCharsets.UTF_8));
            if (res == null) return null;
            Map<String, Double> out = new HashMap<>(4);
            for (EveVlex9.Row row : res.rows) {
                String lang = langs[row.languageId()];
                long total = totalSentences(row.languageId());
                double pos = total == 0 ? 0.0 : (double) row.sentences() / total;
                out.put(lang, pos);
            }
            return out.isEmpty() ? null : out;
        } catch (Exception e) {
            return null;
        }
    }

    public static float[] registers(String word) {
        Object cached;
        synchronized (REG_CACHE) {
            cached = REG_CACHE.get(word);
        }
        if (cached == ABSENT) return null;
        if (cached != null) return ((float[]) cached).clone();
        float[] out = decodeRegisters(word);
        synchronized (REG_CACHE) {
            REG_CACHE.put(word, out == null ? ABSENT : out);
        }
        return out == null ? null : out.clone();
    }

    private static float[] decodeRegisters(String word) {
        EveVlex9 r = reader;
        if (r == null) return null;
        try {
            EveVlex9.LookupResult res = r.lookup(word.getBytes(StandardCharsets.UTF_8));
            if (res == null) return null;
            EveVlex9.Row best = null;
            long bestSum = -1;
            for (EveVlex9.Row row : res.rows) {
                long s = 0;
                for (int x : row.registers()) s += x;
                if (s > bestSum) {
                    bestSum = s;
                    best = row;
                }
            }
            if (best == null) return null;
            int[] reg = best.registers();
            float[] out = new float[R];
            for (int i = 0; i < R; i++) out[i] = reg[i] / 1000.0f;
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean knownIn(String lang, String word) {
        Map<String, Double> pos = position(word);
        return pos != null && pos.containsKey(lang);
    }

    public static Double positionIn(String lang, String word) {
        Map<String, Double> pos = position(word);
        return pos == null ? null : pos.get(lang);
    }

    public static double maxPosition(String word) {
        Map<String, Double> pos = position(word);
        if (pos == null) return 0.0;
        double mx = 0.0;
        for (double p : pos.values()) if (p > mx) mx = p;
        return mx;
    }

    private static long[] TOTALS = new long[0];

    private static long totalSentences(int languageId) {
        return languageId >= 0 && languageId < TOTALS.length ? TOTALS[languageId] : 0;
    }

    public static void load(Plugin plugin) {
        closeReader();
        langs = new String[0];
        TOTALS = new long[0];
        synchronized (POS_CACHE) {
            POS_CACHE.clear();
        }
        synchronized (REG_CACHE) {
            REG_CACHE.clear();
        }
        try {
            EveVlex9.configureNativeDir(new java.io.File(plugin.getDataFolder(), "filter/.native"));
            java.io.File f = new java.io.File(plugin.getDataFolder(), "filter/.tokenizers/tokenizer.vlex9");
            EveVlex9 r = new EveVlex9(f.toPath());
            reader = r;
            langs = r.languageNames();
            TOTALS = r.languageTotals();
            plugin.getLogger().info("EVE lexicon: opened " + langs.length + " langs (on-disk).");
        } catch (Exception e) {
            plugin.getLogger().warning("EVE lexicon: open failed: " + e.getMessage());
            closeReader();
            langs = new String[0];
            TOTALS = new long[0];
        }
    }

    private static void closeReader() {
        EveVlex9 r = reader;
        reader = null;
        if (r != null) {
            try {
                r.close();
            } catch (Exception ignored) {
            }
        }
    }
}
