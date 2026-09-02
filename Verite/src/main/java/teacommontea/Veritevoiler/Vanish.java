package teacommontea.veritevoiler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import teacommontea.util.KvStore;
import teacommontea.util.Scope;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Vanish {

    private static Vanish instance;

    private final Plugin plugin;
    private final KvStore store;
    private final Scope scope;
    private final Set<UUID> vanished = ConcurrentHashMap.newKeySet();
    private volatile VanishSettings settings;
    private final VanishEffectStore effectStore;
    private VanishPacketInterceptor muffler;

    private static final String SCOPE = "vanish";

    private Vanish(Plugin plugin, KvStore store, VanishSettings settings) {
        this.plugin = plugin;
        this.store = store;
        this.scope = store.scope(SCOPE);
        this.settings = settings;
        this.effectStore = new VanishEffectStore(store);
    }

    VanishEffectStore effectStore() {
        return effectStore;
    }

    VanishSettings settings() {
        return settings;
    }

    public boolean enabled() {
        return settings != null && settings.componentEnabled;
    }

    public void reload() {
        boolean wasEnabled = enabled();
        this.settings = VanishSettings.load(plugin);
        VanishPacketInterceptor old = this.muffler;
        this.muffler = VanishPacketInterceptor.install(plugin, this, this.settings);
        if (old != null) {
            old.shutdown();
        }
        if (wasEnabled && !enabled()) {
            revealAll();
            VanishTab.shutdown();
        } else if (!wasEnabled && enabled()) {
            VanishTab.installIfAvailable(plugin, this);
        }
    }

    private void revealAll() {
        for (UUID id : new HashSet<>(vanished)) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                unvanish(p);
            }
        }
    }

    void setMuffler(VanishPacketInterceptor muffler) {
        this.muffler = muffler;
    }

    public static Vanish instance() {
        return instance;
    }

    public Plugin plugin() {
        return plugin;
    }

    public static Vanish enable(Plugin pl) throws Exception {
        KvStore store = KvStore.open("vanish");
        Vanish v = new Vanish(pl, store, VanishSettings.load(pl));
        instance = v;
        v.restore();
        pl.getServer().getPluginManager().registerEvents(new VanishListener(v), pl);
        VanishFeatures.registerAll(pl, v);
        return v;
    }

    public void disable() {

        VanishTab.shutdown();
        if (muffler != null) {
            muffler.shutdown();
            muffler = null;
        }
        for (UUID id : new HashSet<>(vanished)) {
            Player p = Bukkit.getPlayer(id);
            if (p != null) {
                showToAll(p);
            }
        }
        store.shutdown();
        instance = null;
    }

    private void restore() {
        for (Scope.Entry e : scope.entries()) {
            try {
                UUID id = UUID.fromString(e.key());
                vanished.add(id);
                Player p = Bukkit.getPlayer(id);
                if (p != null) {
                    hideFromAll(p);
                }
            } catch (IllegalArgumentException ignored) {

            }
        }
    }

    public boolean isVanished(UUID player) {
        return player != null && vanished.contains(player);
    }

    public Set<UUID> getVanished() {
        return Collections.unmodifiableSet(new HashSet<>(vanished));
    }

    public boolean vanish(Player p) {
        if (p == null || !enabled()) return false;
        UUID id = p.getUniqueId();
        if (vanished.contains(id)) return true;

        VeriteVanishEvent ev = new VeriteVanishEvent(p, true);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return false;

        vanished.add(id);
        scope.set(id.toString(), true);
        hideFromAll(p);
        VanishEvents.fireVanished(id);
        return true;
    }

    public boolean unvanish(Player p) {
        if (p == null) return false;
        UUID id = p.getUniqueId();
        if (!vanished.contains(id)) return true;

        VeriteVanishEvent ev = new VeriteVanishEvent(p, false);
        Bukkit.getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return false;

        vanished.remove(id);
        scope.delete(id.toString());
        showToAll(p);
        VanishEvents.fireUnvanished(id);
        return true;
    }

    public boolean toggle(Player p) {
        return isVanished(p.getUniqueId()) ? !unvanish(p) : vanish(p);
    }

    private void hideFromAll(Player p) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(p)) continue;
            if (!canSee(viewer, p)) {
                viewer.hidePlayer(plugin, p);
            } else if (settings.ghost) {

                VanishGhost.ghostFor(viewer, p);
            }
        }
    }

    private void showToAll(Player p) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(p)) continue;
            viewer.showPlayer(plugin, p);
        }
    }

    static boolean canSee(Player viewer, Player hidden) {
        if (viewer == null || !viewer.hasPermission("verite.vanish.see")) return false;
        return tierOf(viewer) >= tierOf(hidden);
    }

    static boolean canSee(Player viewer) {
        return viewer != null && viewer.hasPermission("verite.vanish.see");
    }

    private static int tierOf(Player p) {
        return VanishLevel.tierOf(p);
    }

    static int tier(Player p) {
        return VanishLevel.tierOf(p);
    }

    void applyOnJoin(Player joiner) {
        for (UUID id : vanished) {
            Player hidden = Bukkit.getPlayer(id);
            if (hidden != null && !hidden.equals(joiner) && !canSee(joiner, hidden)) {
                joiner.hidePlayer(plugin, hidden);
            }
        }
        if (vanished.contains(joiner.getUniqueId())) {
            hideFromAll(joiner);
        }
    }
}
