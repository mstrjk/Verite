package teacommontea.skript.elements;

import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import teacommontea.api.FilterResult;
import teacommontea.api.VeriteFilter;

public class ExprVeriteFilter extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprVeriteFilter.class, String.class, ExpressionType.COMBINED,
                "filter [the] result of %string% [for %-player%]");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<String> text;
    private Expression<Player> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        text = (Expression<String>) exprs[0];
        player = (Expression<Player>) exprs[1];
        return true;
    }

    @Override
    protected String[] get(Event event) {
        String message = text.getSingle(event);
        if (message == null || message.isEmpty()) {
            return new String[] { FilterResult.CLEAN.name().toLowerCase(Locale.ROOT) };
        }
        Player p = player != null ? player.getSingle(event) : null;
        java.util.UUID id = p != null ? p.getUniqueId() : VeriteFilter.ANONYMOUS;
        FilterResult result = VeriteFilter.check(id, message);
        return new String[] { result.name().toLowerCase(Locale.ROOT) };
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
        return "filter the result of " + text.toString(event, debug);
    }
}
