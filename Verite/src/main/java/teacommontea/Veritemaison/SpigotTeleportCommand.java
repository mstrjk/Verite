package teacommontea.veritemaison;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import teacommontea.util.Complete;
import teacommontea.util.ConsoleColours;
import teacommontea.util.Lang;
import teacommontea.util.Trace;

final class SpigotTeleportCommand extends Command implements PluginIdentifiableCommand {

    private static final Pattern NUMBER = Pattern.compile("-?(\\d+(\\.\\d*)?|\\.\\d+)");

    private final Plugin plugin;
    private final Teleport teleport = new Teleport(
            (entity, to) -> CompletableFuture.completedFuture(entity.teleport(to, TeleportCause.COMMAND)));

    private SpigotTeleportCommand(Plugin plugin) {
        super(Teleport.LABEL, Lang.of("maison.teleport.description"),
                "/" + Teleport.LABEL + " [<targets>] <location|destination> [<rotation>]", Teleport.ALIASES);
        this.plugin = plugin;
        setPermission(Teleport.SELF + ";" + Teleport.OTHERS);
    }

    static void register(JavaPlugin plugin) {
        try {
            SimpleCommandMap map = (SimpleCommandMap) Bukkit.getServer().getClass()
                    .getMethod("getCommandMap").invoke(Bukkit.getServer());
            SpigotTeleportCommand command = new SpigotTeleportCommand(plugin);
            String prefix = plugin.getName().toLowerCase(Locale.ROOT);
            map.register(prefix, command);
            Bukkit.getPluginManager().registerEvents(new SpigotCommandTree(plugin, map, command, prefix), plugin);
        } catch (ReflectiveOperationException | ClassCastException e) {
            plugin.getLogger().warning(ConsoleColours.bad(Lang.of("maison.teleport.register.failed")) + Trace.of(e));
        }
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (!plugin.isEnabled()) {
            return false;
        }
        if (!Teleport.permitted(sender)) {
            teleport.send(sender, Lang.of("maison.teleport.deny.self"));
            return true;
        }
        CommandSender actor = sender instanceof ProxiedCommandSender proxied ? proxied.getCallee() : sender;
        Entity executor = actor instanceof Entity entity ? entity : null;
        boolean named = args.length % 2 == 0;
        int from = named ? 1 : 0;
        int rest = args.length - from;
        if (args.length == 0 || args.length > 6 || (rest != 1 && rest != 3 && rest != 5)) {
            teleport.send(sender, Lang.of("maison.teleport.usage", "command", label));
            return true;
        }
        List<Entity> targets;
        if (named) {
            targets = select(sender, args[0]);
            if (targets == null) {
                return true;
            }
        } else if (executor == null) {
            teleport.consoleMustName(sender, label);
            return true;
        } else {
            targets = List.of(executor);
        }
        if (rest == 1) {
            List<Entity> destination = select(sender, args[from]);
            if (destination != null) {
                teleport.toEntity(sender, executor, targets, destination);
            }
            return true;
        }
        Location origin = origin(actor);
        double[] at = position(sender, origin, args[from], args[from + 1], args[from + 2]);
        if (at == null) {
            return true;
        }
        Float yaw = null, pitch = null;
        if (rest == 5) {
            Float[] rotation = rotation(sender, origin, args[from + 3], args[from + 4]);
            if (rotation == null) {
                return true;
            }
            yaw = rotation[0];
            pitch = rotation[1];
        }
        teleport.toPosition(sender, executor, targets, origin.getWorld(), at[0], at[1], at[2], yaw, pitch);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (!Teleport.permitted(sender) || args.length == 0 || args.length > 2) {
            return new ArrayList<>();
        }
        return Complete.onlineNames(args[args.length - 1], p -> Teleport.visible(sender, p));
    }

    private List<Entity> select(CommandSender sender, String token) {
        if (token.startsWith("@")) {
            try {
                return Bukkit.selectEntities(sender, token);
            } catch (IllegalArgumentException e) {
                teleport.send(sender, Lang.of("maison.teleport.selector.invalid", "input", token));
                return null;
            }
        }
        Entity found = byUuid(token);
        if (found == null) {
            found = Bukkit.getPlayerExact(token);
        }
        return found == null ? List.of() : List.of(found);
    }

    private static Entity byUuid(String token) {
        if (token.length() != 36 || token.indexOf('-') < 0) {
            return null;
        }
        try {
            return Bukkit.getEntity(UUID.fromString(token));
        } catch (IllegalArgumentException notUuid) {
            return null;
        }
    }

    private static Location origin(CommandSender actor) {
        if (actor instanceof Entity entity) {
            return entity.getLocation();
        }
        if (actor instanceof BlockCommandSender blockSender) {
            Block block = blockSender.getBlock();
            return block.getLocation().add(0.5, 0.5, 0.5);
        }
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }

    private double[] position(CommandSender sender, Location origin, String x, String y, String z) {
        boolean local = x.startsWith("^");
        if (local != y.startsWith("^") || local != z.startsWith("^")) {
            return invalid(sender, x, y, z);
        }
        if (local) {
            Double left = offset(x, '^'), up = offset(y, '^'), forwards = offset(z, '^');
            if (left == null || up == null || forwards == null) {
                return invalid(sender, x, y, z);
            }
            return localToWorld(origin, left, up, forwards);
        }
        Double ax = axis(x, origin.getX(), true), ay = axis(y, origin.getY(), false), az = axis(z, origin.getZ(), true);
        if (ax == null || ay == null || az == null) {
            return invalid(sender, x, y, z);
        }
        return new double[] {ax, ay, az};
    }

    private Float[] rotation(CommandSender sender, Location origin, String yaw, String pitch) {
        Double y = axis(yaw, origin.getYaw(), false), p = axis(pitch, origin.getPitch(), false);
        if (y == null || p == null) {
            teleport.send(sender, Lang.of("maison.teleport.position.invalid", "input", yaw + " " + pitch));
            return null;
        }
        return new Float[] {(float) y.doubleValue(), (float) p.doubleValue()};
    }

    private double[] invalid(CommandSender sender, String x, String y, String z) {
        teleport.send(sender, Lang.of("maison.teleport.position.invalid", "input", x + " " + y + " " + z));
        return null;
    }

    private static Double axis(String token, double base, boolean centre) {
        if (token.startsWith("~")) {
            Double shift = offset(token, '~');
            return shift == null ? null : base + shift;
        }
        if (!NUMBER.matcher(token).matches()) {
            return null;
        }
        double value = Double.parseDouble(token);
        return centre && token.indexOf('.') < 0 ? value + 0.5 : value;
    }

    private static Double offset(String token, char marker) {
        if (token.isEmpty() || token.charAt(0) != marker) {
            return null;
        }
        String rest = token.substring(1);
        if (rest.isEmpty()) {
            return 0.0;
        }
        return NUMBER.matcher(rest).matches() ? Double.parseDouble(rest) : null;
    }

    private static double[] localToWorld(Location origin, double left, double up, double forwards) {
        double yaw = Math.toRadians(origin.getYaw() + 90.0F);
        double pitch = Math.toRadians(-origin.getPitch());
        double pitchUp = Math.toRadians(-origin.getPitch() + 90.0F);
        double cosYaw = Math.cos(yaw), sinYaw = Math.sin(yaw);
        double fx = cosYaw * Math.cos(pitch), fy = Math.sin(pitch), fz = sinYaw * Math.cos(pitch);
        double ux = cosYaw * Math.cos(pitchUp), uy = Math.sin(pitchUp), uz = sinYaw * Math.cos(pitchUp);
        double lx = -(fy * uz - fz * uy), ly = -(fz * ux - fx * uz), lz = -(fx * uy - fy * ux);
        return new double[] {
                origin.getX() + fx * forwards + ux * up + lx * left,
                origin.getY() + fy * forwards + uy * up + ly * left,
                origin.getZ() + fz * forwards + uz * up + lz * left
        };
    }
}
