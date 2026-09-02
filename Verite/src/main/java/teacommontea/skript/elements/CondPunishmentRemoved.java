package teacommontea.skript.elements;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import teacommontea.skript.VeritePunishmentEvent;

public class CondPunishmentRemoved extends Condition {

    static {
        Skript.registerCondition(CondPunishmentRemoved.class,
                "[the] verite punishment was removed",
                "[the] verite punishment was (not removed|issued)");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (!(event instanceof VeritePunishmentEvent)) {
            return isNegated();
        }
        return ((VeritePunishmentEvent) event).removed() != isNegated();
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "the verite punishment was " + (isNegated() ? "issued" : "removed");
    }
}
