package teacommontea.veritedoux.util;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.bukkit.plugin.Plugin;

public final class EveText {

    private static final int MAX_CANDIDATES = 256;

    private EveText() {}

    public static String stripEntities(String s) {
        return teacommontea.veritedoux.util.Eve.stripEntities(s);
    }

    public static String stripDeletes(String s) {
        return teacommontea.veritedoux.util.Eve.stripDeletes(s);
    }

    public static String reduceRuns(String s, int max) {
        return teacommontea.veritedoux.util.Eve.reduceRuns(s, max);
    }

    public static String fingerprint(String w) {
        return teacommontea.veritedoux.util.Eve.fingerprint(w);
    }

    public static String foldAccents(String s) {
        if (s.chars().allMatch(c -> c < 0x80)) {
            return s;
        }
        String d = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKD);
        StringBuilder b = new StringBuilder(d.length());
        int i = 0;
        while (i < d.length()) {
            int cp = d.codePointAt(i);
            if (Character.getType(cp) != Character.NON_SPACING_MARK) {
                b.appendCodePoint(cp);
            }
            i += Character.charCount(cp);
        }
        return b.toString();
    }

    public static boolean hasRepeat(String s) {
        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == s.charAt(i - 1) && Character.isLetter(c)) return true;
        }
        return false;
    }

    public static String trimEdges(String w) {
        int s = 0, e = w.length();
        while (s < e && !Character.isLetterOrDigit(w.charAt(s))) s++;
        while (e > s && !Character.isLetterOrDigit(w.charAt(e - 1))) e--;
        return (s == 0 && e == w.length()) ? w : w.substring(s, e);
    }

    public static boolean isFancyLetter(int cp) {
        return (cp >= 0x1D400 && cp <= 0x1D7FF)
                || (cp >= 0xFF21 && cp <= 0xFF3A)
                || (cp >= 0xFF41 && cp <= 0xFF5A)
                || (cp >= 0x2460 && cp <= 0x24FF)
                || (cp >= 0x1F130 && cp <= 0x1F169)
                || (cp >= 0x1F150 && cp <= 0x1F189);
    }

    public static List<String> candidates(String message, boolean entityStrip, boolean homoglyphFold) {
        java.util.LinkedHashSet<String> out = new java.util.LinkedHashSet<>();
        out.addAll(teacommontea.veritedoux.util.Eve.candidates(message, entityStrip, homoglyphFold, MAX_CANDIDATES));
        String normalized = foldAccents(message.toLowerCase(java.util.Locale.ROOT));
        if (!normalized.equals(message)) {
            out.addAll(teacommontea.veritedoux.util.Eve.candidates(normalized, entityStrip, homoglyphFold, MAX_CANDIDATES));
        }
        return new java.util.ArrayList<>(out);
    }

    public static void loadConfusables(Plugin plugin) {
        try {
            java.io.File f = new java.io.File(plugin.getDataFolder(), "filter/.native/confusables.evefold");
            if (!f.isFile()) {
                return;
            }
            String text = new String(java.nio.file.Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            teacommontea.veritedoux.util.Eve.loadFold(text);
        } catch (Exception e) {
            plugin.getLogger().warning("EVE confusables table failed to load: " + e.getMessage());
        }
    }
}
