package teacommontea.skript;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import teacommontea.api.Punishment;

public final class VeritePunishmentEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Punishment punishment;
    private final boolean removed;

    public VeritePunishmentEvent(Punishment punishment, boolean removed) {
        this.punishment = punishment;
        this.removed = removed;
    }

    public Punishment punishment() {
        return punishment;
    }

    public boolean removed() {
        return removed;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
