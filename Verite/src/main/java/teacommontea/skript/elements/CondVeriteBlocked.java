package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteFilter;

public class CondVeriteBlocked extends Condition {

    static {
        Skript.registerCondition(CondVeriteBlocked.class,
                "%string% (was|is|are) blocked [for %-player%]",
                "%string% (wasn't|was not|isn't|is not|aren't|are not) blocked [for %-player%]");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<String> text;
    private Expression<Player> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        text = (Expression<String>) exprs[0];
        player = (Expression<Player>) exprs[1];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(Event event) {
        String message = text.getSingle(event);
        if (message == null || message.isEmpty()) {
            return isNegated();
        }
        Player p = player != null ? player.getSingle(event) : null;
        java.util.UUID id = p != null ? p.getUniqueId() : VeriteFilter.ANONYMOUS;
        boolean blocked = VeriteFilter.check(id, message).blocks();
        return blocked != isNegated();
    }

    @Override
    public String toString(Event event, boolean debug) {
        return text.toString(event, debug) + (isNegated() ? " was not" : " was") + " blocked";
    }
}
