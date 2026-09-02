package teacommontea.skript.elements;

import teacommontea.api.VeriteModeration;
import teacommontea.skript.VeritePlayerCondition;

public class CondVeriteMuted extends VeritePlayerCondition {

    static {
        register(CondVeriteMuted.class, "muted", "players");
    }

    public CondVeriteMuted() {
        super(VeriteModeration::isMuted, "muted");
    }
}
