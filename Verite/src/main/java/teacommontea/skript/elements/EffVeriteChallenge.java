package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

import teacommontea.api.VeriteCaptcha;

public class EffVeriteChallenge extends Effect {

    static {
        Skript.registerEffect(EffVeriteChallenge.class,
                "challenge %player% with [a] captcha [(with reason|for|from) %-string%]",
                "challenge %player% with [a] detailed captcha [(with reason|for|from) %-string%]");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<Player> player;
    private Expression<String> source;
    private boolean detailed;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        player = (Expression<Player>) exprs[0];
        source = (Expression<String>) exprs[1];
        detailed = matchedPattern == 1;
        return true;
    }

    @Override
    protected void execute(Event event) {
        Player p = player.getSingle(event);
        if (p == null) {
            return;
        }
        String why = source != null ? source.getSingle(event) : null;
        if (why == null) {
            why = "skript";
        }
        if (detailed) {
            VeriteCaptcha.challengeDetailed(p, why);
        } else {
            VeriteCaptcha.challengeStandard(p, why);
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "challenge " + player.toString(event, debug) + " with a " + (detailed ? "detailed " : "") + "captcha";
    }
}
