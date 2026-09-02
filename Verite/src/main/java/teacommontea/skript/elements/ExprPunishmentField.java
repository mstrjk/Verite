package teacommontea.skript.elements;

import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import teacommontea.api.Punishment;

public class ExprPunishmentField extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprPunishmentField.class, Object.class, ExpressionType.PROPERTY,
                "[get] punishment type of %veritepunishment%",
                "[get] punishment reason of %veritepunishment%",
                "[get] punishment target uuid of %veritepunishment%",
                "[get] punishment target ip of %veritepunishment%",
                "[get] punishment executor name of %veritepunishment%",
                "[get] punishment removed by name of %veritepunishment%",
                "[get] punishment removal reason of %veritepunishment%",
                "[get] punishment length of %veritepunishment%");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<Punishment> source;
    private int field;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        Expression<? extends Punishment> converted =
                exprs[0].getConvertedExpression(Punishment.class);
        if (converted == null) {
            return false;
        }
        source = (Expression<Punishment>) converted;
        field = matchedPattern;
        return true;
    }

    @Override
    protected Object[] get(Event event) {
        Punishment p = source.getSingle(event);
        if (p == null) {
            return new Object[0];
        }
        switch (field) {
            case 0:  return one(p.type() == null ? null : p.type().id());
            case 1:  return one(p.reason());
            case 2:  return one(p.uuid() == null ? null : p.uuid().toString());
            case 3:  return one(p.ip());
            case 4:  return one(p.executorName());
            case 5:  return one(p.removedByName());
            case 6:  return one(p.removalReason());
            default: return new Object[] { (Number) p.duration() };
        }
    }

    private static Object[] one(String value) {
        return value == null ? new Object[0] : new Object[] { value };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return Object.class;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "punishment field of " + source.toString(event, debug);
    }
}
