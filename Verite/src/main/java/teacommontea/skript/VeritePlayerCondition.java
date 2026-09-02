package teacommontea.skript;

import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.entity.Player;

import ch.njol.skript.conditions.base.PropertyCondition;


public abstract class VeritePlayerCondition extends PropertyCondition<Player> {

    private final Predicate<UUID> test;
    private final String property;

    protected VeritePlayerCondition(Predicate<UUID> test, String property) {
        this.test = test;
        this.property = property;
    }

    @Override
    public boolean check(Player player) {
        return test.test(player.getUniqueId());
    }

    @Override
    protected String getPropertyName() {
        return property;
    }
}

