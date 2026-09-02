package teacommontea.skript.elements;

import java.util.UUID;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import teacommontea.api.VeritePlayerData;

public class ExprVeriteUuid extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprVeriteUuid.class, String.class, ExpressionType.COMBINED,
                "[get] [verite] uuid of %string%");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<String> name;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        name = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event event) {
        String n = name.getSingle(event);
        if (n == null || n.isEmpty()) {
            return new String[0];
        }
        UUID uuid = VeritePlayerData.uuidByName(n);
        return uuid == null ? new String[0] : new String[] { uuid.toString() };
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
        return "verite uuid of " + name.toString(event, debug);
    }
}
