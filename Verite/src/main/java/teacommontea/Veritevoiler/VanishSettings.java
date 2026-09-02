package teacommontea.veritevoiler;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

final class VanishSettings {

    boolean componentEnabled = true;
    boolean fakeMessage = true;
    boolean actionbar = true;
    boolean selfView = true;
    boolean fly = true;
    boolean invulnerability = true;
    boolean effects = true;
    boolean gamemode = true;
    boolean rideEntity = true;
    boolean silentContainer = true;
    boolean inventoryInspect = true;
    boolean serverPing = true;
    boolean ghost = true;
    boolean preventChat = true;
    boolean preventPickup = true;
    boolean preventDrop = true;
    boolean preventInteract = true;
    boolean preventBlockBreak = true;
    boolean preventBlockPlace = true;
    boolean preventTarget = true;
    boolean preventDamage = true;
    boolean preventFood = true;
    boolean preventBuckets = true;
    boolean preventAdvancement = true;
    boolean preventProjectiles = true;
    boolean muffleSounds = true;
    boolean muffleParticles = true;

    private VanishSettings() {}

    static VanishSettings load(Plugin plugin) {
        VanishSettings s = new VanishSettings();
        File f = new File(plugin.getDataFolder(), "config.yml");
        if (!f.isFile()) {
            return s;
        }
        YamlConfiguration y = teacommontea.util.Yaml.loadYaml(f);
        s.componentEnabled   = y.getBoolean("vanish.enabled", true);
        s.fakeMessage        = y.getBoolean("vanish.features.fake.message", true);
        s.actionbar          = y.getBoolean("vanish.features.actionbar", true);
        s.selfView           = y.getBoolean("vanish.features.self.view", true);
        s.fly                = y.getBoolean("vanish.features.fly", true);
        s.invulnerability    = y.getBoolean("vanish.features.invulnerability", true);
        s.effects            = y.getBoolean("vanish.features.effects", true);
        s.gamemode           = y.getBoolean("vanish.features.gamemode", true);
        s.rideEntity         = y.getBoolean("vanish.features.ride.entity", true);
        s.silentContainer    = y.getBoolean("vanish.features.silent.container", true);
        s.inventoryInspect   = y.getBoolean("vanish.features.inventory.inspect", true);
        s.serverPing         = y.getBoolean("vanish.features.server.ping", true);
        s.ghost              = y.getBoolean("vanish.features.ghost", true);
        s.preventChat        = y.getBoolean("vanish.features.prevent.chat", true);
        s.preventPickup      = y.getBoolean("vanish.features.prevent.pickup", true);
        s.preventDrop        = y.getBoolean("vanish.features.prevent.drop", true);
        s.preventInteract    = y.getBoolean("vanish.features.prevent.interact", true);
        s.preventBlockBreak  = y.getBoolean("vanish.features.prevent.block.break", true);
        s.preventBlockPlace  = y.getBoolean("vanish.features.prevent.block.place", true);
        s.preventTarget      = y.getBoolean("vanish.features.prevent.target", true);
        s.preventDamage      = y.getBoolean("vanish.features.prevent.damage", true);
        s.preventFood        = y.getBoolean("vanish.features.prevent.food", true);
        s.preventBuckets     = y.getBoolean("vanish.features.prevent.buckets", true);
        s.preventAdvancement = y.getBoolean("vanish.features.prevent.advancement", true);
        s.preventProjectiles = y.getBoolean("vanish.features.prevent.projectiles", true);
        s.muffleSounds       = y.getBoolean("vanish.features.muffle.sounds", true);
        s.muffleParticles    = y.getBoolean("vanish.features.muffle.particles", true);
        return s;
    }
}
