package teacommontea.util.sched;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


final class FoliaBackend implements Sched.Backend {

    private final Plugin plugin;

    private final Object globalScheduler;
    private final Object regionScheduler;
    private final Object asyncScheduler;

    private final Class<?> scheduledTaskClass;

    // global region scheduler
    private final Method gRun, gRunDelayed, gRunAtFixedRate;
    // region scheduler (Location-based)
    private final Method rRun, rRunDelayed, rRunAtFixedRate;
    // async scheduler
    private final Method aRunNow, aRunDelayed, aRunAtFixedRate;
    // entity scheduler
    private final Method entityGetScheduler;
    private final Method eRun, eRunDelayed, eRunAtFixedRate;
    private final Method taskCancel;

    FoliaBackend(Plugin plugin) throws Throwable {
        this.plugin = plugin;

        this.globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
        this.regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
        this.asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);

        this.scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
        this.taskCancel = scheduledTaskClass.getMethod("cancel");

        Class<?> global = globalScheduler.getClass();
        this.gRun = method(global, "run", Plugin.class, Consumer.class);
        this.gRunDelayed = method(global, "runDelayed", Plugin.class, Consumer.class, long.class);
        this.gRunAtFixedRate = method(global, "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class);

        Class<?> region = regionScheduler.getClass();
        this.rRun = method(region, "run", Plugin.class, Location.class, Consumer.class);
        this.rRunDelayed = method(region, "runDelayed", Plugin.class, Location.class, Consumer.class, long.class);
        this.rRunAtFixedRate = method(region, "runAtFixedRate", Plugin.class, Location.class, Consumer.class, long.class, long.class);

        Class<?> async = asyncScheduler.getClass();
        this.aRunNow = method(async, "runNow", Plugin.class, Consumer.class);
        this.aRunDelayed = method(async, "runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class);
        this.aRunAtFixedRate = method(async, "runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class);

        this.entityGetScheduler = Entity.class.getMethod("getScheduler");
        Class<?> entitySched = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
        this.eRun = method(entitySched, "run", Plugin.class, Consumer.class, Runnable.class);
        this.eRunDelayed = method(entitySched, "runDelayed", Plugin.class, Consumer.class, Runnable.class, long.class);
        this.eRunAtFixedRate = method(entitySched, "runAtFixedRate", Plugin.class, Consumer.class, Runnable.class, long.class, long.class);
    }

    private static Method method(Class<?> owner, String name, Class<?>... params) throws NoSuchMethodException {
        Method m = owner.getMethod(name, params);
        m.setAccessible(true);
        return m;
    }

    // adapt a Runnable into the scheduler's Consumer<ScheduledTask> callback
    private Consumer<Object> cb(Runnable task) {
        return ignored -> task.run();
    }

    private TaskHandle wrap(Object scheduledTask) {
        if (scheduledTask == null) return TaskHandle.NONE;
        return () -> {
            try { taskCancel.invoke(scheduledTask); } catch (Throwable ignored) { }
        };
    }

    private TaskHandle invokeWrap(Method m, Object receiver, Object... args) {
        try {
            return wrap(m.invoke(receiver, args));
        } catch (Throwable t) {
            return TaskHandle.NONE;
        }
    }

    // ---- entity ----
    @Override public TaskHandle forEntity(Entity entity, Runnable task) {
        Object es = entityScheduler(entity);
        return es == null ? TaskHandle.NONE : invokeWrap(eRun, es, plugin, cb(task), (Runnable) null);
    }
    @Override public TaskHandle forEntityLater(Entity entity, Runnable task, long delayTicks) {
        Object es = entityScheduler(entity);
        return es == null ? TaskHandle.NONE : invokeWrap(eRunDelayed, es, plugin, cb(task), (Runnable) null, Math.max(1L, delayTicks));
    }
    @Override public TaskHandle forEntityTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        if (periodTicks <= 0) return forEntityLater(entity, task, delayTicks);
        Object es = entityScheduler(entity);
        return es == null ? TaskHandle.NONE : invokeWrap(eRunAtFixedRate, es, plugin, cb(task), (Runnable) null, Math.max(1L, delayTicks), periodTicks);
    }

    private Object entityScheduler(Entity entity) {
        try { return entityGetScheduler.invoke(entity); } catch (Throwable t) { return null; }
    }

    // ---- location / region ----
    @Override public TaskHandle at(Location location, Runnable task) {
        return invokeWrap(rRun, regionScheduler, plugin, location, cb(task));
    }
    @Override public TaskHandle atLater(Location location, Runnable task, long delayTicks) {
        return invokeWrap(rRunDelayed, regionScheduler, plugin, location, cb(task), Math.max(1L, delayTicks));
    }
    @Override public TaskHandle atTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        if (periodTicks <= 0) return atLater(location, task, delayTicks);
        return invokeWrap(rRunAtFixedRate, regionScheduler, plugin, location, cb(task), Math.max(1L, delayTicks), periodTicks);
    }

    // ---- global ----
    @Override public TaskHandle global(Runnable task) {
        return invokeWrap(gRun, globalScheduler, plugin, cb(task));
    }
    @Override public TaskHandle globalLater(Runnable task, long delayTicks) {
        return invokeWrap(gRunDelayed, globalScheduler, plugin, cb(task), Math.max(1L, delayTicks));
    }
    @Override public TaskHandle globalTimer(Runnable task, long delayTicks, long periodTicks) {
        if (periodTicks <= 0) return globalLater(task, delayTicks);
        return invokeWrap(gRunAtFixedRate, globalScheduler, plugin, cb(task), Math.max(1L, delayTicks), periodTicks);
    }

    // ---- async ----
    @Override public TaskHandle async(Runnable task) {
        return invokeWrap(aRunNow, asyncScheduler, plugin, cb(task));
    }
    @Override public TaskHandle asyncLater(Runnable task, long delayMillis) {
        return invokeWrap(aRunDelayed, asyncScheduler, plugin, cb(task), Math.max(1L, delayMillis), TimeUnit.MILLISECONDS);
    }
    @Override public TaskHandle asyncTimer(Runnable task, long delayMillis, long periodMillis) {
        if (periodMillis <= 0) return asyncLater(task, delayMillis);
        return invokeWrap(aRunAtFixedRate, asyncScheduler, plugin, cb(task), Math.max(1L, delayMillis), Math.max(1L, periodMillis), TimeUnit.MILLISECONDS);
    }
}
