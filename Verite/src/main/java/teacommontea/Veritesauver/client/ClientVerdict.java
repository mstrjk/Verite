package teacommontea.veritesauver.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ClientVerdict {

    private ClientVerdict() {}

    private static final int CONFIDENT = 70;

    public static void decide(ClientProfile profile) {
        Map<String, Map<Signal.Source, Integer>> strongest = new LinkedHashMap<>();
        for (Signal s : profile.signals()) {
            if (!identifying(s.source())) {
                continue;
            }
            strongest.computeIfAbsent(s.key(), k -> new LinkedHashMap<>())
                    .merge(s.source(), s.weight(), Integer::max);
        }
        Map<String, Integer> scores = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Signal.Source, Integer>> e : strongest.entrySet()) {
            int total = 0;
            for (int weight : e.getValue().values()) {
                total += weight;
            }
            scores.put(e.getKey(), total);
        }

        String best = null;
        int bestScore = 0;
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            if (e.getValue() > bestScore) {
                best = e.getKey();
                bestScore = e.getValue();
            }
        }

        String reported = profile.reportedBrand();
        if (best == null) {
            boolean contradicted = contradictsVanilla(profile);
            if (contradicted) {
                profile.conclude(pretty(reported) + " (disputed)", 60);
            } else {
                profile.conclude(pretty(reported), reported == null ? 0 : 40);
            }
            return;
        }

        profile.conclude(best, confidence(profile, best, bestScore, reported));
    }

    private static int confidence(ClientProfile profile, String identity, int score, String reported) {
        java.util.Set<Signal.Source> corroborating = new java.util.HashSet<>();
        for (Signal s : profile.signals()) {
            if (identifying(s.source()) && s.key().equals(identity)) {
                corroborating.add(s.source());
            }
        }
        int capped = Math.min(70, score);
        if (corroborating.size() > 1) {
            capped = Math.min(95, capped + 12 * (corroborating.size() - 1));
        }
        if (corroborating.size() == 1 && corroborating.contains(Signal.Source.BRAND)) {
            capped = Math.min(capped, 45);
        }
        if (reported != null && !reported.isBlank() && !matches(reported, identity)) {
            capped = Math.min(95, capped + 5);
        }
        return capped;
    }

    public static boolean confident(ClientProfile profile) {
        return profile.confidence() >= CONFIDENT;
    }

    public static boolean contradictsVanilla(ClientProfile profile) {
        String reported = profile.reportedBrand();
        boolean claimsVanilla = reported == null || reported.isBlank()
                || reported.equalsIgnoreCase("vanilla");
        if (!claimsVanilla) {
            return false;
        }
        for (Signal s : profile.signals()) {
            if (identifying(s.source())) {
                return true;
            }
        }
        return false;
    }

    public static List<String> mods(ClientProfile profile) {
        List<String> out = new ArrayList<>();
        for (Signal s : profile.signals()) {
            if (identifying(s.source()) && !out.contains(s.key())) {
                out.add(s.key());
            }
        }
        return out;
    }

    public static List<String> unknownChannels(ClientProfile profile) {
        List<String> out = new ArrayList<>();
        for (Signal s : profile.signals()) {
            if (s.source() == Signal.Source.UNKNOWN_CHANNEL && !out.contains(s.key())) {
                out.add(s.key());
            }
        }
        return out;
    }

    private static boolean identifying(Signal.Source source) {
        return source == Signal.Source.CHANNEL || source == Signal.Source.SIGN
                || source == Signal.Source.BRAND
                || source == Signal.Source.NORMALISED;
    }

    public static List<String> anomalies(ClientProfile profile) {
        List<String> out = new ArrayList<>();
        for (Signal s : profile.signals()) {
            if (s.source() != Signal.Source.COOKIE && s.source() != Signal.Source.CLIENT_INFO
                    && s.source() != Signal.Source.NORMALISED) {
                continue;
            }
            if (!out.contains(s.key())) {
                out.add(s.key());
            }
        }
        return out;
    }

    private static boolean matches(String reported, String identity) {
        String a = reported.toLowerCase(Locale.ROOT).replace(" ", "");
        String b = identity.toLowerCase(Locale.ROOT).replace(" ", "");
        return a.contains(b) || b.contains(a);
    }

    private static String pretty(String brand) {
        if (brand == null || brand.isBlank()) {
            return "unknown";
        }
        return teacommontea.util.text.Text.capitalise(brand);
    }
}
