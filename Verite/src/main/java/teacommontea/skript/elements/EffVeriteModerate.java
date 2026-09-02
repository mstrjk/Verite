package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;

import teacommontea.api.Punishment;
import teacommontea.api.VeriteModeration;

public class EffVeriteModerate extends Effect {

    static {
        Skript.registerEffect(EffVeriteModerate.class,
                "verite ban %player% [for %-timespan%] [(with reason|for) %-string%] [by %-player%] [silent:silently]",
                "verite mute %player% [for %-timespan%] [(with reason|for) %-string%] [by %-player%] [silent:silently]",
                "verite kick %player% [(with reason|for) %-string%] [by %-player%] [silent:silently]",
                "verite warn %player% [(with reason|for) %-string%] [by %-player%] [silent:silently]");
    }

    @SuppressWarnings("NotNullFieldNotInitialized")
    private Expression<Player> target;
    private Expression<Timespan> duration;
    private Expression<String> reason;
    private Expression<Player> executor;
    private boolean silent;
    private int verb;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        verb = matchedPattern;
        target = (Expression<Player>) exprs[0];
        boolean hasDuration = verb == 0 || verb == 1;
        int i = 1;
        duration = hasDuration ? (Expression<Timespan>) exprs[i++] : null;
        reason = (Expression<String>) exprs[i++];
        executor = (Expression<Player>) exprs[i];
        silent = parseResult.hasTag("silent");
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
        Player by = executor != null ? executor.getSingle(event) : null;
        java.util.UUID execUuid = by != null ? by.getUniqueId() : null;
        String execName = by != null ? by.getName() : "Console";
        long millis = Punishment.PERMANENT;
        if (duration != null) {
            Timespan span = duration.getSingle(event);
            if (span != null) {
                millis = millisOf(span);
            }
        }
        switch (verb) {
            case 0:
                VeriteModeration.ban(p.getUniqueId(), p.getName(), why, execUuid, execName, millis, silent);
                break;
            case 1:
                VeriteModeration.mute(p.getUniqueId(), p.getName(), why, execUuid, execName, millis, silent);
                break;
            case 2:
                VeriteModeration.kick(p.getUniqueId(), p.getName(), why, execUuid, execName, silent);
                break;
            default:
                VeriteModeration.warn(p.getUniqueId(), p.getName(), why, execUuid, execName, silent);
                break;
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        String verbName = verb == 0 ? "ban" : verb == 1 ? "mute" : verb == 2 ? "kick" : "warn";
        return "verite " + verbName + " " + target.toString(event, debug);
    }

    private static long millisOf(Timespan span) {
        try {
            Class<?> period = Class.forName("ch.njol.skript.util.Timespan$TimePeriod");
            Object millisecond = null;
            for (Object constant : period.getEnumConstants()) {
                if (((Enum<?>) constant).name().equals("MILLISECOND")) {
                    millisecond = constant;
                    break;
                }
            }
            if (millisecond != null) {
                return (long) Timespan.class.getMethod("getAs", period).invoke(span, millisecond);
            }
        } catch (Throwable ignored) {
        }
        try {
            return (long) Timespan.class.getMethod("getMilliSeconds").invoke(span);
        } catch (Throwable ignored) {
            return 0L;
        }
    }
}
