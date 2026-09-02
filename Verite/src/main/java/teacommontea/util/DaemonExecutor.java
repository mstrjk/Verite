package teacommontea.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public final class DaemonExecutor {

    private DaemonExecutor() {}

    public static ExecutorService single(String threadName) {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, threadName);
            t.setDaemon(true);
            return t;
        });
    }
}

