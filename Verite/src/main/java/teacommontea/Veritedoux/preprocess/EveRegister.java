package teacommontea.veritedoux.preprocess;

import org.bukkit.plugin.Plugin;

import teacommontea.veritedoux.preprocess.EveLexicon;

public final class EveRegister {

    private static final int MT = 0, LY = 1, SP = 2, ID = 3, NA = 4, HI = 5, IN = 6, OP = 7, IP = 8;

    private static boolean loaded = false;
    private static double adThreshold = 0.241;

    private EveRegister() {}

    public static void load(Plugin plugin) {
        loaded = EveLexicon.ready();
        if (loaded) {
            plugin.getLogger().info("EVE register: message-type detection ready.");
        }
    }

    public static void configure(double threshold) {
        adThreshold = threshold;
    }

    public static boolean ready() {
        return loaded;
    }

    public static double[] vector(String line) {
        double[] agg = new double[9];
        int n = 0;
        for (String w : line.toLowerCase().split("\\s+")) {
            String tok = teacommontea.veritedoux.util.EveText.foldAccents(w);
            if (tok.isEmpty()) continue;
            float[] v = EveLexicon.registers(tok);
            if (v == null) continue;
            double s = 0;
            for (float x : v) s += x;
            if (s <= 0) continue;
            for (int i = 0; i < 9; i++) agg[i] += v[i] / s;
            n++;
        }
        if (n == 0) return null;
        for (int i = 0; i < 9; i++) agg[i] /= n;
        return agg;
    }

    public static double adLean(String line) {
        double[] v = vector(line);
        if (v == null) return Double.NaN;
        return v[IP] + v[OP] - v[ID] - v[SP];
    }

    public static boolean looksLikeAd(String line) {
        double s = adLean(line);
        return !Double.isNaN(s) && s >= adThreshold;
    }
}
