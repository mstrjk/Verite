package teacommontea.veritechasse.Reality;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class MovementProbe implements Listener {

    public static final int TICK_PERIOD = 1;

    private final JavaPlugin plugin;
    private final Logger logger;

    private final Map<UUID, Sample> lastTickEnd = new HashMap<>();
    private final Map<UUID, Sample> lastTickStart = new HashMap<>();
    private final Map<UUID, Integer> movesThisTick = new HashMap<>();
    private final Map<UUID, Double> moveSumThisTick = new HashMap<>();

    private BukkitTask startTask;
    private BukkitTask endTask;
    private long schedulerTick;
    private boolean running;

    public MovementProbe(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void start() {
        this.running = true;
        this.logger.info("=== MOVEMENT PROBE ARMED ===");
        this.logger.info("comparing position sources per server tick");
        this.startTask = Bukkit.getScheduler().runTaskTimer(
            this.plugin, this::onTickStart, TICK_PERIOD, TICK_PERIOD);
        this.endTask = Bukkit.getScheduler().runTaskTimer(
            this.plugin, this::onTickEnd, TICK_PERIOD + 1, TICK_PERIOD);
    }

    public void stop() {
        this.running = false;
        if (this.startTask != null) {
            this.startTask.cancel();
            this.startTask = null;
        }
        if (this.endTask != null) {
            this.endTask.cancel();
            this.endTask = null;
        }
    }

    public boolean running() {
        return this.running;
    }

    private void onTickStart() {
        this.schedulerTick++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.lastTickStart.put(player.getUniqueId(), Sample.of(player));
        }
    }

    private void onTickEnd() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            reportPlayer(player);
        }
        this.movesThisTick.clear();
        this.moveSumThisTick.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        Integer count = this.movesThisTick.get(id);
        this.movesThisTick.put(id, Integer.valueOf(count == null ? 1 : count.intValue() + 1));

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) {
            return;
        }
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        Double sum = this.moveSumThisTick.get(id);
        this.moveSumThisTick.put(id,
            Double.valueOf(sum == null ? distance : sum.doubleValue() + distance));
    }

    private void reportPlayer(Player player) {
        UUID id = player.getUniqueId();
        Sample now = Sample.of(player);
        Sample previousEnd = this.lastTickEnd.get(id);
        Sample thisStart = this.lastTickStart.get(id);
        this.lastTickEnd.put(id, now);

        if (previousEnd == null) {
            this.logger.info("[probe] " + player.getName() + " baseline");
            return;
        }

        int moves = count(this.movesThisTick, id);
        double moveSum = sum(this.moveSumThisTick, id);

        double endToEnd = now.horizontalTo(previousEnd);
        double startToEnd = thisStart == null ? -1.0D : now.horizontalTo(thisStart);
        int livedDelta = now.ticksLived - previousEnd.ticksLived;

        if (endToEnd < 1.0E-9D && moves == 0) {
            return;
        }

        StringBuilder line = new StringBuilder();
        line.append("[probe] ").append(player.getName());
        line.append(" | schedTick=").append(this.schedulerTick);
        line.append(" livedD=").append(livedDelta);
        line.append(" moves=").append(moves);
        line.append(" || A_endToEnd=").append(format(endToEnd));
        line.append(" B_moveSum=").append(format(moveSum));
        line.append(" C_startToEnd=").append(format(startToEnd));
        line.append(" D_velXZ=").append(format(now.velocityHorizontal));
        line.append(" E_dY=").append(format(now.y - previousEnd.y));
        line.append(" F_velY=").append(format(now.velocityY));
        line.append(" | onGroundFlag=").append(now.onGroundFlag);
        line.append(" support=").append(now.supported);
        line.append(" sprint=").append(now.sprinting);
        line.append(" fall=").append(format(now.fallDistance));
        line.append(" ping=").append(now.ping);
        this.logger.info(line.toString());

        if (moves > 1) {
            this.logger.warning("[probe] " + player.getName()
                + " COALESCED: " + moves + " move events in one server tick"
                + " (A=" + format(endToEnd) + " vs B=" + format(moveSum) + ")");
        }
        if (livedDelta != 1) {
            this.logger.warning("[probe] " + player.getName()
                + " TICK DRIFT: ticksLived advanced " + livedDelta
                + " across one scheduler tick");
        }
        if (Math.abs(endToEnd - moveSum) > 1.0E-6D && moves > 0) {
            this.logger.warning("[probe] " + player.getName()
                + " SOURCE DISAGREEMENT: endToEnd=" + format(endToEnd)
                + " moveSum=" + format(moveSum)
                + " delta=" + format(Math.abs(endToEnd - moveSum)));
        }
    }

    private static int count(Map<UUID, Integer> map, UUID id) {
        Integer value = map.get(id);
        return value == null ? 0 : value.intValue();
    }

    private static double sum(Map<UUID, Double> map, UUID id) {
        Double value = map.get(id);
        return value == null ? 0.0D : value.doubleValue();
    }

    private static String format(double value) {
        return String.format("%.5f", value);
    }

    private static final class Sample {
        private double x;
        private double y;
        private double z;
        private double velocityHorizontal;
        private double velocityY;
        private int ticksLived;
        private boolean onGroundFlag;
        private boolean supported;
        private boolean sprinting;
        private double fallDistance;
        private int ping;

        private static Sample of(Player player) {
            Location location = player.getLocation();
            Vector velocity = player.getVelocity();
            Sample sample = new Sample();
            sample.x = location.getX();
            sample.y = location.getY();
            sample.z = location.getZ();
            sample.velocityHorizontal = Math.sqrt(
                velocity.getX() * velocity.getX() + velocity.getZ() * velocity.getZ());
            sample.velocityY = velocity.getY();
            sample.ticksLived = player.getTicksLived();
            sample.onGroundFlag = ((org.bukkit.entity.Entity) player).isOnGround();
            sample.supported = SupportingBlock.present(player);
            sample.sprinting = player.isSprinting();
            sample.fallDistance = player.getFallDistance();
            sample.ping = player.getPing();
            return sample;
        }

        private double horizontalTo(Sample other) {
            double dx = this.x - other.x;
            double dz = this.z - other.z;
            return Math.sqrt(dx * dx + dz * dz);
        }
    }
}
