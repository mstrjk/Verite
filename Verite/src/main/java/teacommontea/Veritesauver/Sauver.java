package teacommontea.veritesauver;

import org.bukkit.plugin.Plugin;

import teacommontea.util.KvStore;
import teacommontea.veritesauver.util.SauverMessages;

import java.util.UUID;
import teacommontea.veritesauver.core.Entry;
import teacommontea.veritesauver.core.SauverDAO;
import teacommontea.veritesauver.core.SauverEvents;
import teacommontea.veritesauver.geoip.SauverGeoIp;
import teacommontea.veritesauver.chat.SauverChat;
import teacommontea.veritesauver.login.SauverListeners;
import teacommontea.veritesauver.punish.PunishTree;
import teacommontea.veritesauver.util.SauverConfig;

public final class Sauver {

    private static Sauver instance;

    private final Plugin plugin;
    private final KvStore store;
    private final SauverDAO dao;
    private final SauverMessages messages;
    private final SauverChat chat;
    private final PunishTree tree;

    private teacommontea.util.sched.TaskHandle sweepTask;
    private teacommontea.util.sched.TaskHandle playtimeTask;

    private Sauver(Plugin plugin, KvStore store) {
        this.plugin = plugin;
        this.store = store;
        this.dao = new SauverDAO(store);
        this.messages = new SauverMessages();
        this.chat = new SauverChat(this);
        this.tree = new PunishTree(this);
    }

    public static Sauver enable(Plugin pl) throws Exception {
        KvStore store = KvStore.open("sauver", true, false);
        Sauver s = new Sauver(pl, store);
        instance = s;

        pl.getServer().getPluginManager().registerEvents(new SauverListeners(s), pl);
        pl.getServer().getPluginManager().registerEvents(s.tree, pl);
        SauverConfig.load(pl.getDataFolder());
        SauverGeoIp.load(pl.getDataFolder());

        s.sweepTask = teacommontea.util.sched.Sched.executeGlobalRepeating(s::sweepExpired, 600L, 600L);
        s.playtimeTask = teacommontea.util.sched.Sched.executeGlobalRepeating(s::flushPlaytime, 1200L, 1200L);
        return s;
    }

    public void disable() {
        if (sweepTask != null) {
            sweepTask.cancel();
            sweepTask = null;
        }
        if (playtimeTask != null) {
            playtimeTask.cancel();
            playtimeTask = null;
        }

        long now = System.currentTimeMillis();
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            dao.recordLogout(p.getUniqueId(), now);
        }
        store.shutdown();
        if (instance == this) {
            instance = null;
        }
    }

    public void reload() {
        SauverConfig.load(plugin.getDataFolder());
        SauverGeoIp.load(plugin.getDataFolder());
    }

    void sweepExpired() {
        long now = System.currentTimeMillis();
        for (SauverDAO.ActivePointer ptr : dao.activePointers()) {
            Entry e = dao.load(ptr.entryId());
            if (e != null && e.active() && e.expired(now)) {
                Entry lapsed = dao.expire(ptr.entryId());
                if (lapsed != null) {
                    SauverEvents.fireRemoved(lapsed);
                }
            }
        }
    }

    void flushPlaytime() {
        long now = System.currentTimeMillis();
        for (org.bukkit.entity.Player p : org.bukkit.Bukkit.getOnlinePlayers()) {
            dao.flushSession(p.getUniqueId(), now);
        }
    }

    public static Sauver instance()     { return instance; }
    public Plugin plugin()              { return plugin; }
    public SauverDAO dao()              { return dao; }
    public KvStore store()          { return store; }
    public SauverMessages messages()    { return messages; }
    public SauverChat chat()            { return chat; }
    public PunishTree tree()            { return tree; }

    public Entry activeMute(UUID u) {
        Entry own = inForceOrNull(dao.activeMute(u));
        if (own != null) {
            return own;
        }
        org.bukkit.entity.Player p = org.bukkit.Bukkit.getPlayer(u);
        if (p != null && p.getAddress() != null && p.getAddress().getAddress() != null) {
            String ip = p.getAddress().getAddress().getHostAddress();
            Entry ipMute = dao.activeIpPunishment(Entry.Type.MUTE, ip, System.currentTimeMillis());
            if (ipMute != null) {
                return ipMute;
            }
        }
        return own;
    }

    public Entry activeBan(UUID u) {
        return inForceOrNull(dao.activeBan(u));
    }

    private static Entry inForceOrNull(Entry e) {
        if (e == null) {
            return null;
        }
        return e.inForce(System.currentTimeMillis()) ? e : null;
    }
}
