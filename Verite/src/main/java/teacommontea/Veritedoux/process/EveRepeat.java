package teacommontea.veritedoux.process;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EveRepeat {

    private EveRepeat() {}

    private record Seen(String norm, long at) {}

    private static final ConcurrentHashMap<UUID, Deque<Seen>> HISTORY = new ConcurrentHashMap<>();

    private static boolean enabled = false;
    private static boolean blockExact = true;
    private static boolean blockNear = true;
    private static int historySize = 5;
    private static long windowMs = 5_000L;
    private static double similarityThreshold = 0.85;

    public static void configure(boolean on, boolean exact, boolean near,
                          int history, long window, double threshold) {
        enabled = on;
        blockExact = exact;
        blockNear = near;
        historySize = history;
        windowMs = window;
        similarityThreshold = threshold;
        HISTORY.clear();
    }

    public static void reset() {
        HISTORY.clear();
    }

    public static boolean blocks(UUID player, String message) {
        if (!enabled || player == null || message == null || message.isEmpty()) {
            return false;
        }
        if (!blockExact && !blockNear) {
            return false;
        }
        if (historySize == 0 || windowMs == 0L) {
            return false;
        }
        String norm = normalize(message);
        if (norm.isEmpty()) {
            return false;
        }
        long now = System.currentTimeMillis();
        Deque<Seen> hist = HISTORY.get(player);
        if (hist == null) {
            return false;
        }
        synchronized (hist) {
            boolean hit = false;
            java.util.Iterator<Seen> it = hist.iterator();
            while (it.hasNext()) {
                Seen s = it.next();
                if (windowMs >= 0L && now - s.at() > windowMs) {
                    it.remove();
                    continue;
                }
                if (blockExact && s.norm().equals(norm)) {
                    hit = true;
                }
                if (!hit && blockNear && similarity(s.norm(), norm) >= similarityThreshold) {
                    hit = true;
                }
            }
            return hit;
        }
    }

    public static void record(UUID player, String message) {
        if (!enabled || player == null || message == null || message.isEmpty()) {
            return;
        }
        if (!blockExact && !blockNear) {
            return;
        }
        if (historySize == 0 || windowMs == 0L) {
            return;
        }
        String norm = normalize(message);
        if (norm.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        Deque<Seen> hist = HISTORY.computeIfAbsent(player, k -> new ArrayDeque<>());
        synchronized (hist) {
            hist.addFirst(new Seen(norm, now));
            while (historySize >= 0 && hist.size() > historySize) {
                hist.removeLast();
            }
        }
    }

    private static String normalize(String message) {
        String stripped = teacommontea.veritedoux.util.EveText.stripEntities(message);
        return teacommontea.veritedoux.util.EveText.reduceRuns(teacommontea.veritedoux.util.EveText.foldAccents(stripped.toLowerCase()), 1);
    }

    public static double similarity(String a, String b) {
        if (a.equals(b)) {
            return 1.0;
        }
        int max = Math.max(a.length(), b.length());
        if (max == 0) {
            return 1.0;
        }
        return 1.0 - (double) distance(a, b) / max;
    }

    private static int distance(String a, String b) {
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= m; j++) {
                int cost = ca == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] swap = prev;
            prev = curr;
            curr = swap;
        }
        return prev[m];
    }
}
