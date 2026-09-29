package teacommontea.util;

import org.bukkit.Bukkit;

public final class ServerVersion {

    public static final String MINIMUM = "1.21.6";
    public static final String MAXIMUM = "26.3";

    private static final int[] RUNNING = detect();

    private ServerVersion() {}

    public static String running() {
        return RUNNING[0] + "." + RUNNING[1] + (RUNNING[2] == 0 ? "" : "." + RUNNING[2]);
    }

    public static boolean known() {
        return RUNNING[0] > 0;
    }

    public static boolean belowMinimum() {
        return known() && compare(RUNNING, parse(MINIMUM)) < 0;
    }

    public static boolean aboveMaximum() {
        return known() && compare(RUNNING, parse(MAXIMUM)) > 0;
    }

    private static int[] detect() {
        String full = Bukkit.getVersion();
        int mc = full.indexOf("(MC: ");
        if (mc >= 0) {
            int close = full.indexOf(')', mc);
            int[] v = parse(full.substring(mc + 5, close < 0 ? full.length() : close));
            if (v[0] > 0) return v;
        }
        return parse(Bukkit.getBukkitVersion());
    }

    static int[] parse(String raw) {
        int[] out = new int[3];
        int end = raw.indexOf('-');
        String[] parts = (end < 0 ? raw : raw.substring(0, end)).split("\\.");
        for (int i = 0; i < out.length && i < parts.length; i++) {
            try {
                out[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException e) {
                break;
            }
        }
        return out;
    }

    private static int compare(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return Integer.compare(a[i], b[i]);
        }
        return 0;
    }
}
