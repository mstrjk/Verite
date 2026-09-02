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

public class ExprVeriteName extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprVeriteName.class, String.class, ExpressionType.PROPERTY,
                "[get] [verite] name of %player%");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<org.bukkit.entity.Player> player;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        player = (Expression<org.bukkit.entity.Player>) exprs[0];
        return true;
    }

    @Override
    protected String[] get(Event event) {
        org.bukkit.entity.Player p = player.getSingle(event);
        if (p == null) {
            return new String[0];
        }
        UUID uuid = p.getUniqueId();
        String name = VeritePlayerData.nameOf(uuid);
        return name == null ? new String[0] : new String[] { name };
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
        return "verite name of " + player.toString(event, debug);
    }
}
