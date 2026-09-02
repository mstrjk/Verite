package teacommontea.skript.elements;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteGeoIp;

public class ExprVeriteCountry extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprVeriteCountry.class, String.class, ExpressionType.COMBINED,
                "[get] country of %string%");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<String> ip;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        ip = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event event) {
        String address = ip.getSingle(event);
        if (address == null || address.isEmpty()) {
            return new String[0];
        }
        String country = VeriteGeoIp.country(address);
        return country == null ? new String[0] : new String[] { country };
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
        return "country of " + ip.toString(event, debug);
    }
}
