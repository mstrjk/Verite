package teacommontea.veritedoux.preprocess;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import teacommontea.veritedoux.preprocess.EveLexicon;

public final class EveLang {

    private static final String[] PRIOR_LANGS = {
            "en", "es", "fr", "it", "pt", "de"};
    private static final Map<String, String> CODE = Map.ofEntries(
            Map.entry("english", "en"), Map.entry("spanish", "es"), Map.entry("french", "fr"),
            Map.entry("italian", "it"), Map.entry("portuguese", "pt"), Map.entry("german", "de"));

    private static double FIT_KNOWN = 50.0;
    private static double FIT_UNKNOWN = 0.1;

    private static final Map<String, Double> PRIOR_LANG = new HashMap<>(Map.ofEntries(
            Map.entry("en", 0.464), Map.entry("es", 0.170), Map.entry("de", 0.122),
            Map.entry("fr", 0.096), Map.entry("pt", 0.080), Map.entry("it", 0.068)));

    private EveLang() {}

    public static void configure(double known, double unknown) {
        if (known > 0) FIT_KNOWN = known;
        if (unknown > 0) FIT_UNKNOWN = unknown;
    }

    public static void load(Plugin plugin) {
        if (!EveLexicon.ready()) {
            plugin.getLogger().info("EVE lang: lexicon not ready, prior disabled");
            return;
        }
        plugin.getLogger().info("EVE lang: priors served on-disk per token.");
    }

    private static final double RATE_SCALE = 1000.0;

    private static float[] priorFor(String tok, Map<String, float[]> cache) {
        if (cache != null && cache.containsKey(tok)) return cache.get(tok);
        float[] prior = priorForUncached(tok);
        if (cache != null) cache.put(tok, prior);
        return prior;
    }

    private static float[] priorForUncached(String tok) {
        if (!EveLexicon.ready()) return null;
        java.util.Map<String, Double> pos = EveLexicon.position(tok);
        if (pos == null || pos.isEmpty()) return null;
        float[] rates = new float[PRIOR_LANGS.length];
        boolean any = false;
        for (java.util.Map.Entry<String, Double> e : pos.entrySet()) {
            String code = CODE.get(e.getKey());
            if (code == null) continue;
            for (int j = 0; j < PRIOR_LANGS.length; j++) {
                if (PRIOR_LANGS[j].equals(code)) {
                    rates[j] = (float) (e.getValue() * RATE_SCALE);
                    any = true;
                    break;
                }
            }
        }
        return any ? rates : null;
    }

    private static double[] logEvidence(String tok, List<String> langs) {
        return logEvidence(tok, langs, null);
    }

    private static double[] logEvidence(String tok, List<String> langs, Map<String, float[]> priors) {
        double[] logp = new double[langs.size()];
        float[] prior = priorFor(tok, priors);
        double priorW = 1.0 - markedness(tok, priors);
        for (int i = 0; i < langs.size(); i++) {
            String lang = langs.get(i);
            double rate;
            if (prior != null) {
                rate = priorRate(prior, lang);
            } else {
                rate = EveSegment.knownIn(lang, tok) ? FIT_KNOWN : FIT_UNKNOWN;
            }
            logp[i] = Math.log(rate) + priorW * Math.log(priorLang(lang));
        }
        return logp;
    }

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

    private static double priorLang(String lang) {
        Double p = PRIOR_LANG.get(CODE.get(lang));
        return p != null ? p : 0.01;
    }

    public static void setPriorLang(Map<String, Double> overrides) {
        if (overrides != null) PRIOR_LANG.putAll(overrides);
    }

    private static double priorRate(float[] prior, String lang) {
        String code = CODE.get(lang);
        for (int i = 0; i < PRIOR_LANGS.length; i++) {
            if (PRIOR_LANGS[i].equals(code)) return Math.max(prior[i], FIT_UNKNOWN);
        }
        return FIT_UNKNOWN;
    }

    private static final double MARK_LO = 1.0;
    private static final double MARK_HI = 500.0;

    private static double markedness(String tok, Map<String, float[]> priors) {
        float[] prior = priorFor(tok, priors);
        if (prior == null) return 1.0;
        double mx = 0;
        for (float r : prior) if (r > mx) mx = r;
        if (mx <= MARK_LO) return 1.0;
        if (mx >= MARK_HI) return 0.0;

        return 1.0 - Math.log(mx) / Math.log(MARK_HI);
    }

    private static final double MISFIT_EJECT = 2.0;

    public static List<double[]> label(String line) {
        return label(line, null);
    }

    public static List<double[]> label(String line, Map<String, float[]> priors) {
        List<String> langs = EveSegment.languages();
        List<double[]> out = new ArrayList<>();
        String[] toks = line.split("\\s+");
        List<String> words = new ArrayList<>();
        for (String t : toks) if (!t.isEmpty()) words.add(t);
        if (langs.isEmpty() || words.isEmpty()) return out;

        int L = langs.size();
        double[][] ev = new double[words.size()][];
        double[] mark = new double[words.size()];
        blockWeight = new double[words.size()];
        for (int i = 0; i < words.size(); i++) {
            String w = words.get(i);
            ev[i] = logEvidence(w, langs, priors);

            if (w.length() < 2) {
                java.util.Arrays.fill(ev[i], 0.0);
                mark[i] = 0.0;
                blockWeight[i] = 0.0;
            } else {
                mark[i] = markedness(w, priors);
                blockWeight[i] = BLOCK_WEIGHT_FLOOR + (1.0 - BLOCK_WEIGHT_FLOOR) * mark[i];
            }
        }

        double[][] result = new double[words.size()][];

        List<Integer> remaining = new ArrayList<>();
        int worked = Math.min(words.size(), MAX_LABEL_TOKENS);
        for (int i = 0; i < worked; i++) remaining.add(i);
        double[] uniform = new double[L];
        java.util.Arrays.fill(uniform, 1.0 / L);

        int blockCount = 0;
        double[] dominant = null;
        while (!remaining.isEmpty()) {
            if (++blockCount > MAX_BLOCKS) {

                for (int tok : remaining) if (result[tok] == null) result[tok] = dominant != null ? dominant : uniform;
                break;
            }
            List<Integer> block = new ArrayList<>(remaining);
            List<Integer> ejected = new ArrayList<>();
            double[] dist = blockDist(block, ev, L);

            for (int pass = 0; pass < MAX_PURIFY_PASSES && block.size() > 1; pass++) {
                int win = argmax(dist);
                List<Integer> keep = new ArrayList<>(block.size());
                boolean ejectedAny = false;
                for (int tok : block) {
                    if (mark[tok] < 0.5 && (ev[tok][argmax(ev[tok])] - ev[tok][win]) > MISFIT_EJECT) {
                        ejected.add(tok);
                        ejectedAny = true;
                    } else {
                        keep.add(tok);
                    }
                }
                if (!ejectedAny || keep.isEmpty()) break;
                block = keep;
                dist = blockDist(block, ev, L);
            }

            for (int tok : block) result[tok] = dist;

            if (dominant == null && conf(dist) > 1.0 / L + 1e-6) dominant = dist;

            if (ejected.size() == remaining.size()) {

                for (int tok : ejected) if (result[tok] == null) result[tok] = uniform;
                break;
            }
            remaining = ejected;
        }

        double[] tail = dominant != null ? dominant : uniform;
        for (int i = 0; i < words.size(); i++) {
            if (result[i] != null) out.add(result[i]);
            else out.add(i >= worked ? tail : uniform);
        }
        return out;
    }

    private static double[] blockDist(List<Integer> block, double[][] ev, int L) {
        double[] logp = new double[L];
        double wsum = 0;
        for (int tok : block) {
            double w = blockWeight[tok];
            for (int k = 0; k < L; k++) logp[k] += w * ev[tok][k];
            wsum += w;
        }

        if (wsum > 1e-9) for (int k = 0; k < L; k++) logp[k] /= wsum;
        return softmax(logp);
    }

    private static double[] blockWeight;
    private static final double BLOCK_WEIGHT_FLOOR = 0.02;

    private static final int MAX_PURIFY_PASSES = 4;
    private static final int MAX_BLOCKS = 6;
    private static final int MAX_LABEL_TOKENS = 64;

    private static double conf(double[] dist) {
        double m = 0; for (double x : dist) if (x > m) m = x; return m;
    }

    private static int argmax(double[] v) {
        int b = 0; for (int k = 1; k < v.length; k++) if (v[k] > v[b]) b = k; return b;
    }

    public static String tokenLang(List<double[]> labelled, int i) {
        List<String> langs = EveSegment.languages();
        if (i < 0 || i >= labelled.size() || langs.isEmpty()) return "";
        double[] p = labelled.get(i);
        int best = 0;
        for (int k = 1; k < p.length; k++) if (p[k] > p[best]) best = k;

        if (p[best] <= 1.0 / langs.size() + 1e-6) return "";
        return langs.get(best);
    }

    public record Span(String lang, int start, int end) {}

    static List<Span> partitions(String line) {
        List<double[]> lab = label(line);
        List<Span> spans = new ArrayList<>();
        String cur = null;
        int start = 0;
        for (int i = 0; i < lab.size(); i++) {
            String lg = tokenLang(lab, i);
            if (cur == null) {
                cur = lg;
                start = i;
            } else if (!lg.equals(cur)) {
                spans.add(new Span(cur, start, i));
                cur = lg;
                start = i;
            }
        }
        if (cur != null) spans.add(new Span(cur, start, lab.size()));
        return spans;
    }

    private static final double GATE_STRONG_CONF = 0.75;
    private static final double GATE_RUN_CONF = 0.55;
    private static final int GATE_RUN_LEN = 2;

    public static java.util.Set<String> presentLanguages(String line) {
        return presentLanguages(line, label(line));
    }

    public static java.util.Set<String> presentLanguages(String line, List<double[]> lab) {
        List<String> langs = EveSegment.languages();
        java.util.Set<String> present = new java.util.LinkedHashSet<>();
        if (langs.isEmpty()) return present;
        String[] toks = line.split("\\s+");
        List<String> words = new ArrayList<>();
        for (String t : toks) if (!t.isEmpty()) words.add(t);
        int n = Math.min(lab.size(), words.size());

        for (int i = 0; i < n; i++) {
            double[] p = lab.get(i);
            int best = argmax(p);
            if (p[best] >= GATE_STRONG_CONF && words.get(i).length() >= 3) {
                present.add(langs.get(best));
            }
        }

        String runLang = null;
        int run = 0;
        for (int i = 0; i < n; i++) {
            double[] p = lab.get(i);
            int best = argmax(p);
            String lg = p[best] >= GATE_RUN_CONF ? langs.get(best) : null;
            if (lg != null && lg.equals(runLang)) {
                run++;
            } else {
                runLang = lg;
                run = lg != null ? 1 : 0;
            }
            if (run >= GATE_RUN_LEN && runLang != null) {
                present.add(runLang);
            }
        }
        return present;
    }

    public record Confidence(String lang, double share, double margin) {}

    public static Confidence confidence(String line) {
        return confidence(label(line));
    }

    public static Confidence confidence(List<double[]> lab) {
        List<String> langs = EveSegment.languages();
        if (langs.isEmpty() || lab.isEmpty()) return new Confidence("", 0, 0);
        double[] agg = new double[langs.size()];
        for (double[] p : lab) for (int k = 0; k < p.length; k++) agg[k] += p[k];
        for (int k = 0; k < agg.length; k++) agg[k] /= lab.size();
        int best = 0, second = -1;
        for (int k = 1; k < agg.length; k++) if (agg[k] > agg[best]) best = k;
        for (int k = 0; k < agg.length; k++) {
            if (k == best) continue;
            if (second < 0 || agg[k] > agg[second]) second = k;
        }
        double margin = second < 0 ? agg[best] : agg[best] - agg[second];
        return new Confidence(langs.get(best), agg[best], margin);
    }
}
