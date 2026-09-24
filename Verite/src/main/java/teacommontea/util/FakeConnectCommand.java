package teacommontea.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


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


    private final Plugin plugin;

    public FakeConnectCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    private boolean leave(Command command) {
        return command.getName().toLowerCase(java.util.Locale.ROOT).startsWith("fakeleave");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        boolean leave = leave(command);

        if (!enabled()) {
            send(sender, Lang.of("fakeconnect.disabled"));
            return true;
        }

        String base = leave ? "verite.fakeleave" : "verite.fakejoin";
        String name;
        if (args.length == 0) {
            if (!(sender instanceof Player p)) {
                send(sender, Lang.of("console.name.player", "command", command.getName()));
                return true;
            }
            if (!p.hasPermission(base)) {
                send(sender, Lang.of("fakeconnect.deny.self"));
                return true;
            }
            name = p.getName();
        } else {
            if (!sender.hasPermission(base + ".others")) {
                send(sender, Lang.of("fakeconnect.deny.others"));
                return true;
            }
            name = args[0];
        }

        String template = leave ? leaveMessage() : joinMessage();
        teacommontea.util.text.Send.broadcast(teacommontea.util.text.Legacy.parse(
                Colours.legacy(template.replace("%player%", name))));
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

    private void send(CommandSender to, String tagged) {
        teacommontea.util.text.Send.to(to, new Messages().prefixed(tagged));
    }

    private YamlConfiguration config() {
        File f = new File(plugin.getDataFolder(), "config.yml");
        return f.isFile() ? Yaml.loadYaml(f) : new YamlConfiguration();
    }

    private boolean enabled() {
        return config().getBoolean("fake.connect.enabled", true);
    }

    private String joinMessage() {
        return config().getString("fake.connect.join.message", Lang.of("fakeconnect.join"));
    }

    private String leaveMessage() {
        return config().getString("fake.connect.leave.message", Lang.of("fakeconnect.leave"));
    }
}
