package teacommontea.veritedoux.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EveDebug {

    private static volatile boolean enabled = false;
    private static final Map<String, long[]> STAGES = new LinkedHashMap<>();

    private EveDebug() {}

    public static void enable() {
        enabled = true;
    }

    public static void disable() {
        enabled = false;
    }

    public static boolean on() {
        return enabled;
    }

    public static void reset() {
        synchronized (STAGES) {
            STAGES.clear();
        }
    }

    public static long start() {
        return enabled ? System.nanoTime() : 0L;
    }

    public static void end(String stage, long startNanos) {
        if (!enabled) return;
        long delta = System.nanoTime() - startNanos;
        synchronized (STAGES) {
            long[] acc = STAGES.computeIfAbsent(stage, k -> new long[2]);
            acc[0] += delta;
            acc[1]++;
        }
    }

    public static void count(String stage) {
        if (!enabled) return;
        synchronized (STAGES) {
            long[] acc = STAGES.computeIfAbsent(stage, k -> new long[2]);
            acc[1]++;
        }
    }

    public static String report() {
        java.util.List<Map.Entry<String, long[]>> rows;
        synchronized (STAGES) {
            rows = new java.util.ArrayList<>(STAGES.entrySet());
        }
        rows.sort((a, b) -> Long.compare(b.getValue()[0], a.getValue()[0]));

        long grand = 0;
        for (Map.Entry<String, long[]> e : rows) grand = Math.max(grand, e.getValue()[0]);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-28s %11s %9s %10s %7s%n",
                "stage (slowest first)", "total_ms", "calls", "us/call", "share"));
        for (Map.Entry<String, long[]> e : rows) {
            long totalNs = e.getValue()[0];
            long calls = e.getValue()[1];
            double totalMs = totalNs / 1_000_000.0;
            double perUs = calls > 0 ? (totalNs / 1000.0) / calls : 0;
            double share = grand > 0 ? (100.0 * totalNs / grand) : 0;
            if (totalNs == 0) {
                sb.append(String.format("%-28s %11s %9d %10s %7s%n",
                        e.getKey(), "-", calls, "-", "-"));
            } else {
                sb.append(String.format("%-28s %11.2f %9d %10.1f %6.1f%%%n",
                        e.getKey(), totalMs, calls, perUs, share));
            }
        }
        return sb.toString();
    }
}
