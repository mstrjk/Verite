package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

import teacommontea.api.Punishment;
import teacommontea.api.VeriteModeration;

public class ExprVeritePunishInfo extends SimpleExpression<Object> {

    static {
        Skript.registerExpression(ExprVeritePunishInfo.class, Object.class, ExpressionType.COMBINED,
                "[get] [verite] ban status of %player% [reason:with reason]",
                "[get] [verite] mute status of %player% [reason:with reason]");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<Player> player;
    private boolean ban;
    private boolean withReason;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        ban = matchedPattern == 0;
        withReason = parseResult.hasTag("reason");
        return true;
    }

    @Override
    protected Object[] get(Event event) {
        Player p = player.getSingle(event);
        if (p == null) {
            return new Object[0];
        }
        Punishment punishment = ban
                ? VeriteModeration.activeBan(p.getUniqueId())
                : VeriteModeration.activeMute(p.getUniqueId());
        if (punishment == null) {
            return new Object[0];
        }
        if (withReason) {
            return new Object[] { punishment.reason() == null ? "" : punishment.reason() };
        }
        return new Object[] { (Number) punishment.remaining(System.currentTimeMillis()) };
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
        return (ban ? "ban" : "mute") + " status of " + player.toString(event, debug) + (withReason ? " with reason" : "");
    }
}
