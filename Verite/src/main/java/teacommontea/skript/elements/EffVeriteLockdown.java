package teacommontea.skript.elements;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteLockdown;

public class EffVeriteLockdown extends Effect {

    static {
        Skript.registerEffect(EffVeriteLockdown.class,
                "begin message lockdown [(with reason|for) %-string%]",
                "end message lockdown");
    }

    private Expression<String> reason;
    private boolean end;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        end = matchedPattern == 1;
        reason = end ? null : (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (end) {
            VeriteLockdown.end();
            return;
        }
        String why = reason != null ? reason.getSingle(event) : null;
        VeriteLockdown.begin(why == null ? "Lockdown" : why);
    }

    @Override
    public String toString(Event event, boolean debug) {
        return end ? "end message lockdown" : "begin message lockdown";
    }
}
