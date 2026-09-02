package teacommontea.skript.elements;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

import teacommontea.api.VeriteVanish;

public class ExprVeriteVanishState extends SimplePropertyExpression<Player, Boolean> {

    static {
        register(ExprVeriteVanishState.class, Boolean.class, "vanish state", "players");
    }

    @Override
    public Boolean convert(Player player) {
        return VeriteVanish.isVanished(player.getUniqueId());
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Boolean.class) : null;
    }

    @Override
    public void change(Event event, Object[] delta, Changer.ChangeMode mode) {
        if (mode != Changer.ChangeMode.SET || delta == null || delta.length == 0) {
            return;
        }
        boolean vanished = Boolean.TRUE.equals(delta[0]);
        for (Player p : getExpr().getArray(event)) {
            if (vanished) {
                VeriteVanish.vanish(p);
            } else {
                VeriteVanish.unvanish(p);
            }
        }
    }

    @Override
    public Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

    @Override
    protected String getPropertyName() {
        return "vanish state";
    }
}
