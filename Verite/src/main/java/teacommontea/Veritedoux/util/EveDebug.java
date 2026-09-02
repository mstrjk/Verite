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
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-28s %12s %10s %12s%n", "stage", "total_us", "calls", "us/call"));
        synchronized (STAGES) {
            for (Map.Entry<String, long[]> e : STAGES.entrySet()) {
                long totalUs = e.getValue()[0] / 1000;
                long calls = e.getValue()[1];
                double per = calls > 0 ? (double) totalUs / calls : 0;
                sb.append(String.format("%-28s %12d %10d %12.1f%n", e.getKey(), totalUs, calls, per));
            }
        }
        return sb.toString();
    }
}
