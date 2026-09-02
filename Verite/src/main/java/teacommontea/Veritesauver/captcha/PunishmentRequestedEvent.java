package teacommontea.veritesauver.captcha;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public final class PunishmentRequestedEvent extends Event implements Cancellable {

    public enum Cause {
        CAPTCHA_FAILED,
        CAPTCHA_TIMEOUT
    }

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final UUID playerId;
    private final Cause cause;
    private final String source;
    private String kickMessage;
    private boolean cancelled;

    public PunishmentRequestedEvent(Player player, Cause cause, String source, String kickMessage) {
        this.player = player;
        this.playerId = player.getUniqueId();
        this.cause = cause;
        this.source = source;
        this.kickMessage = kickMessage;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Cause getCause() {
        return cause;
    }

    public String getSource() {
        return source;
    }

    public String getKickMessage() {
        return kickMessage;
    }

    public void setKickMessage(String kickMessage) {
        this.kickMessage = kickMessage;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
