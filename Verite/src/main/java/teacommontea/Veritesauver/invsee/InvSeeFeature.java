package teacommontea.veritesauver.invsee;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;


public final class InvSeeFeature {

    private static InvSeeFeature instance;

    private final boolean enabled;
    private final InvSee core;
    private final InvSeeCommands commands;
    private final InvSeeTab tab;
    private final InvSeeListener listener;

    private InvSeeFeature(Plugin plugin) {
        InvSeeClassgen builtGen = null;
        InvSeeMenuRoles builtRoles = null;
        try {
            builtGen = InvSeeClassgen.build();
            builtRoles = InvSeeMenuRoles.resolve(builtGen.access());
        } catch (Throwable t) {
            plugin.getLogger().warning("InvSee spectating disabled: unsupported server ("
                    + t.getMessage() + ")");
        }

        if (builtGen == null || builtRoles == null) {
            this.enabled = false;
            this.core = null;
            this.commands = null;
            this.tab = null;
            this.listener = null;
            return;
        }

        this.enabled = true;
        InvSeeAccess access = builtGen.access();
        Config config = Config.from(teacommontea.veritesauver.util.SauverConfig.yaml());
        Resolver resolver = new Resolver(plugin, access);
        OfflineInventories offlineIo = new OfflineInventories(plugin, access);
        this.listener = new InvSeeListener(plugin, offlineIo, access);
        this.core = new InvSee(plugin, config, resolver, listener, offlineIo, builtGen, builtRoles);
        this.commands = new InvSeeCommands(core);
        this.tab = new InvSeeTab(config.offlineSupport && config.tabCompleteOffline);
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    public static InvSeeFeature enable(Plugin plugin) {
        instance = new InvSeeFeature(plugin);
        return instance;
    }

    public static InvSeeFeature get() {
        return instance;
    }

    public boolean enabled() {
        return enabled;
    }

    public void invsee(CommandSender sender, String[] args) {
        if (enabled) {
            commands.invsee(sender, args);
        }
    }

    public void endersee(CommandSender sender, String[] args) {
        if (enabled) {
            commands.endersee(sender, args);
        }
    }

    public List<String> tab(CommandSender sender, String[] args) {
        return enabled ? tab.complete(sender, args) : List.of();
    }
}
