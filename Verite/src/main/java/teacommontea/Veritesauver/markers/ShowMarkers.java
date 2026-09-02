package teacommontea.veritesauver.markers;

import teacommontea.util.Colours;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import teacommontea.util.Messages;
import teacommontea.util.KvStore;
import teacommontea.util.Scope;

public final class ShowMarkers implements CommandExecutor, TabCompleter, Listener {

    private static final int MOVE_ITERATIONS = 667;
    private static final int MOVE_INNER_RADIUS = 16;
    private static final int MOVE_OUTER_RADIUS = 32;

    private static final int ALL_ITERATIONS = 6000;
    private static final int ALL_INNER_RADIUS = 48;
    private static final int ALL_OUTER_RADIUS = 96;

    private static final long PERIOD = 1L;

    private static final int FADE_TICKS = 80;
    private static final double MOVE_SAMPLE_DENSITY = (double) MOVE_ITERATIONS / (MOVE_OUTER_RADIUS * MOVE_OUTER_RADIUS);
    private static final int FADE_MAX_ITERATIONS = 20000;

    private final Plugin plugin;
    private final Messages messages = new Messages();
    private final Scope scope;
    private final Material target;
    private final String noun;
    private final String label;
    private final String permission;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public ShowMarkers(Plugin plugin, KvStore store, Material target, String noun, String label, String permission) {
        this.plugin = plugin;
        this.scope = store.scope("show_" + target.name().toLowerCase(java.util.Locale.ROOT));
        this.target = target;
        this.noun = noun;
        this.label = label;
        this.permission = permission;
    }

    private enum Mode { MOVE, ALL }

    private static final class Session {
        final Mode mode;
        teacommontea.util.sched.TaskHandle task;
        int fadeLeft = -1;
        int fadeStartRadius;
        Session(Mode mode) {
            this.mode = mode;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
        Messages m = messages;
        if (!sender.hasPermission(permission)) {
            sender.spigot().sendMessage(m.prefixed(Messages.DENY_PERMISSION));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.spigot().sendMessage(m.prefixed(Colours.WARNING + "Players only."));
            return true;
        }

        UUID id = player.getUniqueId();
        String modeArg = args.length >= 1 ? args[0].toLowerCase(Locale.ROOT) : "";

        if (modeArg.isEmpty()) {
            switch (fadeOff(id, true)) {
                case STOPPED -> sender.spigot().sendMessage(m.prefixed(Colours.BRAND + "Stopped showing " + noun + "."));
                case ALREADY_STOPPING -> sender.spigot().sendMessage(m.prefixed(Colours.BRAND_ACCENT_SECONDARY + "Already fading out."));
                case NOT_RUNNING -> {
                    start(player, Mode.MOVE, true);
                    sender.spigot().sendMessage(m.prefixed(Colours.BRAND + "Showing " + noun + " near you as you move. Run " + Colours.BRAND_ACCENT_SECONDARY + "/" + label + " off" + Colours.BRAND + " to stop."));
                }
            }
            return true;
        }

        if (modeArg.equals("off")) {
            switch (fadeOff(id, true)) {
                case STOPPED -> sender.spigot().sendMessage(m.prefixed(Colours.BRAND + "Stopped showing " + noun + "."));
                case ALREADY_STOPPING -> sender.spigot().sendMessage(m.prefixed(Colours.BRAND_ACCENT_SECONDARY + "Already fading out."));
                case NOT_RUNNING -> sender.spigot().sendMessage(m.prefixed(Colours.BRAND_ACCENT_SECONDARY + "You were not showing " + noun + "."));
            }
            return true;
        }

        Mode mode;
        if (modeArg.equals("move")) {
            mode = Mode.MOVE;
        } else if (modeArg.equals("all")) {
            mode = Mode.ALL;
        } else {
            sender.spigot().sendMessage(m.prefixed(Colours.WARNING + "Usage: " + Colours.BRAND_ACCENT_SECONDARY + "/" + label + " all|move|off"));
            return true;
        }

        start(player, mode, true);
        String where = mode == Mode.ALL ? "across your view distance" : "near you as you move";
        sender.spigot().sendMessage(m.prefixed(Colours.BRAND + "Showing " + noun + " " + Colours.BRAND_ACCENT_SECONDARY + where
                + Colours.BRAND + ". Run " + Colours.BRAND_ACCENT_SECONDARY + "/" + label + " off" + Colours.BRAND + " to stop."));
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        restore(event.getPlayer());
    }

    public void resumeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            restore(player);
        }
    }

    private void restore(Player player) {
        String blob = scope.getString(player.getUniqueId().toString(), null);
        if (blob == null || blob.isEmpty()) {
            return;
        }
        Mode mode;
        try {
            mode = Mode.valueOf(blob);
        } catch (IllegalArgumentException e) {
            scope.delete(player.getUniqueId().toString());
            return;
        }
        start(player, mode, false);
    }

    private void start(Player player, Mode mode, boolean persist) {
        UUID id = player.getUniqueId();
        stop(id, false);
        Session session = new Session(mode);
        session.task = teacommontea.util.sched.Sched.executeForRepeating(player, () -> tick(player, session), 0L, PERIOD);
        sessions.put(id, session);
        if (persist) {
            scope.set(id.toString(), mode.name());
        }
    }

    private boolean stop(UUID id, boolean persist) {
        Session removed = sessions.remove(id);
        if (persist) {
            scope.delete(id.toString());
        }
        if (removed == null) {
            return false;
        }
        if (removed.task != null) {
            removed.task.cancel();
        }
        return true;
    }

    private void endSession(UUID id, Session session) {
        if (session.task != null) {
            session.task.cancel();
        }
        sessions.remove(id, session);
    }

    private enum OffResult { STOPPED, ALREADY_STOPPING, NOT_RUNNING }

    private OffResult fadeOff(UUID id, boolean persist) {
        if (persist) {
            scope.delete(id.toString());
        }
        Session session = sessions.get(id);
        if (session == null) {
            return OffResult.NOT_RUNNING;
        }
        if (session.fadeLeft >= 0) {
            return OffResult.ALREADY_STOPPING;
        }
        session.fadeLeft = FADE_TICKS;
        session.fadeStartRadius = session.mode == Mode.ALL ? ALL_OUTER_RADIUS : MOVE_OUTER_RADIUS;
        return OffResult.STOPPED;
    }

    private void tick(Player player, Session session) {
        if (sessions.get(player.getUniqueId()) != session) {
            if (session.task != null) {
                session.task.cancel();
            }
            return;
        }
        if (!player.isOnline()) {
            endSession(player.getUniqueId(), session);
            return;
        }
        if (session.fadeLeft == 0) {
            endSession(player.getUniqueId(), session);
            return;
        }
        if (session.fadeLeft > 0) {
            fadeReveal(player, session);
            session.fadeLeft -= 1;
        } else if (session.mode == Mode.ALL) {
            reveal(player, ALL_ITERATIONS, ALL_INNER_RADIUS, ALL_OUTER_RADIUS);
        } else {
            reveal(player, MOVE_ITERATIONS, MOVE_INNER_RADIUS, MOVE_OUTER_RADIUS);
        }
    }

    private void fadeReveal(Player player, Session session) {
        World world = player.getWorld();
        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double fraction = (double) session.fadeLeft / FADE_TICKS;
        int outer = Math.max(1, (int) Math.round(session.fadeStartRadius * fraction));
        int inner = Math.max(1, outer / 2);
        int iterations = (int) Math.min(FADE_MAX_ITERATIONS, Math.round(MOVE_SAMPLE_DENSITY * outer * outer));
        for (int i = 0; i < iterations; i++) {
            sampleOnce(player, world, px, py, pz, inner, rng);
            sampleOnce(player, world, px, py, pz, outer, rng);
        }
    }

    private void reveal(Player player, int iterations, int innerRadius, int outerRadius) {
        World world = player.getWorld();
        int px = player.getLocation().getBlockX();
        int py = player.getLocation().getBlockY();
        int pz = player.getLocation().getBlockZ();
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int i = 0; i < iterations; i++) {
            sampleOnce(player, world, px, py, pz, innerRadius, rng);
            sampleOnce(player, world, px, py, pz, outerRadius, rng);
        }
    }

    private void sampleOnce(Player player, World world, int px, int py, int pz, int radius, ThreadLocalRandom rng) {
        int x = px + rng.nextInt(radius) - rng.nextInt(radius);
        int y = py + rng.nextInt(radius) - rng.nextInt(radius);
        int z = pz + rng.nextInt(radius) - rng.nextInt(radius);
        org.bukkit.block.Block block = world.getBlockAt(x, y, z);
        if (block.getType() == target) {
            emitForced(player, x + 0.5D, y + 0.5D, z + 0.5D, block.getBlockData());
        }
    }

    private void emitForced(Player player, double x, double y, double z, BlockData shownAs) {
        new ParticleBuilder(Particle.BLOCK_MARKER)
                .location(player.getWorld(), x, y, z)
                .count(1)
                .offset(0.0D, 0.0D, 0.0D)
                .extra(0.0D)
                .data(shownAs)
                .receivers(player)
                .force(true)
                .spawn();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String option : new String[] { "all", "move", "off" }) {
                if (option.startsWith(prefix)) {
                    out.add(option);
                }
            }
        }
        return out;
    }
}
