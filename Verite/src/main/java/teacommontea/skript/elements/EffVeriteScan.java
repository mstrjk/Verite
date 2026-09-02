package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteFilter;

public class EffVeriteScan extends Effect {

    static {
        Skript.registerEffect(EffVeriteScan.class,
                "check %string% for %player%");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<String> text;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<Player> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        text = (Expression<String>) exprs[0];
        player = (Expression<Player>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        String message = text.getSingle(event);
        Player p = player.getSingle(event);
        if (message == null || message.isEmpty() || p == null) {
            return;
        }
        VeriteFilter.check(p.getUniqueId(), message);
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "check " + text.toString(event, debug) + " for " + player.toString(event, debug);
    }
}
