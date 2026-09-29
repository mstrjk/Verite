package teacommontea.veritemaison;

import java.util.EnumSet;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

import teacommontea.util.ConsoleColours;
import teacommontea.util.Lang;
import teacommontea.util.Trace;

public final class Maison {

    public enum Feature { TELEPORT, TO_PLAYER, HERE }

    private Maison() {}

    public static void enable(JavaPlugin plugin, Set<Feature> features) {
        if (features.isEmpty()) {
            return;
        }
        Set<Feature> wanted = EnumSet.copyOf(features);
        if (classPresent("io.papermc.paper.command.brigadier.Commands")
                && classPresent("io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents")) {
            try {
                Class.forName("teacommontea.veritemaison.PaperMaisonCommands")
                        .getMethod("register", JavaPlugin.class, Set.class)
                        .invoke(null, plugin, wanted);
                return;
            } catch (ReflectiveOperationException | LinkageError e) {
                plugin.getLogger().warning(ConsoleColours.bad(Lang.of("maison.paper.failed")) + Trace.of(e));
            }
        }
        SpigotMaisonCommands.register(plugin, wanted);
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
