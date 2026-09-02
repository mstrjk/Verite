package teacommontea.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;


public final class FakeConnectCommand implements CommandExecutor, TabCompleter {

    private static final String DEFAULT_JOIN = "&#FFFF55%player% joined the game";
    private static final String DEFAULT_LEAVE = "&#FFFF55%player% left the game";

    private final Plugin plugin;

    public FakeConnectCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean leave(Command command) {
        return command.getName().toLowerCase(java.util.Locale.ROOT).startsWith("fakeleave");
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        boolean leave = leave(command);

        if (!enabled()) {
            send(sender, Colours.WARNING + "Fake join and leave messages are currently disabled.");
            return true;
        }

        String base = leave ? "verite.fakeleave" : "verite.fakejoin";
        String name;
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                send(sender, Colours.WARNING + "Console must name a player: " + Colours.BRAND_ACCENT_SECONDARY
                        + "/" + command.getName() + " <player>");
                return true;
            }
            if (!p.hasPermission(base)) {
                send(sender, Colours.WARNING + "You may not do that.");
                return true;
            }
            name = p.getName();
        } else {
            if (!sender.hasPermission(base + ".others")) {
                send(sender, Colours.WARNING + "You may not fake a message for another player.");
                return true;
            }
            name = args[0];
        }

        String template = leave ? leaveMessage() : joinMessage();
        BaseComponent[] line = TextComponent.fromLegacyText(
                Colours.legacy(template.replace("%player%", name)));
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.spigot().sendMessage(line);
        }
        Bukkit.getConsoleSender().sendMessage(TextComponent.toLegacyText(line));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        String base = leave(command) ? "verite.fakeleave" : "verite.fakejoin";
        if (args.length == 1 && sender.hasPermission(base + ".others")) {
            return Complete.onlineNames(args[0]);
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("deprecation")
    private void send(CommandSender to, String mini) {
        to.spigot().sendMessage(new Messages().prefixed(mini));
    }

    private YamlConfiguration config() {
        File f = new File(plugin.getDataFolder(), "config.yml");
        return f.isFile() ? Yaml.loadYaml(f) : new YamlConfiguration();
    }

    private boolean enabled() {
        return config().getBoolean("fake.connect.enabled", true);
    }

    private String joinMessage() {
        return config().getString("fake.connect.join.message", DEFAULT_JOIN);
    }

    private String leaveMessage() {
        return config().getString("fake.connect.leave.message", DEFAULT_LEAVE);
    }
}
