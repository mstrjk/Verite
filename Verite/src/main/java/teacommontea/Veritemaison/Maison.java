package teacommontea.veritemaison;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import teacommontea.util.ConsoleColours;
import teacommontea.util.Lang;
import teacommontea.util.Trace;

public final class Maison {

    public enum Feature { TELEPORT, TO_PLAYER, HERE, REQUEST, REQUEST_HERE }

    interface Action {
        int run(CommandSender sender, Entity executor, List<? extends Entity> named, String label);
    }

    record PlayerCommand(String label, List<String> permissions, boolean optional, Action action) {

        boolean permitted(CommandSender sender) {
            for (String permission : permissions) {
                if (sender.hasPermission(permission)) {
                    return true;
                }
            }
            return false;
        }

        String description() {
            return Lang.of("maison." + label + ".description");
        }

        String denial() {
            return Lang.of("maison." + label + ".deny");
        }
    }

    private Maison() {}

    public static void enable(JavaPlugin plugin, Set<Feature> features, long requestExpiryMillis) {
        if (features.isEmpty()) {
            return;
        }
        Set<Feature> wanted = EnumSet.copyOf(features);
        if (classPresent("io.papermc.paper.command.brigadier.Commands")
                && classPresent("io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents")) {
            try {
                Class.forName("teacommontea.veritemaison.PaperMaisonCommands")
                        .getMethod("register", JavaPlugin.class, Set.class, long.class)
                        .invoke(null, plugin, wanted, requestExpiryMillis);
                return;
            } catch (ReflectiveOperationException | LinkageError e) {
                plugin.getLogger().warning(ConsoleColours.bad(Lang.of("maison.paper.failed")) + Trace.of(e));
            }
        }
        SpigotMaisonCommands.register(plugin, wanted, requestExpiryMillis);
    }

    static List<PlayerCommand> playerCommands(JavaPlugin plugin, Set<Feature> features,
                                              Teleport teleport, long requestExpiryMillis) {
        List<PlayerCommand> out = new ArrayList<>();
        if (features.contains(Feature.TO_PLAYER)) {
            out.add(new PlayerCommand(Teleport.TO_PLAYER_LABEL, List.of(Teleport.TO_PLAYER), false,
                    (sender, executor, named, label) -> teleport.toPlayer(sender, executor, named, label)));
        }
        if (features.contains(Feature.HERE)) {
            out.add(new PlayerCommand(Teleport.HERE_LABEL, List.of(Teleport.HERE), false,
                    (sender, executor, named, label) -> teleport.here(sender, executor, named, label)));
        }
        boolean ask = features.contains(Feature.REQUEST);
        boolean askHere = features.contains(Feature.REQUEST_HERE);
        if (!ask && !askHere) {
            return out;
        }
        TeleportRequests requests = new TeleportRequests(teleport, requestExpiryMillis);
        Bukkit.getPluginManager().registerEvents(requests, plugin);
        List<String> senders = new ArrayList<>();
        if (ask) {
            senders.add(TeleportRequests.ASK);
            out.add(new PlayerCommand("tpa", List.of(TeleportRequests.ASK), false,
                    (sender, executor, named, label) -> requests.ask(sender, executor, named, label, false)));
        }
        if (askHere) {
            senders.add(TeleportRequests.ASK_HERE);
            out.add(new PlayerCommand("tpahere", List.of(TeleportRequests.ASK_HERE), false,
                    (sender, executor, named, label) -> requests.ask(sender, executor, named, label, true)));
        }
        out.add(new PlayerCommand("tpaccept", List.of(TeleportRequests.ANSWER), true,
                (sender, executor, named, label) -> requests.answer(sender, executor, named, label, true)));
        out.add(new PlayerCommand("tpdeny", List.of(TeleportRequests.ANSWER), true,
                (sender, executor, named, label) -> requests.answer(sender, executor, named, label, false)));
        out.add(new PlayerCommand("tpacancel", List.copyOf(senders), true,
                requests::cancel));
        return out;
    }

    private static boolean classPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
