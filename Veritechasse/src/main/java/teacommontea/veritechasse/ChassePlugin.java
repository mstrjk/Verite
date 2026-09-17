package teacommontea.veritechasse;

import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

import teacommontea.veritechasse.Reality.InteractionMonitor;
import teacommontea.veritechasse.Reality.MovementProbe;
import teacommontea.veritechasse.Reality.RealityMonitor;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Catalogue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;

public final class ChassePlugin extends JavaPlugin {

    public static final String VERBOSE_KEY = "verbose";

    private Protocol protocol;
    private RealityMonitor monitor;
    private InteractionMonitor interactions;

    @Override
    public void onEnable() {
        this.protocol = Protocol.current();
        getLogger().info("Veritechasse active on " + this.protocol
            + " (enchantments: " + Era.enchantments(this.protocol) + ")");

        verifyRegistry();

        saveDefaultConfig();
        boolean verbose = getConfig().getBoolean(VERBOSE_KEY, false);

        this.monitor = new RealityMonitor(this, this.protocol);
        this.monitor.setVerbose(verbose);
        getServer().getPluginManager().registerEvents(this.monitor, this);
        this.monitor.start();

        this.interactions = new InteractionMonitor(this, this.protocol);
        this.interactions.setVerbose(verbose);
        getServer().getPluginManager().registerEvents(this.interactions, this);
        this.monitor.attachInteractions(this.interactions);
    }

    @Override
    public void onDisable() {
        if (this.monitor != null) {
            this.monitor.stop();
        }
        if (this.interactions != null) {
            this.interactions.reportTotals();
        }
    }

    private void verifyRegistry() {
        Set<String> known = Catalogue.keys();
        if (known.isEmpty()) {
            getLogger().warning("No enchantment facts were discovered. Veritechasse cannot establish reality.");
            return;
        }

        for (String key : known) {
            Registry.isResolvable(key);
        }

        Set<String> missing = Registry.unresolved();
        if (missing.isEmpty()) {
            getLogger().info("Resolved " + known.size() + " enchantment facts.");
            return;
        }

        getLogger().warning("Enchantments this server did not resolve: " + missing);
        getLogger().warning("Reality for those enchantments will read as absent. Report this with the server version.");
    }

    public Protocol protocol() {
        return this.protocol;
    }

    public RealityMonitor monitor() {
        return this.monitor;
    }

    public MovementProbe newProbe() {
        return new MovementProbe(this);
    }
}
