package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteModeration;

public class EffVeritePardon extends Effect {

    static {
        Skript.registerEffect(EffVeritePardon.class,
                "verite unban %player% [(with reason|for) %-string%] [by %-player%]",
                "verite unmute %player% [(with reason|for) %-string%] [by %-player%]",
                "verite unwarn %player% [(with reason|for) %-string%] [by %-player%]");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<Player> target;
    private Expression<String> reason;
    private Expression<Player> remover;
    private int verb;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        verb = matchedPattern;
        target = (Expression<Player>) exprs[0];
        reason = (Expression<String>) exprs[1];
        remover = (Expression<Player>) exprs[2];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Player p = target.getSingle(event);
        if (p == null) {
            return;
        }
        String why = reason != null ? reason.getSingle(event) : null;
        if (why == null) {
            why = "";
        }
        Player by = remover != null ? remover.getSingle(event) : null;
        java.util.UUID byUuid = by != null ? by.getUniqueId() : null;
        String byName = by != null ? by.getName() : "Console";
        switch (verb) {
            case 0:  VeriteModeration.unban(p.getUniqueId(), p.getName(), byUuid, byName, why); break;
            case 1:  VeriteModeration.unmute(p.getUniqueId(), p.getName(), byUuid, byName, why); break;
            default: VeriteModeration.unwarn(p.getUniqueId(), p.getName(), byUuid, byName, why); break;
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        String verbName = verb == 0 ? "unban" : verb == 1 ? "unmute" : "unwarn";
        return "verite " + verbName + " " + target.toString(event, debug);
    }
}
