package teacommontea.veritedoux.preprocess;

import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.tukaani.xz.XZInputStream;
import teacommontea.veritedoux.preprocess.EveLexicon;

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

    public static byte[] decodeXzBytes(InputStream raw) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1 << 24);
        try (XZInputStream xz = new XZInputStream(raw)) {
            byte[] buf = new byte[1 << 16];
            int n;
            while ((n = xz.read(buf)) > 0) out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    public static void load(Plugin plugin) {
        LANGS.clear();
        if (!EveLexicon.ready()) {
            plugin.getLogger().warning("EVE segmentation: lexicon not ready, disabled");
            return;
        }
        java.util.Set<String> present = new java.util.HashSet<>(java.util.Arrays.asList(EveLexicon.languages()));
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
        double pos = Math.max(position, 0.00001);
        return -Math.log(pos);
    }

    private Double costOf(String word) {
        Double pos = EveLexicon.positionIn(label, word);
        return pos == null ? null : costForPosition(pos);
    }

    private Double costOf(String word, Map<String, Map<String, Double>> positionCache) {
        Map<String, Double> positions = positionCache.get(word);
        if (positions == null && !positionCache.containsKey(word)) {
            positions = EveLexicon.position(word);
            positionCache.put(word, positions);
        }
        Double pos = positions == null ? null : positions.get(label);
        return pos == null ? null : costForPosition(pos);
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
        java.util.Arrays.sort(mid);
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
                if (w.isEmpty()) continue;
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

    public static List<String> segmentLines(String lower, Map<String, String> tokenCache,
                                     Map<String, Map<String, Double>> positionCache) {
        List<String> out = new ArrayList<>();
        for (EveSegment seg : LANGS) {
            String segged = seg.segmentLine(lower, tokenCache, positionCache);
            if (!segged.equals(lower) && !out.contains(segged)) {
                out.add(segged);
            }
        }
        return out;
    }

    private String segmentLine(String lower, Map<String, String> tokenCache,
                               Map<String, Map<String, Double>> positionCache) {
        StringBuilder sb = new StringBuilder();
        for (String w : lower.split("\\s+")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(segmentCached(w, tokenCache, positionCache));
        }
        return sb.toString();
    }

    private String segmentCached(String token, Map<String, String> tokenCache,
                                 Map<String, Map<String, Double>> positionCache) {
        if (token.length() < 4 || token.length() > MAX_SEGMENT_TOKEN) {
            return token;
        }
        String key = label + '\u0000' + token;
        String cached = tokenCache.get(key);
        if (cached != null) {
            return cached;
        }
        String segmented = segment(token, positionCache);
        tokenCache.put(key, segmented);
        return segmented;
    }

    private String segment(String token, Map<String, Map<String, Double>> positionCache) {
        int n = token.length();
        if (n < 4 || n > MAX_SEGMENT_TOKEN) {
            return token;
        }
        double[] c = new double[n + 1];
        int[] back = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            double best = Double.MAX_VALUE;
            int bestK = i - 1;
            for (int k = Math.max(0, i - maxWord); k < i; k++) {
                Double wc = costOf(token.substring(k, i), positionCache);
                double cc = c[k] + (wc != null ? wc : (i - k) * 9.0 + 9.0);
                if (cc < best) {
                    best = cc;
                    bestK = k;
                }
            }
            c[i] = best;
            back[i] = bestK;
        }
        LinkedList<String> tiles = new LinkedList<>();
        int i = n;
        while (i > 0) {
            tiles.addFirst(token.substring(back[i], i));
            i = back[i];
        }
        return String.join(" ", tiles);
    }

    private static final Map<String, String> VOWELS_BY_LANG = new HashMap<>();

    public static void setVowels(String lang, String vowels) {
        if (vowels == null) vowels = "";
        VOWELS_BY_LANG.put(lang, vowels);
    }

    public static void clearVowels() {
        VOWELS_BY_LANG.clear();
    }

    private static String vowels() {
        java.util.LinkedHashSet<Character> set = new java.util.LinkedHashSet<>();
        for (String v : VOWELS_BY_LANG.values()) {
            for (int i = 0; i < v.length(); i++) set.add(v.charAt(i));
        }
        StringBuilder sb = new StringBuilder();
        for (char c : set) sb.append(c);
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
