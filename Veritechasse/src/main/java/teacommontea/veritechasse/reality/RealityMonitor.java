package teacommontea.veritechasse.reality;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerTeleport.TeleportReality;
import teacommontea.veritechasse.vanilla.Protocol;

public final class RealityMonitor implements Listener {

    public static final int TICK_PERIOD = 1;

    public static final int CONTEXT_LOG_INTERVAL_TICKS = 40;

    private final JavaPlugin plugin;
    private final Logger logger;
    private final RealityCheck check;

    private final Map<UUID, PlayerSnapshot> previous = new HashMap<>();
    private final Map<UUID, Long> suppressUntil = new HashMap<>();
    private final Map<String, Integer> suspectCounts = new HashMap<>();
    private final Map<UUID, Integer> packetsThisTick = new HashMap<>();

    private BukkitTask task;
    private long tick;
    private boolean verbose = true;
    private InteractionMonitor interactions;

    public RealityMonitor(JavaPlugin plugin, Protocol protocol) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.check = new RealityCheck(protocol);
    }

    public void start() {
        this.logger.info("Reality monitor starting.");
        this.logger.info("  capabilities: " + this.check.describeCapabilities());
        this.logger.info("  logging every tick, context every "
            + CONTEXT_LOG_INTERVAL_TICKS + " ticks");
        this.task = Bukkit.getScheduler().runTaskTimer(
            this.plugin, this::sample, TICK_PERIOD, TICK_PERIOD);
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
        reportTotals();
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        this.logger.info("Reality monitor verbose logging " + (verbose ? "ON" : "OFF"));
    }

    public boolean verbose() {
        return this.verbose;
    }

    public void attachInteractions(InteractionMonitor interactions) {
        this.interactions = interactions;
        if (interactions != null) {
            interactions.setTick(this.tick);
        }
    }

    private void sample() {
        this.tick++;
        this.packetsThisTick.clear();
        if (this.interactions != null) {
            this.interactions.setTick(this.tick);
        }
    }

    private void samplePlayer(Player player) {
        UUID id = player.getUniqueId();

        int index = nextPacketIndex(id);
        PlayerSnapshot current = PlayerSnapshot.of(player, this.tick, index);
        PlayerSnapshot last = this.previous.get(id);
        this.previous.put(id, current);

        if (last == null) {
            this.logger.info("[" + player.getName() + "] baseline established");
            return;
        }
        if (isSuppressed(id)) {
            return;
        }

        List<Observation> observations = this.check.evaluate(last, current);
        for (Observation observation : observations) {
            report(player, observation);
        }
    }

    private int nextPacketIndex(UUID id) {
        Integer seen = this.packetsThisTick.get(id);
        int index = seen == null ? 0 : seen.intValue();
        this.packetsThisTick.put(id, Integer.valueOf(index + 1));
        return index;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }
        samplePlayer(event.getPlayer());
    }

    private boolean isSuppressed(UUID id) {
        Long until = this.suppressUntil.get(id);
        if (until == null) {
            return false;
        }
        if (this.tick >= until.longValue()) {
            this.suppressUntil.remove(id);
            return false;
        }
        return true;
    }

    private void report(Player player, Observation observation) {
        if (observation.isContext()) {
            if (this.verbose && this.tick % CONTEXT_LOG_INTERVAL_TICKS == 0) {
                this.logger.info("[" + player.getName() + "] " + observation.detail());
            }
            return;
        }
        if (observation.isNote()) {
            if (this.verbose) {
                this.logger.info("[" + player.getName() + "] note "
                    + observation.label() + ": " + observation.detail());
            }
            return;
        }

        count(observation.label());
        this.logger.warning("[" + player.getName() + "] SUSPECT "
            + observation.label() + ": " + observation.detail());
    }

    private void count(String label) {
        Integer existing = this.suspectCounts.get(label);
        this.suspectCounts.put(label, Integer.valueOf(existing == null ? 1 : existing.intValue() + 1));
    }

    public void reportTotals() {
        if (this.suspectCounts.isEmpty()) {
            this.logger.info("Reality monitor: no suspect observations recorded.");
            return;
        }
        this.logger.info("Reality monitor totals (highest first):");
        this.suspectCounts.entrySet().stream()
            .sorted((a, b) -> b.getValue().intValue() - a.getValue().intValue())
            .forEach(entry -> this.logger.info(
                "  " + entry.getKey() + ": " + entry.getValue()));
    }

    public void suppress(UUID id, int ticks) {
        this.suppressUntil.put(id, Long.valueOf(this.tick + ticks));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        String cause = event.getCause() == null
            ? null
            : event.getCause().name().toLowerCase(java.util.Locale.ROOT);
        UUID id = event.getPlayer().getUniqueId();
        this.previous.remove(id);
        suppress(id, 2);
        if (this.verbose) {
            this.logger.info("[" + event.getPlayer().getName() + "] teleport "
                + cause + ", baseline invalidated"
                + (TeleportReality.invalidatesPreviousPosition(cause, this.check.protocol())
                    ? "" : " (UNRECOGNISED CAUSE)"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        this.previous.remove(event.getPlayer().getUniqueId());
        suppress(event.getPlayer().getUniqueId(), 20);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.previous.remove(event.getPlayer().getUniqueId());
        this.suppressUntil.remove(event.getPlayer().getUniqueId());
    }
}
