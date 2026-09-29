package teacommontea.veritemaison;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import teacommontea.util.Complete;
import teacommontea.util.Lang;

final class SpigotPlayerCommand extends Command implements PluginIdentifiableCommand {

    private final Plugin plugin;
    private final Teleport teleport;
    private final Maison.PlayerCommand spec;

    SpigotPlayerCommand(Plugin plugin, Teleport teleport, Maison.PlayerCommand spec) {
        super(spec.label(), spec.description(),
                "/" + spec.label() + (spec.optional() ? " [<player>]" : " <player>"), List.of());
        this.plugin = plugin;
        this.teleport = teleport;
        this.spec = spec;
        setPermission(String.join(";", spec.permissions()));
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
        if (!spec.permitted(sender)) {
            teleport.send(sender, spec.denial());
            return true;
        }
        if (args.length > 1 || (args.length == 0 && !spec.optional())) {
            teleport.send(sender, Lang.of(spec.optional() ? "maison.player.usage.optional" : "maison.player.usage",
                    "command", label));
            return true;
        }
        CommandSender actor = sender instanceof ProxiedCommandSender proxied ? proxied.getCallee() : sender;
        Entity executor = actor instanceof Entity entity ? entity : null;
        List<Entity> named = null;
        if (args.length == 1) {
            named = SpigotTeleportCommand.select(teleport, sender, args[0]);
            if (named == null) {
                return true;
            }
        }
        spec.action().run(sender, executor, named, label);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1 || !spec.permitted(sender)) {
            return new ArrayList<>();
        }
        return Complete.onlineNames(args[0], p -> Teleport.visible(sender, p));
    }
}
