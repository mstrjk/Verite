package teacommontea.skript.elements;

import teacommontea.api.VeriteModeration;
import teacommontea.skript.VeritePlayerCondition;

public class CondVeriteBanned extends VeritePlayerCondition {

    static {
        register(CondVeriteBanned.class, "banned by verite", "players");
    }

    public CondVeriteBanned() {
        super(VeriteModeration::isBanned, "banned by verite");
    }
}
