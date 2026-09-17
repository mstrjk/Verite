package teacommontea.veritechasse.Reality;

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

import teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerGamemode.ModeAbilities;
import teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerRespawn.RespawnRules;
import teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerTicks.TimerReality;
import teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerClientToServerForgiveness.PacketBatching;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness.ServerHealth;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerClientForgiveness.Verdict;
import teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerTicks.TickRate;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerTeleport.TeleportReality;
import teacommontea.veritechasse.Vanilla.Protocol;

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
    private final Map<UUID, Integer> packetsThisWindow = new HashMap<>();
    private final Map<UUID, PlayerSnapshot> latestThisTick = new HashMap<>();
    private final Map<UUID, Long> lastPacketTick = new HashMap<>();
    private final Map<String, Integer> discardCounts = new HashMap<>();
    private final java.util.Set<UUID> gateBlocked = new java.util.HashSet<>();
    private final Map<UUID, Long> lastBadPacketLog = new HashMap<>();
    private final Map<UUID, Double> violationLevels = new HashMap<>();

    public static final long BAD_PACKET_LOG_INTERVAL = 100L;

    public static final int TIMER_WINDOW_TICKS = 40;

    private long windowStartMillis = System.currentTimeMillis();
    private long lastTickMillis = System.currentTimeMillis();
    private long worstStallMillis;

    private int lastServerTick = -1;
    private int serverTicksThisWindow;
    private boolean serverTickStalled;

    private BukkitTask task;
    private long tick;
    private boolean verbose;
    private InteractionMonitor interactions;

    public RealityMonitor(JavaPlugin plugin, Protocol protocol) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.check = new RealityCheck(protocol);
    }

    public void start() {
        this.logger.info("Reality monitor starting.");
        this.logger.info("  capabilities: " + this.check.describeCapabilities());
        this.logger.info("  verbose diagnostics "
            + (this.verbose ? "ON" : "OFF"));
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
        boolean changed = this.verbose != verbose;
        this.verbose = verbose;
        if (changed) {
            this.logger.info("Reality monitor verbose logging "
                + (verbose ? "ON" : "OFF"));
        }
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
        evaluateGates();
        sampleStationaryPlayers();
        this.tick++;
        this.packetsThisTick.clear();
        if (this.interactions != null) {
            this.interactions.setTick(this.tick);
        }

        long now = System.currentTimeMillis();
        long sinceLastTick = now - this.lastTickMillis;
        this.lastTickMillis = now;
        if (sinceLastTick > this.worstStallMillis) {
            this.worstStallMillis = sinceLastTick;
        }

        int serverTick = Bukkit.getServer().getCurrentTick();
        if (this.lastServerTick >= 0) {
            int advanced = serverTick - this.lastServerTick;
            this.serverTicksThisWindow += advanced;
            this.serverTickStalled = advanced <= 0;
        }
        this.lastServerTick = serverTick;

        if (this.tick % TIMER_WINDOW_TICKS == 0L) {
            evaluateTimers(now);
        }
    }

    private void evaluateTimers(long now) {
        double elapsedMillis = (double) (now - this.windowStartMillis);
        float tickrate = observedServerTickRate(elapsedMillis);
        long behindNanos = behindTimeNanos(elapsedMillis, tickrate);

        for (Map.Entry<UUID, Integer> entry : this.packetsThisWindow.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            reportTimer(player, entry.getValue().intValue(),
                elapsedMillis, tickrate, behindNanos);
        }

        this.packetsThisWindow.clear();
        this.windowStartMillis = now;
        this.worstStallMillis = 0L;
        this.serverTicksThisWindow = 0;
    }

    private long behindTimeNanos(double elapsedMillis, float tickrate) {
        double expectedTickMillis = (double) MILLIS_PER_SECOND / (double) tickrate;
        double stallBeyondOneTick = (double) this.worstStallMillis - expectedTickMillis;
        if (stallBeyondOneTick <= 0.0D) {
            return 0L;
        }
        return (long) (stallBeyondOneTick * (double) NANOS_PER_MILLI);
    }

    public static final long NANOS_PER_MILLI = 1000000L;

    private void reportTimer(
            Player player,
            int packets,
            double elapsedMillis,
            float tickrate,
            long behindNanos) {
        if (elapsedMillis <= 0.0D) {
            return;
        }
        long nanosPerTick = TickRate.nanosecondsPerTick(tickrate, this.check.protocol());

        if (!TimerReality.evidenceIsAdmissible(
                false, false, behindNanos, nanosPerTick, 0, 0, this.check.protocol())) {
            return;
        }
        if (!TimerReality.rateExceedsReality(
                packets, elapsedMillis, tickrate, TIMER_TOLERANCE, this.check.protocol())) {
            return;
        }

        double ratio = TimerReality.observedRateRatio(
            packets, elapsedMillis, tickrate, this.check.protocol());
        count(TimerReality.KEY);
        if (this.verbose) {
            this.logger.info("[" + player.getName() + "] " + TimerReality.KEY
                + ": " + packets + " move packets in " + elapsedMillis + "ms at "
                + tickrate + "tps, ratio " + ratio);
        }
    }

    public static final double TIMER_TOLERANCE = 0.3D;

    public static final long MILLIS_PER_SECOND = 1000L;

    private float observedServerTickRate(double elapsedMillis) {
        if (elapsedMillis <= 0.0D || this.serverTicksThisWindow <= 0) {
            return TickRate.DEFAULT_TICKRATE;
        }
        double observed = (double) this.serverTicksThisWindow
            * (double) MILLIS_PER_SECOND / elapsedMillis;
        return TickRate.clampRate((float) observed);
    }

    private boolean serverIsSprinting(double elapsedMillis) {
        if (elapsedMillis <= 0.0D || this.serverTicksThisWindow <= 0) {
            return false;
        }
        double nominal = elapsedMillis
            / (double) MILLIS_PER_SECOND * (double) TickRate.DEFAULT_TICKRATE;
        return (double) this.serverTicksThisWindow > nominal * SPRINT_RATIO;
    }

    public static final double SPRINT_RATIO = 3.0D;

    private void sampleStationaryPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            if (!this.latestThisTick.containsKey(id)) {
                samplePlayer(player);
            }
            evaluateTickMovement(player);
        }
    }

    private void samplePlayer(Player player) {
        UUID id = player.getUniqueId();

        int index = nextPacketIndex(id);
        this.latestThisTick.put(id, PlayerSnapshot.of(player, this.tick, index));
    }

    private void evaluateTickMovement(Player player) {
        UUID id = player.getUniqueId();
        PlayerSnapshot current = this.latestThisTick.remove(id);
        if (current == null) {
            return;
        }

        PlayerSnapshot last = this.previous.get(id);
        this.previous.put(id, current);

        if (last == null) {
            if (this.verbose) {
                this.logger.info("[" + player.getName() + "] baseline established");
            }
            return;
        }
        if (isSuppressed(id)) {
            return;
        }
        if (this.gateBlocked.contains(id)) {
            return;
        }

        List<Violation> violations = this.check.evaluate(last, current);
        for (Violation violation : violations) {
            report(player, violation);
        }
        traceDropouts(player, id);
    }

    private void evaluateGates() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            Integer seen = this.packetsThisTick.get(id);
            int packets = seen == null ? 0 : seen.intValue();
            if (packets > 0) {
                this.lastPacketTick.put(id, Long.valueOf(this.tick));
            }

            String server = routeServerHealth();
            if (Verdict.isDiscard(server)) {
                this.gateBlocked.add(id);
                handleGateVerdict(player, id, server);
                continue;
            }
            this.gateBlocked.remove(id);
            reviewArrival(player, id, packets);
        }
    }

    private String routeServerHealth() {
        double windowMillis = (double) (System.currentTimeMillis() - this.windowStartMillis);
        float tickrate = observedServerTickRate(windowMillis);
        long nanosPerTick = TickRate.nanosecondsPerTick(tickrate, this.check.protocol());
        return ServerHealth.route(
            serverIsSprinting(windowMillis),
            this.serverTickStalled,
            behindTimeNanos(windowMillis, tickrate),
            nanosPerTick,
            0,
            0,
            (double) this.worstStallMillis,
            tickrate,
            this.check.protocol());
    }

    private void reviewArrival(Player player, UUID id, int packets) {
        PlayerSnapshot latest = this.previous.get(id);
        if (latest == null) {
            return;
        }
        if (packets == 0) {
            return;
        }

        long silentTicks = silentTicksFor(id);
        String verdict = PacketBatching.route(packets, silentTicks);
        if (!Verdict.isBadPackets(verdict)) {
            return;
        }
        countDiscard(Verdict.BAD_PACKETS);
        if (this.verbose) {
            Long last = this.lastBadPacketLog.get(id);
            if (last == null || this.tick - last.longValue() >= BAD_PACKET_LOG_INTERVAL) {
                this.lastBadPacketLog.put(id, Long.valueOf(this.tick));
                this.logger.info("[" + player.getName() + "] arrival: "
                    + packets + " packets this tick after " + silentTicks
                    + " silent ticks");
            }
        }
    }


    private long silentTicksFor(UUID id) {
        Long seen = this.lastPacketTick.get(id);
        return seen == null ? 0L : this.tick - seen.longValue();
    }


    private void handleGateVerdict(Player player, UUID id, String verdict) {
        if (Verdict.isDiscard(verdict)) {
            this.previous.remove(id);
        }
        if (Verdict.isBadPackets(verdict)) {
            countDiscard(Verdict.BAD_PACKETS);
            if (this.verbose) {
                Long last = this.lastBadPacketLog.get(id);
                if (last == null || this.tick - last.longValue() >= BAD_PACKET_LOG_INTERVAL) {
                    this.lastBadPacketLog.put(id, Long.valueOf(this.tick));
                    this.logger.info("[" + player.getName() + "] note arrival: "
                        + this.packetsThisTick.getOrDefault(id, Integer.valueOf(0))
                        + " packets this tick, last seen tick "
                        + this.lastPacketTick.get(id) + ", now " + this.tick);
                }
            }
            return;
        }
        countDiscard(Verdict.DISCARD);
        if (this.verbose && this.tick % CONTEXT_LOG_INTERVAL_TICKS == 0) {
            this.logger.info("[" + player.getName() + "] sample discarded, degradation detected");
        }
    }

    private void countDiscard(String label) {
        Integer existing = this.discardCounts.get(label);
        this.discardCounts.put(label,
            Integer.valueOf(existing == null ? 1 : existing.intValue() + 1));
    }

    private int nextPacketIndex(UUID id) {
        Integer seen = this.packetsThisTick.get(id);
        int index = seen == null ? 0 : seen.intValue();
        this.packetsThisTick.put(id, Integer.valueOf(index + 1));

        Integer window = this.packetsThisWindow.get(id);
        this.packetsThisWindow.put(id,
            Integer.valueOf(window == null ? 1 : window.intValue() + 1));
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

    public static final long TRACE_INTERVAL_TICKS = 20L;

    private final Map<UUID, Long> lastTraceTick = new HashMap<>();

    private void traceDropouts(Player player, UUID id) {
        List<String> lines = this.check.lastTrace();
        if (lines.isEmpty()) {
            return;
        }
        Long last = this.lastTraceTick.get(id);
        if (last != null && this.tick - last.longValue() < TRACE_INTERVAL_TICKS) {
            return;
        }
        this.lastTraceTick.put(id, Long.valueOf(this.tick));
        this.logger.warning("[" + player.getName() + "] DROPOUT over sinkable surface:");
        for (String line : lines) {
            this.logger.warning("    " + line);
        }
    }

    private void report(Player player, Violation violation) {
        UUID id = player.getUniqueId();
        double total = accrue(id, violation.weight());
        count(violation.key());
        this.logger.warning("[" + player.getName() + "] " + violation.title()
            + " +" + format(violation.weight())
            + " (" + violation.origin() + ", vl " + format(total) + "): "
            + violation.detail());
    }

    private double accrue(UUID id, double weight) {
        Double existing = this.violationLevels.get(id);
        double total = existing == null ? weight : existing.doubleValue() + weight;
        this.violationLevels.put(id, Double.valueOf(total));
        return total;
    }

    public double violationLevel(UUID id) {
        Double existing = this.violationLevels.get(id);
        return existing == null ? 0.0D : existing.doubleValue();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", Double.valueOf(value));
    }

    private void count(String label) {
        Integer existing = this.suspectCounts.get(label);
        this.suspectCounts.put(label, Integer.valueOf(existing == null ? 1 : existing.intValue() + 1));
    }

    public void reportTotals() {
        if (!this.discardCounts.isEmpty()) {
            this.logger.info("Forgiveness gate totals:");
            this.discardCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().intValue() - a.getValue().intValue())
                .forEach(entry -> this.logger.info(
                    "  " + entry.getKey() + ": " + entry.getValue()));
        }
        if (this.suspectCounts.isEmpty()) {
            this.logger.info("Reality monitor: no violations recorded.");
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
        suppress(event.getPlayer().getUniqueId(), JOIN_SUPPRESSION_TICKS);
    }

    public static final int JOIN_SUPPRESSION_TICKS = 20;

    public static final int RESPAWN_SUPPRESSION_TICKS = 5;

    public static final int WORLD_CHANGE_SUPPRESSION_TICKS = 10;

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(org.bukkit.event.player.PlayerRespawnEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        this.previous.remove(id);
        this.lastPacketTick.remove(id);
        suppress(id, RESPAWN_SUPPRESSION_TICKS);
        if (this.verbose) {
            this.logger.info("[" + event.getPlayer().getName()
                + "] respawn, baseline invalidated"
                + (RespawnRules.baselineIsInvalidatedByRespawn() ? "" : " (UNEXPECTED)"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(org.bukkit.event.player.PlayerChangedWorldEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        this.previous.remove(id);
        this.lastPacketTick.remove(id);
        suppress(id, WORLD_CHANGE_SUPPRESSION_TICKS);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameModeChange(org.bukkit.event.player.PlayerGameModeChangeEvent event) {
        String previousMode = event.getPlayer().getGameMode()
            .name().toLowerCase(java.util.Locale.ROOT);
        String nextMode = event.getNewGameMode().name().toLowerCase(java.util.Locale.ROOT);
        if (ModeAbilities.modeChangeInvalidatesBaseline(previousMode, nextMode)) {
            this.previous.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        this.previous.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(org.bukkit.event.player.PlayerBedEnterEvent event) {
        this.previous.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBedLeave(org.bukkit.event.player.PlayerBedLeaveEvent event) {
        this.previous.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVelocity(org.bukkit.event.player.PlayerVelocityEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        org.bukkit.util.Vector velocity = event.getVelocity();
        double horizontal = Math.sqrt(
            velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
        this.check.authoriseImpulse(id, horizontal, velocity.getY(), this.tick);
        this.previous.remove(id);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onToggleGlide(org.bukkit.event.entity.EntityToggleGlideEvent event) {
        if (event.getEntity() instanceof Player) {
            this.previous.remove(event.getEntity().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleEnter(org.bukkit.event.vehicle.VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player) {
            this.previous.remove(event.getEntered().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleExit(org.bukkit.event.vehicle.VehicleExitEvent event) {
        if (event.getExited() instanceof Player) {
            this.previous.remove(event.getExited().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.previous.remove(event.getPlayer().getUniqueId());
        this.suppressUntil.remove(event.getPlayer().getUniqueId());
        this.packetsThisWindow.remove(event.getPlayer().getUniqueId());
        this.packetsThisTick.remove(event.getPlayer().getUniqueId());
        this.lastPacketTick.remove(event.getPlayer().getUniqueId());
        this.gateBlocked.remove(event.getPlayer().getUniqueId());
        this.lastBadPacketLog.remove(event.getPlayer().getUniqueId());
        this.latestThisTick.remove(event.getPlayer().getUniqueId());
        this.check.forget(event.getPlayer().getUniqueId());
    }
}
