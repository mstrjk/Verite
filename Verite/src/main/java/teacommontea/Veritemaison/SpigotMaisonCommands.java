package teacommontea.veritemaison;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.java.JavaPlugin;

import teacommontea.util.ConsoleColours;
import teacommontea.util.Lang;
import teacommontea.util.Trace;

final class SpigotMaisonCommands {

    private SpigotMaisonCommands() {}

    static void register(JavaPlugin plugin, Set<Maison.Feature> features, long requestExpiryMillis) {
        SimpleCommandMap map;
        try {
            map = (SimpleCommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException | ClassCastException e) {
            plugin.getLogger().warning(ConsoleColours.bad(Lang.of("maison.register.failed")) + Trace.of(e));
            return;
        }
        Teleport teleport = new Teleport(
                (entity, to) -> CompletableFuture.completedFuture(entity.teleport(to, TeleportCause.COMMAND)));
        String prefix = plugin.getName().toLowerCase(Locale.ROOT);
        SpigotCommandTree tree = new SpigotCommandTree(plugin, map, prefix);
        if (features.contains(Maison.Feature.TELEPORT)) {
            SpigotTeleportCommand command = new SpigotTeleportCommand(plugin, teleport);
            map.register(prefix, command);
            tree.add(command, SpigotCommandTree.Shape.TELEPORT);
        }
        for (Maison.PlayerCommand spec : Maison.playerCommands(plugin, features, teleport, requestExpiryMillis)) {
            SpigotPlayerCommand command = new SpigotPlayerCommand(plugin, teleport, spec);
            map.register(prefix, command);
            tree.add(command, spec.optional() ? SpigotCommandTree.Shape.OPTIONAL_PLAYER : SpigotCommandTree.Shape.PLAYER);
        }
        Bukkit.getPluginManager().registerEvents(tree, plugin);
    }
}
