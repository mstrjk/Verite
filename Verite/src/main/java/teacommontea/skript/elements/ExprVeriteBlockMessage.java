package teacommontea.skript.elements;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteFilter;

public class ExprVeriteBlockMessage extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprVeriteBlockMessage.class, String.class, ExpressionType.SIMPLE,
                "block message");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    protected String[] get(Event event) {
        return new String[] { VeriteFilter.blockMessage() };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "block message";
    }
}
