package teacommontea.util;

public final class Trace {

    private static final int MAX_FRAMES = 12;

    private Trace() {}

    public static String of(Throwable t) {
        if (t == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(t);
        int frames = 0;
        for (StackTraceElement el : t.getStackTrace()) {
            if (frames++ >= MAX_FRAMES) {
                sb.append("\n    ...");
                break;
            }
            sb.append("\n    at ").append(el);
        }
        Throwable cause = t.getCause();
        int depth = 0;
        while (cause != null && cause != t && depth++ < 4) {
            sb.append("\nCaused by: ").append(cause);
            int inner = 0;
            for (StackTraceElement el : cause.getStackTrace()) {
                if (inner++ >= 4) {
                    sb.append("\n    ...");
                    break;
                }
                sb.append("\n    at ").append(el);
            }
            Throwable next = cause.getCause();
            cause = next == cause ? null : next;
        }
        return sb.toString();
    }
}
