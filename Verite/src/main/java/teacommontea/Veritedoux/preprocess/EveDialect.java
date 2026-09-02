package teacommontea.veritedoux.preprocess;

import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import teacommontea.veritedoux.util.EveVlex9;

public final class EveDialect {

    private static final Map<String, String> LANG_CODE = Map.of(
            "spanish", "es", "french", "fr", "portuguese", "pt");

    private static final Map<String, String[]> DIALECTS = Map.of(
            "es", new String[]{"es_ar", "es_mx", "es_es", "es_co", "es_cl", "es_pe", "es_ve", "es_uy", "es_cu", "es_do", "es_pa"},
            "fr", new String[]{"fr_fr", "fr_ca"},
            "pt", new String[]{"pt_pt", "pt_br"});

    private static final Map<String, EveDialect> REGISTRY = new HashMap<>();
    private static double confidenceFloor = 0.30;

    private final String[] dialects;
    private final EveVlex9 reader;

    private EveDialect(String[] dialects, EveVlex9 reader) {
        this.dialects = dialects;
        this.reader = reader;
    }

    public static void configure(double floor) {
        if (floor >= 0 && floor <= 1) confidenceFloor = floor;
    }

    public static void clear() {
        for (EveDialect d : REGISTRY.values()) {
            if (d.reader != null) {
                try {
                    d.reader.close();
                } catch (Exception ignored) {
                }
            }
        }
        REGISTRY.clear();
    }

    public static boolean load(Plugin plugin, String language) {
        String code = LANG_CODE.get(language);
        if (code == null) return false;
        java.io.File vlexFile = new java.io.File(plugin.getDataFolder(),
                "filter/.tokenizers/" + code + "_dialect.vlex9");
        if (!vlexFile.isFile()) return false;
        String[] dialects = DIALECTS.get(code);
        if (dialects == null) return false;
        try {
            EveVlex9.configureNativeDir(new java.io.File(plugin.getDataFolder(), "filter/.native"));
            EveVlex9 reader = new EveVlex9(vlexFile.toPath());
            EveDialect d = new EveDialect(dialects, reader);
            REGISTRY.put(language, d);
            plugin.getLogger().info("EVE dialect (" + language + "): opened over "
                    + dialects.length + " dialects (on-disk).");
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("EVE dialect (" + language + "): load failed: "
                    + e.getMessage());
            return false;
        }
    }

    public static boolean ready(String language) {
        EveDialect d = REGISTRY.get(language);
        return d != null && d.reader != null && d.dialects.length > 0;
    }

    private float[] rates(String word) {
        try {
            EveVlex9.LookupResult res = reader.lookup(word.getBytes(StandardCharsets.UTF_8));
            if (res == null || res.rows.length == 0) return null;
            int[] regs = res.rows[0].registers();
            float[] out = null;
            for (int slot = 0; slot < 4; slot++) {
                int di = regs[slot * 2];
                int nb = regs[slot * 2 + 1];
                if (di <= 0 || di - 1 >= dialects.length || nb == 0) continue;
                if (out == null) out = new float[dialects.length];
                out[di - 1] = dequantize(nb);
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    public static String dominant(String language, String line) {
        EveDialect d = REGISTRY.get(language);
        if (d == null || line == null || line.isEmpty()) return null;
        int L = d.dialects.length;
        double[] logp = new double[L];
        int voted = 0;
        for (String tok : line.split("\\s+")) {
            String w = fold(tok);
            if (w.length() < 2) continue;
            float[] rates = d.rates(w);
            if (rates == null) continue;
            voted++;
            for (int i = 0; i < L; i++) {
                logp[i] += Math.log(Math.max(rates[i], MIN_RATE));
            }
        }
        if (voted == 0) return null;

        double[] p = softmax(logp);
        int best = 0, second = -1;
        for (int i = 1; i < L; i++) if (p[i] > p[best]) best = i;
        for (int i = 0; i < L; i++) {
            if (i == best) continue;
            if (second < 0 || p[i] > p[second]) second = i;
        }
        double margin = second < 0 ? p[best] : p[best] - p[second];
        if (margin < confidenceFloor) return null;
        return d.dialects[best];
    }

    private static final double MIN_RATE = 1e-3;

    private static double[] softmax(double[] logp) {
        double m = Double.NEGATIVE_INFINITY;
        for (double x : logp) m = Math.max(m, x);
        double[] p = new double[logp.length];
        double s = 0;
        for (int i = 0; i < logp.length; i++) {
            p[i] = Math.exp(logp[i] - m);
            s += p[i];
        }
        if (s > 0) for (int i = 0; i < p.length; i++) p[i] /= s;
        return p;
    }

    private static float dequantize(int nibble) {
        double v = (nibble - 1) / 14.0 * 7.7 - 3.0;
        return (float) Math.pow(10.0, v);
    }


    private static String fold(String s) {
        return teacommontea.veritedoux.util.EveText.foldAccents(s.toLowerCase());
    }
}

