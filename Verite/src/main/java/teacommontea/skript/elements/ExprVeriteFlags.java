package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteFilter;

public class ExprVeriteFlags extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprVeriteFlags.class, Number.class, ExpressionType.PROPERTY,
                "[get] message flags (for|of) %player%");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<Player> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    protected Number[] get(Event event) {
        Player p = player.getSingle(event);
        if (p == null) {
            return new Number[] { 0 };
        }
        return new Number[] { VeriteFilter.count(p.getUniqueId()) };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "message flags for " + player.toString(event, debug);
    }
}
