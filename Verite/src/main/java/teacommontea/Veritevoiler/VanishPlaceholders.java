package teacommontea.veritevoiler;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;


public final class VanishPlaceholders {

    private VanishPlaceholders() {}

    public static void registerIfAvailable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return;
        }
        try {
            new Expansion().register();
        } catch (Throwable ignored) {

        }
    }

    private static final class Expansion extends me.clip.placeholderapi.expansion.PlaceholderExpansion {
        @Override public String getIdentifier() { return "verite"; }
        @Override public String getAuthor() { return "teacommontea"; }
        @Override public String getVersion() { return "1.0.0"; }
        @Override public boolean persist() { return true; }

        @Override
        public String onRequest(OfflinePlayer player, String params) {
            switch (params.toLowerCase()) {
                case "vanished":
                    return player != null && Vanish.instance() != null
                            && Vanish.instance().isVanished(player.getUniqueId()) ? "true" : "false";
                case "vanish_count":
                    return Vanish.instance() == null ? "0"
                            : String.valueOf(Vanish.instance().getVanished().size());
                default:
                    return null;
            }
        }
    }
}
