package teacommontea.util.sched;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;


public final class Sched {

    private Sched() {}

    interface Backend {
        TaskHandle forEntity(Entity entity, Runnable task);
        TaskHandle forEntityLater(Entity entity, Runnable task, long delayTicks);
        TaskHandle forEntityTimer(Entity entity, Runnable task, long delayTicks, long periodTicks);
        TaskHandle at(Location location, Runnable task);
        TaskHandle atLater(Location location, Runnable task, long delayTicks);
        TaskHandle atTimer(Location location, Runnable task, long delayTicks, long periodTicks);
        TaskHandle global(Runnable task);
        TaskHandle globalLater(Runnable task, long delayTicks);
        TaskHandle globalTimer(Runnable task, long delayTicks, long periodTicks);
        TaskHandle async(Runnable task);
        TaskHandle asyncLater(Runnable task, long delayMillis);
        TaskHandle asyncTimer(Runnable task, long delayMillis, long periodMillis);
    }

    private static volatile Backend backend;
    private static volatile Plugin plugin;

    public static void install(Plugin pl) {
        plugin = pl;
        Backend b = null;
        if (classPresent("io.papermc.paper.threadedregions.scheduler.RegionScheduler")
                && hasMethod("org.bukkit.Bukkit", "getGlobalRegionScheduler")) {
            try {
                Class<?> c = Class.forName("teacommontea.util.sched.FoliaBackend");
                java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor(Plugin.class);
                ctor.setAccessible(true);
                b = (Backend) ctor.newInstance(pl);
            } catch (Throwable t) {
                Throwable cause = t.getCause() != null ? t.getCause() : t;
                pl.getLogger().severe("Folia scheduler detected but its backend could not be built ("
                        + cause + "); Verite cannot schedule safely on this server.");
                throw cause instanceof RuntimeException re ? re : new RuntimeException(cause);
            }
        }
        if (b == null) {
            b = new BukkitBackend(pl);
        }
        backend = b;
    }

    public static boolean regionised() {
        return backend != null && backend.getClass().getSimpleName().equals("FoliaBackend");
    }

    public static TaskHandle executeFor(Entity entity, Runnable task) { return backend.forEntity(entity, task); }
    public static TaskHandle executeFor(Entity entity, Runnable task, long delayTicks) { return backend.forEntityLater(entity, task, delayTicks); }
    public static TaskHandle executeForRepeating(Entity entity, Runnable task, long delayTicks, long periodTicks) { return backend.forEntityTimer(entity, task, delayTicks, periodTicks); }

    public static TaskHandle executeAt(Location location, Runnable task) { return backend.at(location, task); }
    public static TaskHandle executeAt(Location location, Runnable task, long delayTicks) { return backend.atLater(location, task, delayTicks); }
    public static TaskHandle executeAtRepeating(Location location, Runnable task, long delayTicks, long periodTicks) { return backend.atTimer(location, task, delayTicks, periodTicks); }

    public static TaskHandle executeGlobal(Runnable task) { return backend.global(task); }
    public static TaskHandle executeGlobal(Runnable task, long delayTicks) { return backend.globalLater(task, delayTicks); }
    public static TaskHandle executeGlobalRepeating(Runnable task, long delayTicks, long periodTicks) { return backend.globalTimer(task, delayTicks, periodTicks); }

    public static TaskHandle executeAsync(Runnable task) { return backend.async(task); }
    public static TaskHandle executeAsync(Runnable task, long delayMillis) { return backend.asyncLater(task, delayMillis); }
    public static TaskHandle executeAsyncRepeating(Runnable task, long delayMillis, long periodMillis) { return backend.asyncTimer(task, delayMillis, periodMillis); }

    static Plugin plugin() { return plugin; }

    private static boolean classPresent(String name) {
        try { Class.forName(name); return true; } catch (Throwable t) { return false; }
    }

    private static boolean hasMethod(String className, String method) {
        try { Class.forName(className).getMethod(method); return true; } catch (Throwable t) { return false; }
    }
}
