package teacommontea.veritedoux.preprocess;

import org.bukkit.plugin.Plugin;
import org.tukaani.xz.XZInputStream;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class EveSegment {

    private record Lang(String label) {}

    private static final Lang[] REGISTRY = {
            new Lang("english"),
            new Lang("spanish"),
            new Lang("german"),
            new Lang("italian"),
            new Lang("french"),
            new Lang("portuguese"),
    };

    private static final List<EveSegment> LANGS = new ArrayList<>();

    private int maxWord = 1;
    private String label = "";

    private EveSegment() {}

    public static boolean knownIn(String lang, String word) {
        for (EveSegment seg : LANGS) {
            if (seg.label.equals(lang)) {
                return EveLexicon.knownIn(seg.label, word);
            }
        }
        return false;
    }

    public static List<String> languages() {
        List<String> out = new ArrayList<>();
        for (EveSegment seg : LANGS) {
            out.add(seg.label);
        }
        return out;
    }

    private static final int MAX_SEGMENT_WORD = 40;
    private static final int MAX_SEGMENT_TOKEN = 256;
    private static final double UNKNOWN_CHAR_COST = 9.0;
    private static final double UNKNOWN_TILE_COST = 9.0;

    public static byte[] decodeXzBytes(InputStream raw) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 24);
        try (XZInputStream xz = new XZInputStream(raw)) {
            byte[] buf = new byte[1 << 16];
            int n;
            while ((n = xz.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
        }
        return out.toByteArray();
    }

    public static void load(Plugin plugin) {
        LANGS.clear();
        if (!EveLexicon.ready()) {
            plugin.getLogger().warning("EVE segmentation: lexicon not ready, disabled");
            return;
        }
        java.util.Set<String> present = new java.util.HashSet<>(Arrays.asList(EveLexicon.languages()));
        for (Lang lang : REGISTRY) {
            if (present.contains(lang.label())) {
                EveSegment seg = new EveSegment();
                seg.label = lang.label();
                seg.maxWord = MAX_SEGMENT_WORD;
                LANGS.add(seg);
            }
        }
        if (LANGS.isEmpty()) {
            plugin.getLogger().warning("EVE segmentation: no languages loaded, disabled");
        } else {
            plugin.getLogger().info("EVE segmentation: " + LANGS.size() + " langs (on-disk).");
        }
    }

    private static double costForPosition(double position) {
        double pos = Math.max(position, 0.0000001);
        return -Math.log(pos);
    }

    private Double costOf(String word) {
        Double pos = EveLexicon.positionIn(label, word);
        return pos == null ? null : costForPosition(pos);
    }

    private Double costOf(String word, Map<String, Map<String, Double>> positionCache) {
        Map<String, Double> positions = positionsFor(word, positionCache);
        Double pos = positions == null ? null : positions.get(label);
        return pos == null ? null : costForPosition(pos);
    }

    private static Map<String, Double> positionsFor(
            String word,
            Map<String, Map<String, Double>> positionCache
    ) {
        Map<String, Double> positions = positionCache.get(word);
        if (positions == null && !positionCache.containsKey(word)) {
            positions = EveLexicon.position(word);
            positionCache.put(word, positions);
        }
        return positions;
    }

    private static double unknownCost(int len) {
        return len * UNKNOWN_CHAR_COST + UNKNOWN_TILE_COST;
    }

    public static boolean ready() {
        return !LANGS.isEmpty();
    }

    public static boolean fingerprintCollides(String fp, String slur) {
        if (slur == null || slur.length() < 3) {
            return false;
        }
        char first = slur.charAt(0);
        char last = slur.charAt(slur.length() - 1);
        char[] mid = slur.substring(1, slur.length() - 1).toCharArray();
        Arrays.sort(mid);
        StringBuilder cand = new StringBuilder(slur.length());
        boolean[] used = new boolean[mid.length];
        return permuteProbe(fp, slur, first, last, mid, used, cand);
    }

    private static final double FP_COLLISION_MIN_POSITION = 0.00001;

    private static boolean permuteProbe(String fp, String slur, char first, char last,
                                        char[] mid, boolean[] used, StringBuilder cand) {
        if (cand.length() == mid.length) {
            String word = first + cand.toString() + last;
            if (word.equals(slur) || !fingerprint(word).equals(fp)) {
                return false;
            }
            return EveLexicon.maxPosition(word) > FP_COLLISION_MIN_POSITION;
        }
        char prev = 0;
        for (int i = 0; i < mid.length; i++) {
            if (used[i] || (i > 0 && mid[i] == prev && !used[i - 1])) {
                continue;
            }
            used[i] = true;
            cand.append(mid[i]);
            if (permuteProbe(fp, slur, first, last, mid, used, cand)) {
                return true;
            }
            cand.setLength(cand.length() - 1);
            used[i] = false;
            prev = mid[i];
        }
        return false;
    }

    private static String fingerprint(String w) {
        return teacommontea.veritedoux.util.EveText.fingerprint(w);
    }

    private static final double OWNER_UNKNOWN = 20.0;

    public static String owner(String line) {
        if (LANGS.isEmpty()) {
            return "";
        }
        String[] words = line.split("\\s+");
        String best = LANGS.get(0).label;
        double bestCost = Double.MAX_VALUE;
        for (EveSegment seg : LANGS) {
            double total = 0;
            for (String w : words) {
                if (w.isEmpty()) {
                    continue;
                }
                Double wc = seg.costOf(w);
                total += (wc != null ? wc : OWNER_UNKNOWN);
            }
            if (total < bestCost - 1e-9) {
                bestCost = total;
                best = seg.label;
            }
        }
        return best;
    }

    public static List<String> segmentLines(
            String lower,
            Map<String, String> tokenCache,
            Map<String, Map<String, Double>> positionCache
    ) {
        if (LANGS.isEmpty()) {
            return List.of();
        }
        return segmentLinesFused(lower, tokenCache, positionCache);
    }

    private static List<String> segmentLinesFused(
            String lower,
            Map<String, String> tokenCache,
            Map<String, Map<String, Double>> positionCache
    ) {
        String[] words = lower.split("\s+");
        int langCount = LANGS.size();
        StringBuilder[] builders = new StringBuilder[langCount];
        for (int l = 0; l < langCount; l++) {
            builders[l] = new StringBuilder(lower.length() + 16);
        }

        for (String token : words) {
            String[] results = new String[langCount];
            boolean allCached = true;

            if (token.length() >= 4 && token.length() <= MAX_SEGMENT_TOKEN) {
                for (int l = 0; l < langCount; l++) {
                    String key = cacheKey(LANGS.get(l).label, token);
                    results[l] = tokenCache.get(key);
                    if (results[l] == null) {
                        allCached = false;
                    }
                }
            }

            if (token.length() < 4 || token.length() > MAX_SEGMENT_TOKEN) {
                Arrays.fill(results, token);
            } else if (!allCached) {
                String[] computed = segmentTokenFused(token, positionCache);
                for (int l = 0; l < langCount; l++) {
                    if (results[l] == null) {
                        results[l] = computed[l];
                        tokenCache.put(cacheKey(LANGS.get(l).label, token), results[l]);
                    }
                }
            }

            appendAll(builders, results);
        }

        return collectChangedUnique(lower, builders);
    }

    private static String[] segmentTokenFused(
            String token,
            Map<String, Map<String, Double>> positionCache
    ) {
        final int maxWord = MAX_SEGMENT_WORD;
        int n = token.length();
        int langCount = LANGS.size();
        double[][] c = new double[langCount][n + 1];
        int[][] back = new int[langCount][n + 1];

        @SuppressWarnings("unchecked")
        ArrayDeque<Integer>[] deques = new ArrayDeque[langCount];
        for (int l = 0; l < langCount; l++) {
            deques[l] = new ArrayDeque<>();
        }

        double[] best = new double[langCount];
        int[] bestK = new int[langCount];

        for (int end = 1; end <= n; end++) {
            int entering = end - 1;
            int minK = Math.max(0, end - maxWord);

            for (int l = 0; l < langCount; l++) {
                ArrayDeque<Integer> dq = deques[l];
                double enteringValue = c[l][entering] - UNKNOWN_CHAR_COST * entering;
                while (!dq.isEmpty()) {
                    int tail = dq.peekLast();
                    double tailValue = c[l][tail] - UNKNOWN_CHAR_COST * tail;
                    if (tailValue <= enteringValue) {
                        break;
                    }
                    dq.removeLast();
                }
                dq.addLast(entering);
                while (!dq.isEmpty() && dq.peekFirst() < minK) {
                    dq.removeFirst();
                }

                int k = dq.peekFirst();
                best[l] = UNKNOWN_CHAR_COST * end + UNKNOWN_TILE_COST
                        + (c[l][k] - UNKNOWN_CHAR_COST * k);
                bestK[l] = k;
            }

            for (int k = minK; k < end; k++) {
                String sub = token.substring(k, end);
                Map<String, Double> positions = positionsFor(sub, positionCache);
                if (positions == null || positions.isEmpty()) {
                    continue;
                }

                for (int l = 0; l < langCount; l++) {
                    Double pos = positions.get(LANGS.get(l).label);
                    if (pos == null) {
                        continue;
                    }
                    double cc = c[l][k] + costForPosition(pos);
                    if (cc < best[l]) {
                        best[l] = cc;
                        bestK[l] = k;
                    }
                }
            }

            for (int l = 0; l < langCount; l++) {
                c[l][end] = best[l];
                back[l][end] = bestK[l];
            }
        }

        String[] out = new String[langCount];
        for (int l = 0; l < langCount; l++) {
            out[l] = rebuild(token, back[l]);
        }
        return out;
    }

    private static String cacheKey(String label, String token) {
        return label + ' ' + token;
    }

    private static String rebuild(String token, int[] back) {
        LinkedList<String> tiles = new LinkedList<>();
        int i = token.length();
        while (i > 0) {
            int k = back[i];
            if (k < 0 || k >= i) {
                k = i - 1;
            }
            tiles.addFirst(token.substring(k, i));
            i = k;
        }
        return String.join(" ", tiles);
    }

    private static void appendAll(StringBuilder[] builders, String[] tokens) {
        for (int l = 0; l < builders.length; l++) {
            if (builders[l].length() > 0) {
                builders[l].append(' ');
            }
            builders[l].append(tokens[l]);
        }
    }

    private static List<String> collectChangedUnique(String lower, StringBuilder[] builders) {
        List<String> out = new ArrayList<>();
        for (StringBuilder builder : builders) {
            addUniqueChanged(out, lower, builder.toString());
        }
        return out;
    }

    private static void addUniqueChanged(List<String> out, String lower, String segged) {
        if (!segged.equals(lower) && !out.contains(segged)) {
            out.add(segged);
        }
    }

    private static final Map<String, String> VOWELS_BY_LANG = new HashMap<>();

    public static void setVowels(String lang, String vowels) {
        if (vowels == null) {
            vowels = "";
        }
        VOWELS_BY_LANG.put(lang, vowels);
    }

    public static void clearVowels() {
        VOWELS_BY_LANG.clear();
    }

    private static String vowels() {
        LinkedHashSet<Character> set = new LinkedHashSet<>();
        for (String v : VOWELS_BY_LANG.values()) {
            for (int i = 0; i < v.length(); i++) {
                set.add(v.charAt(i));
            }
        }
        StringBuilder sb = new StringBuilder();
        for (char c : set) {
            sb.append(c);
        }
        return sb.toString();
    }

    public static String deobfuscate(String token) {
        if (LANGS.isEmpty()) {
            return null;
        }

        Map<Character, Integer> freq = new HashMap<>();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                freq.merge(c, 1, Integer::sum);
            }
        }
        char filler = 0;
        int max = 0;
        for (Map.Entry<Character, Integer> e : freq.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                filler = e.getKey();
            }
        }
        if (max < 2) {
            return null;
        }

        StringBuilder dense = new StringBuilder();
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c == filler) {
                int p = dense.length();
                if (p > 0 && (slots.isEmpty() || slots.get(slots.size() - 1) != p)) {
                    slots.add(p);
                }
            } else if (Character.isLetter(c)) {
                dense.append(c);
            }
        }
        String d = dense.toString();
        if (isKnown(d)) {
            return d;
        }

        if (d.length() > DEOB_MAX_DENSE || slots.size() > DEOB_MAX_SLOTS) {
            return null;
        }
        List<Integer> use = new ArrayList<>();
        for (int s : slots) {
            if (s > 0 && s < d.length()) {
                use.add(s);
            }
        }
        return repairSearch(d, use, 0, 0, vowels());
    }

    private static final int DEOB_MAX_DENSE = 14;
    private static final int DEOB_MAX_SLOTS = 8;

    private static String repairSearch(String s, List<Integer> slots, int idx, int inserted, String vowels) {
        if (isKnown(s)) {
            return s;
        }
        if (idx >= slots.size() || inserted >= 3) {
            return null;
        }
        int pos = slots.get(idx) + inserted;
        if (pos <= s.length()) {
            for (int v = 0; v < vowels.length(); v++) {
                String cand = s.substring(0, pos) + vowels.charAt(v) + s.substring(pos);
                String r = repairSearch(cand, slots, idx + 1, inserted + 1, vowels);
                if (r != null) {
                    return r;
                }
            }
        }
        return repairSearch(s, slots, idx + 1, inserted, vowels);
    }

    private static boolean isKnown(String w) {
        if (w.length() < 3) {
            return false;
        }
        for (EveSegment seg : LANGS) {
            if (EveLexicon.knownIn(seg.label, w)) {
                return true;
            }
        }
        return false;
    }
}
