package teacommontea.veritechasse.Vanilla.ControllableEntities.Minecarts.Support;

import teacommontea.veritechasse.Vanilla.Protocol;

public enum MinecartBehaviour {

    OLD,
    NEW;

    public static final int NEW_BEHAVIOUR_PROTOCOL_MAJOR = 1;
    public static final int NEW_BEHAVIOUR_PROTOCOL_MINOR = 21;
    public static final int NEW_BEHAVIOUR_PROTOCOL_PATCH = 3;

    public static final String FEATURE_FLAG = "minecart_improvements";

    public static boolean availableIn(Protocol protocol) {
        return protocol.atLeast(
            NEW_BEHAVIOUR_PROTOCOL_MAJOR,
            NEW_BEHAVIOUR_PROTOCOL_MINOR,
            NEW_BEHAVIOUR_PROTOCOL_PATCH);
    }

    public static MinecartBehaviour of(Protocol protocol, boolean featureFlagEnabled) {
        if (!availableIn(protocol)) {
            return OLD;
        }
        return featureFlagEnabled ? NEW : OLD;
    }

    public boolean usesGameRuleSpeed() {
        return this == NEW;
    }
}
