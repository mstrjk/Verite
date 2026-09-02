package teacommontea.skript.elements;

import teacommontea.api.VeriteVanish;
import teacommontea.skript.VeritePlayerCondition;

public class CondVeriteVanished extends VeritePlayerCondition {

    static {
        register(CondVeriteVanished.class, "vanished in verite", "players");
    }

    public CondVeriteVanished() {
        super(VeriteVanish::isVanished, "vanished in verite");
    }
}
