package teacommontea.api;

import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import teacommontea.api.internal.ApiBridge;

import teacommontea.veritevoiler.Vanish;
import teacommontea.veritevoiler.VanishPlaceholders;

public final class VeriteVanish {

    private VeriteVanish() {}

    private static Vanish vanish() {
        return Vanish.instance();
    }

    public static boolean enabled() {
        return vanish() != null;
    }

    public static boolean isVanished(UUID player) {
        return enabled() && vanish().isVanished(player);
    }

    public static boolean isVanished(Player player) {
        return player != null && isVanished(player.getUniqueId());
    }

    public static Set<UUID> getVanished() {
        return enabled() ? vanish().getVanished() : Set.of();
    }

    public static boolean vanish(Player p) {
        return enabled() && vanish().vanish(p);
    }

    public static boolean unvanish(Player p) {
        return enabled() && vanish().unvanish(p);
    }

    public static boolean toggle(Player p) {
        return enabled() && vanish().toggle(p);
    }

    public static void registerPlaceholders() {
        VanishPlaceholders.registerIfAvailable();
    }

    public static void registerListener(VanishListener listener) {
        ApiBridge.registerVanish(listener);
    }

    public static void unregisterListener(VanishListener listener) {
        ApiBridge.unregisterVanish(listener);
    }
}
