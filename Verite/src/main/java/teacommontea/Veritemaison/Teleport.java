package teacommontea.veritemaison;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import teacommontea.util.Lang;
import teacommontea.util.Messages;
import teacommontea.util.sched.Sched;

final class Teleport {

    static final String LABEL = "teleport";
    static final List<String> ALIASES = List.of("tp");
    static final String TO_PLAYER_LABEL = "tp2p";
    static final String HERE_LABEL = "tphere";

    static final String SELF = "veritemaison.teleport";
    static final String OTHERS = "veritemaison.teleport.others";
    static final String TO_PLAYER = "veritemaison.tp2p";
    static final String HERE = "veritemaison.tphere";

    private static final double HORIZONTAL_LIMIT = 30_000_000;
    private static final double VERTICAL_LIMIT = 20_000_000;

    interface Mover {
        CompletableFuture<Boolean> move(Entity entity, Location to);
    }

    private final Mover mover;
    private final Messages messages = new Messages();

    Teleport(Mover mover) {
        this.mover = mover;
    }

    static boolean permitted(CommandSender sender) {
        return sender.hasPermission(SELF) || sender.hasPermission(OTHERS);
    }

    static boolean visible(CommandSender viewer, Entity entity) {
        return !(entity instanceof Player hidden)
                || !(viewer instanceof Player looker)
                || looker.equals(hidden)
                || looker.canSee(hidden);
    }

    void send(CommandSender to, String tagged) {
        teacommontea.util.text.Send.to(to, messages.prefixed(tagged));
    }

    int consoleMustName(CommandSender sender, String label) {
        send(sender, Lang.of("maison.teleport.console", "command", label));
        return 0;
    }

    int playersOnly(CommandSender sender, String label) {
        send(sender, Lang.of("maison.players.only", "command", label));
        return 0;
    }

    int toEntity(CommandSender sender, Entity executor, List<? extends Entity> targets, List<? extends Entity> destinations) {
        List<Entity> admitted = admit(sender, executor, targets);
        if (admitted == null) {
            return 0;
        }
        List<Entity> seen = seen(sender, destinations);
        if (seen.isEmpty()) {
            send(sender, Lang.of("maison.teleport.none"));
            return 0;
        }
        if (seen.size() > 1) {
            send(sender, Lang.of("maison.teleport.destination.single"));
            return 0;
        }
        return deliver(sender, executor, admitted, seen.get(0));
    }

    int toPlayer(CommandSender sender, Entity executor, List<? extends Entity> named, String label) {
        if (executor == null) {
            return playersOnly(sender, label);
        }
        Player player = onePlayer(sender, named);
        return player == null ? 0 : deliver(sender, executor, List.of(executor), player);
    }

    int here(CommandSender sender, Entity executor, List<? extends Entity> named, String label) {
        if (executor == null) {
            return playersOnly(sender, label);
        }
        Player player = onePlayer(sender, named);
        return player == null ? 0 : deliver(sender, executor, List.of(player), executor);
    }

    int toPosition(CommandSender sender, Entity executor, List<? extends Entity> targets,
                   World world, double x, double y, double z, Float yaw, Float pitch) {
        List<Entity> admitted = admit(sender, executor, targets);
        if (admitted == null) {
            return 0;
        }
        if (world == null || !inBounds(x, y, z)) {
            send(sender, Lang.of("maison.teleport.position.bounds"));
            return 0;
        }
        for (Entity target : admitted) {
            Location own = target.getLocation();
            float facing = yaw == null ? own.getYaw() : Location.normalizeYaw(yaw);
            float tilt = pitch == null ? own.getPitch() : Location.normalizePitch(pitch);
            move(sender, target, new Location(world, x, y, z, facing, tilt));
        }
        String fx = format(x), fy = format(y), fz = format(z);
        if (admitted.size() == 1) {
            send(sender, Lang.of("maison.teleport.position.one", "name", admitted.get(0).getName(),
                    "x", fx, "y", fy, "z", fz));
        } else {
            send(sender, Lang.of("maison.teleport.position.many", "count", admitted.size(),
                    "x", fx, "y", fy, "z", fz));
        }
        return admitted.size();
    }

    private int deliver(CommandSender sender, Entity executor, List<? extends Entity> targets, Entity destination) {
        transport(sender, targets, destination, executor != null && destination.equals(executor));
        String where = destination.getName();
        if (targets.size() == 1) {
            send(sender, Lang.of("maison.teleport.entity.one", "name", targets.get(0).getName(), "destination", where));
        } else {
            send(sender, Lang.of("maison.teleport.entity.many", "count", targets.size(), "destination", where));
        }
        return targets.size();
    }

    void transport(CommandSender failuresTo, List<? extends Entity> targets, Entity destination, boolean safe) {
        onOwner(destination, () -> {
            Location to = destination.getLocation();
            Location landing = null;
            for (Entity target : targets) {
                if (safe && SafeLanding.grounded(target)) {
                    if (landing == null) {
                        landing = SafeLanding.below(to);
                    }
                    move(failuresTo, target, landing.clone());
                } else {
                    move(failuresTo, target, to.clone());
                }
            }
        });
    }

    private static void onOwner(Entity entity, Runnable task) {
        if (Sched.regionised()) {
            Sched.executeFor(entity, task);
        } else {
            task.run();
        }
    }

    Player onePlayer(CommandSender sender, List<? extends Entity> named) {
        List<Player> players = new ArrayList<>();
        for (Entity entity : seen(sender, named)) {
            if (entity instanceof Player player) {
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            send(sender, Lang.of("maison.player.none"));
            return null;
        }
        if (players.size() > 1) {
            send(sender, Lang.of("maison.player.single"));
            return null;
        }
        return players.get(0);
    }

    private List<Entity> admit(CommandSender sender, Entity executor, List<? extends Entity> targets) {
        List<Entity> seen = seen(sender, targets);
        if (seen.isEmpty()) {
            send(sender, Lang.of("maison.teleport.none"));
            return null;
        }
        boolean othersInvolved = false;
        for (Entity target : seen) {
            if (!target.equals(executor)) {
                othersInvolved = true;
                break;
            }
        }
        if (othersInvolved && !sender.hasPermission(OTHERS)) {
            send(sender, Lang.of("maison.teleport.deny.others"));
            return null;
        }
        if (!permitted(sender)) {
            send(sender, Lang.of("maison.teleport.deny.self"));
            return null;
        }
        return seen;
    }

    private static List<Entity> seen(CommandSender viewer, List<? extends Entity> entities) {
        List<Entity> out = new ArrayList<>(entities.size());
        for (Entity entity : entities) {
            if (entity != null && visible(viewer, entity)) {
                out.add(entity);
            }
        }
        return out;
    }

    private void move(CommandSender sender, Entity target, Location to) {
        String name = target.getName();
        mover.move(target, to).whenComplete((moved, error) -> {
            if (error != null || !Boolean.TRUE.equals(moved)) {
                send(sender, Lang.of("maison.teleport.failed", "name", name));
            }
        });
    }

    private static boolean inBounds(double x, double y, double z) {
        return x >= -HORIZONTAL_LIMIT && x < HORIZONTAL_LIMIT
                && z >= -HORIZONTAL_LIMIT && z < HORIZONTAL_LIMIT
                && y >= -VERTICAL_LIMIT && y < VERTICAL_LIMIT;
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
