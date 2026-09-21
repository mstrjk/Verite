package teacommontea.veritedoux.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import teacommontea.veritedoux.preprocess.EveLexicon;

public final class SegBench {

    private static final int MAX_WORD = 40;
    private static final int WARMUP = 20;
    private static final int ITERS = 100;

    public static final String DEFAULT_SENTENCE =
            "i am an unusually long sentence built to do nothing except for time "
            + "the duration of this specific test and hope that it ends up well "
            + "because jesus long sentences are not easy or nice for this poor thing";

    private interface Known { double apply(double position, int len); }
    private interface Unknown { double apply(int len); }

    private record Variant(String name, String note, Known known, Unknown unknown) {}

    private static final List<Variant> VARIANTS = new ArrayList<>();

    private static void add(String name, String note, Known k, Unknown u) {
        VARIANTS.add(new Variant(name, note, k, u));
    }

    static {
        add("baseline", "-log(pos), unk=9L+9",
                (p, l) -> -Math.log(Math.max(p, 1e-5)), l -> l * 9.0 + 9.0);
        add("unk-len2", "-log(pos), unk=L^2",
                (p, l) -> -Math.log(Math.max(p, 1e-5)), l -> (double) (l * l));
        add("unk-flat", "-log(pos), unk=30",
                (p, l) -> -Math.log(Math.max(p, 1e-5)), l -> 30.0);
        add("unk-steep", "-log(pos), unk=18L+18",
                (p, l) -> -Math.log(Math.max(p, 1e-5)), l -> l * 18.0 + 18.0);
        add("unk-gentle", "-log(pos), unk=4L+4",
                (p, l) -> -Math.log(Math.max(p, 1e-5)), l -> l * 4.0 + 4.0);
        add("floor-1e3", "-log(pos) floor 1e-3",
                (p, l) -> -Math.log(Math.max(p, 1e-3)), l -> l * 9.0 + 9.0);
        add("floor-1e7", "-log(pos) floor 1e-7",
                (p, l) -> -Math.log(Math.max(p, 1e-7)), l -> l * 9.0 + 9.0);
        add("lenbonus", "-log(pos) - log(L)",
                (p, l) -> -Math.log(Math.max(p, 1e-5)) - Math.log(l), l -> l * 9.0 + 9.0);
        add("lenbonus2", "-log(pos) - 2log(L)",
                (p, l) -> -Math.log(Math.max(p, 1e-5)) - 2.0 * Math.log(l), l -> l * 9.0 + 9.0);
        add("lenpenalty", "-log(pos) + L/4",
                (p, l) -> -Math.log(Math.max(p, 1e-5)) + l / 4.0, l -> l * 9.0 + 9.0);
        add("sqrt-pos", "-0.5log(pos)",
                (p, l) -> -0.5 * Math.log(Math.max(p, 1e-5)), l -> l * 9.0 + 9.0);
        add("sq-pos", "-2log(pos)",
                (p, l) -> -2.0 * Math.log(Math.max(p, 1e-5)), l -> l * 9.0 + 9.0);
        add("log10", "-log10(pos)",
                (p, l) -> -Math.log10(Math.max(p, 1e-5)), l -> (l * 9.0 + 9.0) / Math.log(10));
        add("wordcost", "-log(pos) + 6 per word",
                (p, l) -> -Math.log(Math.max(p, 1e-5)) + 6.0, l -> l * 9.0 + 15.0);
        add("zipf", "log2(1/pos)",
                (p, l) -> Math.log(1.0 / Math.max(p, 1e-5)) / Math.log(2), l -> l * 13.0 + 13.0);
        add("norm-len", "-log(pos)/L",
                (p, l) -> -Math.log(Math.max(p, 1e-5)) / l, l -> 9.0 + 9.0 / l);
        add("bounded", "min(-log(pos), 20)",
                (p, l) -> Math.min(-Math.log(Math.max(p, 1e-5)), 20.0), l -> l * 9.0 + 9.0);
        add("shift", "-log(pos + 1e-4)",
                (p, l) -> -Math.log(p + 1e-4), l -> l * 9.0 + 9.0);
        add("entropy", "-log(pos) * (1 + 1/L)",
                (p, l) -> -Math.log(Math.max(p, 1e-5)) * (1.0 + 1.0 / l), l -> l * 9.0 + 9.0);
        add("aggressive", "-log(pos) - 3L",
                (p, l) -> -Math.log(Math.max(p, 1e-5)) - 3.0 * l, l -> l * 9.0 + 9.0);
    }

    private SegBench() {}

    private static String segment(String token, String label, Variant v,
                                  Map<String, Map<String, Double>> cache) {
        int n = token.length();
        if (n < 4) return token;
        double[] c = new double[n + 1];
        int[] back = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            double best = Double.MAX_VALUE;
            int bestK = i - 1;
            for (int k = Math.max(0, i - MAX_WORD); k < i; k++) {
                String sub = token.substring(k, i);
                Map<String, Double> positions = cache.get(sub);
                if (positions == null && !cache.containsKey(sub)) {
                    positions = EveLexicon.position(sub);
                    cache.put(sub, positions);
                }
                Double pos = positions == null ? null : positions.get(label);
                int len = i - k;
                double wc = pos == null ? v.unknown().apply(len) : v.known().apply(pos, len);
                double cc = c[k] + wc;
                if (cc < best) { best = cc; bestK = k; }
            }
            c[i] = best;
            back[i] = bestK;
        }
        LinkedList<String> tiles = new LinkedList<>();
        int i = n;
        while (i > 0) { tiles.addFirst(token.substring(back[i], i)); i = back[i]; }
        return String.join(" ", tiles);
    }

    public static void roundTrip(String sentence, Consumer<String> out) {
        if (sentence == null || sentence.isBlank()) sentence = DEFAULT_SENTENCE;
        if (!EveLexicon.ready()) {
            out.accept("ABORT: lexicon not ready.");
            return;
        }
        out.accept(String.format("message: %d chars, %d words",
                sentence.length(), sentence.split("\\s+").length));

        teacommontea.veritedoux.process.EveRoute.route(sentence);

        teacommontea.veritedoux.process.EveRoute.benchReset();
        int n = 3;
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            teacommontea.veritedoux.process.EveRoute.route(sentence);
        }
        double totalMs = (System.nanoTime() - t0) / 1e6;
        out.accept(String.format("n=%d  total=%.2f ms  per-message=%.2f ms", n, totalMs, totalMs / n));
        out.accept("");
        for (String line : teacommontea.veritedoux.process.EveRoute.benchReport().split("\n")) {
            out.accept(line);
        }
    }

    public static void run(String sentence, Consumer<String> out) {
        if (sentence == null || sentence.isBlank()) sentence = DEFAULT_SENTENCE;

        if (!EveLexicon.ready()) {
            out.accept("ABORT: lexicon not ready - every lookup would miss,");
            out.accept("       so the timings would measure nothing real.");
            return;
        }
        Map<String, Double> probe = EveLexicon.position("sentence");
        if (probe == null || probe.get("english") == null) {
            out.accept("ABORT: lexicon open but 'sentence' has no english row.");
            return;
        }

        String despaced = sentence.replace(" ", "");
        out.accept(String.format("input=%d chars  despaced=%d chars  langs=%s",
                sentence.length(), despaced.length(),
                String.join(",", EveLexicon.languages())));
        out.accept(String.format("pos(sentence,en)=%.3e  warmup=%d iters=%d",
                probe.get("english"), WARMUP, ITERS));
        out.accept(String.format("%-12s %-24s %9s %7s", "variant", "formula", "ms/iter", "rel"));
        out.accept("-".repeat(56));

        String label = "english";
        double baseMs = -1;
        List<String> outputs = new ArrayList<>();
        for (Variant v : VARIANTS) {
            String seg = null;
            for (int i = 0; i < WARMUP; i++) seg = segment(despaced, label, v, new HashMap<>());
            long t0 = System.nanoTime();
            for (int i = 0; i < ITERS; i++) seg = segment(despaced, label, v, new HashMap<>());
            double ms = (System.nanoTime() - t0) / 1e6 / ITERS;
            if (baseMs < 0) baseMs = ms;
            out.accept(String.format("%-12s %-24s %9.3f %6.2fx",
                    v.name(), v.note(), ms, ms / baseMs));
            outputs.add(v.name() + " | " + seg);
        }
        out.accept("-".repeat(56));
        for (String s : outputs) out.accept(s);
    }
}
