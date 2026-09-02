package teacommontea.skript.elements;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteFormat;

public class ExprVeriteFancyTime extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprVeriteFancyTime.class, String.class, ExpressionType.COMBINED,
                "[get] fancy time of %number%");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<Number> millis;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        millis = (Expression<Number>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event event) {
        Number n = millis.getSingle(event);
        if (n == null) {
            return new String[0];
        }
        return new String[] { VeriteFormat.fancyTime(n.longValue()) };
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
        return "fancy time of " + millis.toString(event, debug);
    }
}
