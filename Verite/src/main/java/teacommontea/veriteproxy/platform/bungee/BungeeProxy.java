package teacommontea.veriteproxy.platform.bungee;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import teacommontea.veriteproxy.ProxyConfig;
import teacommontea.veriteproxy.ProxyStore;
import teacommontea.veriteproxy.PunishmentGate;
import teacommontea.veritesauver.core.Entry;

import java.net.InetSocketAddress;
import java.util.UUID;

@SuppressWarnings("deprecation")
public final class BungeeProxy extends Plugin implements Listener {

    private ProxyConfig config;
    private ProxyStore store;
    private PunishmentGate gate;

    @Override
    public void onEnable() {
        config = ProxyConfig.load(getDataFolder());
        try {
            store = ProxyStore.open(config);
        } catch (Exception ex) {
            getLogger().severe(teacommontea.util.ConsoleColours.bad("Verité proxy could not open the shared H2 store. Punishment enforcement is disabled.") + teacommontea.util.Trace.of(ex));
            return;
        }
        gate = new PunishmentGate(store.dao(), config.banAlts());
        getProxy().getPluginManager().registerListener(this, this);
        getLogger().info(teacommontea.util.ConsoleColours.ok("Verité proxy enforcement active. The shared store is read-only."));
    }

    @Override
    public void onDisable() {
        if (store != null) {
            store.close();
        }
    }

    @EventHandler
    public void onLogin(LoginEvent e) {
        if (gate == null) {
            return;
        }
        PendingConnection conn = e.getConnection();
        UUID uuid = conn.getUniqueId();
        String ip = addressOf(conn);
        long now = System.currentTimeMillis();

        Entry ban = gate.resolveLoginBan(uuid, ip, now);
        if (ban != null) {
            e.setCancelled(true);
            e.setCancelReason(TagParser.parse(PunishmentGate.banScreen(ban)));
        }
    }

    @EventHandler
    public void onChat(ChatEvent e) {
        if (gate == null || e.isCancelled()) {
            return;
        }
        if (e.isCommand() && !config.enforceMuteCommands()) {
            return;
        }
        Connection sender = e.getSender();
        if (!(sender instanceof ProxiedPlayer player)) {
            return;
        }
        String ip = addressOf(player.getPendingConnection());
        long now = System.currentTimeMillis();

        Entry mute = gate.resolveMute(player.getUniqueId(), ip, now);
        if (mute != null) {
            e.setCancelled(true);
            player.sendMessage(TagParser.parse(PunishmentGate.muteNotice(mute)));
        }
    }

    private static String addressOf(Connection conn) {
        if (conn == null) {
            return null;
        }
        InetSocketAddress addr = conn.getAddress();
        return addr == null || addr.getAddress() == null ? null : addr.getAddress().getHostAddress();
    }
}
