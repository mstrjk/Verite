package teacommontea.veritedoux.preprocess;

import org.bukkit.plugin.Plugin;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import teacommontea.veritedoux.util.EveVlex9;

public final class EveScam {

    private static final double THRESHOLD = 15.0;
    private static final double SCALE = 100.0;
    private static final int WINDOW = 3;
    private static final String PRIOR_KEY = "__PRIOR__";
    private static final String HASH_TOK = "\u0002HASH\u0002";
    private static final char SEP = '\u001f';

    private static final Pattern URL = Pattern.compile("https?://\\S+");
    private static final Pattern DIGITS = Pattern.compile("\\b\\d{4,}\\b");
    private static final Pattern TOKEN = Pattern.compile("[a-z0-9']+|https?://\\S+|[@#$/.]");

    private static EveVlex9 reader;
    private static double prior = 0.0;
    private static boolean loaded = false;

    private EveScam() {}

    public static void load(Plugin plugin) {
        close();
        try {
            EveVlex9.configureNativeDir(new java.io.File(plugin.getDataFolder(), "filter/.native"));
            java.io.File f = new java.io.File(plugin.getDataFolder(), "filter/.tokenizers/tokenizer_ad.vlex9");
            if (!f.isFile()) return;
            EveVlex9 r = new EveVlex9(f.toPath());
            reader = r;
            Double p = weightOf(PRIOR_KEY);
            prior = p == null ? 0.0 : p;
            loaded = true;
            plugin.getLogger().info("EVE scam: specialty ad/scam model ready.");
        } catch (Exception e) {
            plugin.getLogger().warning("EVE scam: load failed: " + e.getMessage());
            close();
        }
    }

    public static void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception ignored) {
            }
        }
        reader = null;
        prior = 0.0;
        loaded = false;
    }

    public static boolean ready() {
        return loaded;
    }

    private static List<String> normalize(String message) {
        String t = message.toLowerCase();
        t = URL.matcher(t).replaceAll(" URLTOK ");
        t = DIGITS.matcher(t).replaceAll(" NUMTOK ");
        List<String> out = new ArrayList<>();
        Matcher m = TOKEN.matcher(t);
        while (m.find()) out.add(m.group());
        return out;
    }

    private static String keyToken(String token) {
        return token.equals("#") ? HASH_TOK : token;
    }

    private static Double weightOf(String key) {
        EveVlex9 r = reader;
        if (r == null) return null;
        try {
            EveVlex9.LookupResult res = r.lookup(key.getBytes(StandardCharsets.UTF_8));
            if (res == null || res.rows.length == 0) return null;
            int[] regs = res.rows[0].registers();
            return EveVlex9.score(regs) / SCALE;
        } catch (Exception e) {
            return null;
        }
    }

    public static double score(String message) {
        if (!loaded) return Double.NaN;
        List<String> tokens = normalize(message);
        if (tokens.isEmpty()) return prior;
        java.util.HashSet<String> feats = new java.util.HashSet<>();
        int n = tokens.size();
        for (int i = 0; i < n; i++) {
            feats.add(keyToken(tokens.get(i)));
            for (int j = i + 1; j < Math.min(i + 1 + WINDOW, n); j++) {
                String a = tokens.get(i);
                String b = tokens.get(j);
                String lo = a.compareTo(b) <= 0 ? a : b;
                String hi = a.compareTo(b) <= 0 ? b : a;
                feats.add(keyToken(lo) + SEP + keyToken(hi));
            }
        }
        double s = prior;
        for (String f : feats) {
            Double w = weightOf(f);
            if (w != null) s += w;
        }
        return s;
    }

    public static boolean looksLikeScam(String message) {
        double s = score(message);
        return !Double.isNaN(s) && s >= THRESHOLD;
    }
}
