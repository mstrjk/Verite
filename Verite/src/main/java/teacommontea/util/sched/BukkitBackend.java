package teacommontea.util.sched;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.TimeUnit;


final class BukkitBackend implements Sched.Backend {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    BukkitBackend(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = Bukkit.getScheduler();
    }

    private TaskHandle id(int taskId) {
        return () -> scheduler.cancelTask(taskId);
    }

    private static long msToTicks(long ms) {
        long ticks = ms / 50L;
        return ticks < 1 ? 1 : ticks;
    }

    @Override public TaskHandle forEntity(Entity entity, Runnable task) { return global(task); }
    @Override public TaskHandle forEntityLater(Entity entity, Runnable task, long delayTicks) { return globalLater(task, delayTicks); }
    @Override public TaskHandle forEntityTimer(Entity entity, Runnable task, long delayTicks, long periodTicks) { return globalTimer(task, delayTicks, periodTicks); }

    @Override public TaskHandle at(Location location, Runnable task) { return global(task); }
    @Override public TaskHandle atLater(Location location, Runnable task, long delayTicks) { return globalLater(task, delayTicks); }
    @Override public TaskHandle atTimer(Location location, Runnable task, long delayTicks, long periodTicks) { return globalTimer(task, delayTicks, periodTicks); }

    @Override public TaskHandle global(Runnable task) {
        return id(scheduler.runTask(plugin, task).getTaskId());
    }

    @Override public TaskHandle globalLater(Runnable task, long delayTicks) {
        return id(scheduler.runTaskLater(plugin, task, Math.max(1L, delayTicks)).getTaskId());
    }

    @Override public TaskHandle globalTimer(Runnable task, long delayTicks, long periodTicks) {
        if (periodTicks <= 0) return globalLater(task, delayTicks);
        return id(scheduler.runTaskTimer(plugin, task, Math.max(0L, delayTicks), periodTicks).getTaskId());
    }

    @Override public TaskHandle async(Runnable task) {
        return id(scheduler.runTaskAsynchronously(plugin, task).getTaskId());
    }

    @Override public TaskHandle asyncLater(Runnable task, long delayMillis) {
        return id(scheduler.runTaskLaterAsynchronously(plugin, task, msToTicks(delayMillis)).getTaskId());
    }

    @Override public TaskHandle asyncTimer(Runnable task, long delayMillis, long periodMillis) {
        if (periodMillis <= 0) return asyncLater(task, delayMillis);
        return id(scheduler.runTaskTimerAsynchronously(plugin, task, msToTicks(delayMillis), msToTicks(periodMillis)).getTaskId());
    }
}
