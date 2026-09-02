package teacommontea.veriteproxy.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import teacommontea.veriteproxy.ProxyConfig;
import teacommontea.veriteproxy.ProxyStore;
import teacommontea.veriteproxy.PunishmentGate;
import teacommontea.veritesauver.core.Entry;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.UUID;

@Plugin(
        id = "verite",
        name = "Verite",
        version = "1.2.7",
        description = "Read-only network punishment enforcement for Verite.",
        authors = {"teacommontea"}
)
public final class VelocityProxy {

    private final ProxyServer server;
    private final Logger logger;
    private final File dataFolder;

    private ProxyConfig config;
    private ProxyStore store;
    private PunishmentGate gate;

    @Inject
    public VelocityProxy(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder.toFile();
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent e) {
        config = ProxyConfig.load(dataFolder);
        try {
            store = ProxyStore.open(config);
        } catch (Exception ex) {
            logger.error("Verite proxy could not open the shared H2 store; punishment "
                    + "enforcement is disabled: " + ex.getMessage());
            return;
        }
        gate = new PunishmentGate(store.dao(), config.banAlts());
        logger.info("Verite proxy enforcement active (read-only shared store).");
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent e) {
        if (store != null) {
            store.close();
        }
    }

    @Subscribe
    public void onLogin(LoginEvent e) {
        if (gate == null) {
            return;
        }
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        String ip = ipOf(player);
        long now = System.currentTimeMillis();

        Entry ban = gate.resolveLoginBan(uuid, ip, now);
        if (ban != null) {
            e.setResult(ResultedEvent.ComponentResult.denied(
                    Component.text(PunishmentGate.stripTags(PunishmentGate.banScreen(ban)))));
        }
    }

    @Subscribe
    @SuppressWarnings("deprecation")
    public void onChat(PlayerChatEvent e) {
        if (gate == null) {
            return;
        }
        Player player = e.getPlayer();
        String ip = ipOf(player);
        long now = System.currentTimeMillis();

        Entry mute = gate.resolveMute(player.getUniqueId(), ip, now);
        if (mute != null) {
            e.setResult(PlayerChatEvent.ChatResult.denied());
            player.sendMessage(Component.text(
                    PunishmentGate.stripTags(PunishmentGate.muteNotice(mute))));
        }
    }

    private static String ipOf(Player player) {
        InetSocketAddress addr = player.getRemoteAddress();
        return addr == null || addr.getAddress() == null ? null : addr.getAddress().getHostAddress();
    }
}
