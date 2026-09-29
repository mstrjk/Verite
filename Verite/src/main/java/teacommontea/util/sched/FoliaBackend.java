package teacommontea.util.sched;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class FoliaBackend implements Sched.Backend {

    private final Plugin plugin;
    private final GlobalRegionScheduler globalScheduler;
    private final RegionScheduler regionScheduler;
    private final AsyncScheduler asyncScheduler;

    FoliaBackend(Plugin plugin) {
        this.plugin = plugin;
        this.globalScheduler = Bukkit.getGlobalRegionScheduler();
        this.regionScheduler = Bukkit.getRegionScheduler();
        this.asyncScheduler = Bukkit.getAsyncScheduler();
    }

    private static Consumer<ScheduledTask> cb(Runnable task) {
        return ignored -> task.run();
    }

    private static TaskHandle wrap(Supplier<ScheduledTask> schedule) {
        try {
            ScheduledTask scheduledTask = schedule.get();
            return scheduledTask == null ? TaskHandle.NONE : scheduledTask::cancel;
        } catch (RuntimeException e) {
            return TaskHandle.NONE;
        }
    }

    @Override public TaskHandle forEntity(Entity entity, Runnable task) {
        return wrap(() -> entity.getScheduler().run(plugin, cb(task), null));
    }
    @Override public TaskHandle forEntityLater(Entity entity, Runnable task, long delayTicks) {
        return wrap(() -> entity.getScheduler().runDelayed(plugin, cb(task), null, Math.max(1L, delayTicks)));
    }
    @Override public TaskHandle forEntityTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) {
        if (periodTicks <= 0) return forEntityLater(entity, task, delayTicks);
        return wrap(() -> entity.getScheduler().runAtFixedRate(plugin, cb(task), null, Math.max(1L, delayTicks), periodTicks));
    }

    @Override public TaskHandle at(Location location, Runnable task) {
        return wrap(() -> regionScheduler.run(plugin, location, cb(task)));
    }
    @Override public TaskHandle atLater(Location location, Runnable task, long delayTicks) {
        return wrap(() -> regionScheduler.runDelayed(plugin, location, cb(task), Math.max(1L, delayTicks)));
    }
    @Override public TaskHandle atTimer(Location location, Runnable task, long delayTicks, long periodTicks) {
        if (periodTicks <= 0) return atLater(location, task, delayTicks);
        return wrap(() -> regionScheduler.runAtFixedRate(plugin, location, cb(task), Math.max(1L, delayTicks), periodTicks));
    }

    @Override public TaskHandle global(Runnable task) {
        return wrap(() -> globalScheduler.run(plugin, cb(task)));
    }
    @Override public TaskHandle globalLater(Runnable task, long delayTicks) {
        return wrap(() -> globalScheduler.runDelayed(plugin, cb(task), Math.max(1L, delayTicks)));
    }
    @Override public TaskHandle globalTimer(Runnable task, long delayTicks, long periodTicks) {
        if (periodTicks <= 0) return globalLater(task, delayTicks);
        return wrap(() -> globalScheduler.runAtFixedRate(plugin, cb(task), Math.max(1L, delayTicks), periodTicks));
    }

    @Override public TaskHandle async(Runnable task) {
        return wrap(() -> asyncScheduler.runNow(plugin, cb(task)));
    }
    @Override public TaskHandle asyncLater(Runnable task, long delayMillis) {
        return wrap(() -> asyncScheduler.runDelayed(plugin, cb(task), Math.max(1L, delayMillis), TimeUnit.MILLISECONDS));
    }
    @Override public TaskHandle asyncTimer(Runnable task, long delayMillis, long periodMillis) {
        if (periodMillis <= 0) return asyncLater(task, delayMillis);
        return wrap(() -> asyncScheduler.runAtFixedRate(plugin, cb(task), Math.max(1L, delayMillis),
                Math.max(1L, periodMillis), TimeUnit.MILLISECONDS));
    }
}
