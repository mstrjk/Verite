package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteModeration;

public class CondVeriteMuteGate extends Condition {

    static {
        Skript.registerCondition(CondVeriteMuteGate.class,
                "chat is frozen [for %-player%]",
                "%player% is rate limited",
                "%player% is on slowmode");
    }

    private Expression<Player> player;
    private boolean slowmode;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        slowmode = matchedPattern != 0;
        return true;
    }

    @Override
    public boolean check(Event event) {
        Player p = player != null ? player.getSingle(event) : null;
        if (p == null) {
            return false;
        }
        return slowmode ? VeriteModeration.slowmodeGate(p) : VeriteModeration.chatMuteGate(p);
    }

    @Override
    public String toString(Event event, boolean debug) {
        return slowmode
                ? player.toString(event, debug) + " is on slowmode"
                : "chat is frozen";
    }
}
