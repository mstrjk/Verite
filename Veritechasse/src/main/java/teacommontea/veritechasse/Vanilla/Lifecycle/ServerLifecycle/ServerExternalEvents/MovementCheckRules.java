package teacommontea.veritechasse.Vanilla.Lifecycle.ServerLifecycle.ServerExternalEvents;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class MovementCheckRules {

    public static final String KEY = "movement_check_rules";

    public static final int RULES_EXIST_MAJOR = 1;
    public static final int RULES_EXIST_MINOR = 21;
    public static final int RULES_EXIST_PATCH = 2;

    public static final int DIRECT_POLARITY_MAJOR = 1;
    public static final int DIRECT_POLARITY_MINOR = 21;
    public static final int DIRECT_POLARITY_PATCH = 11;

    public static final String DISABLE_PLAYER_RULE = "disablePlayerMovementCheck";
    public static final String DISABLE_ELYTRA_RULE = "disableElytraMovementCheck";

    public static final String PLAYER_RULE = "player_movement_check";
    public static final String ELYTRA_RULE = "elytra_movement_check";

    private MovementCheckRules() {
    }

    public static boolean rulesExist(Protocol protocol) {
        return protocol.atLeast(
            RULES_EXIST_MAJOR, RULES_EXIST_MINOR, RULES_EXIST_PATCH);
    }

    public static boolean rulesUseDirectPolarity(Protocol protocol) {
        return protocol.atLeast(
            DIRECT_POLARITY_MAJOR, DIRECT_POLARITY_MINOR, DIRECT_POLARITY_PATCH);
    }

    public static String playerRuleName(Protocol protocol) {
        return rulesUseDirectPolarity(protocol) ? PLAYER_RULE : DISABLE_PLAYER_RULE;
    }

    public static String elytraRuleName(Protocol protocol) {
        return rulesUseDirectPolarity(protocol) ? ELYTRA_RULE : DISABLE_ELYTRA_RULE;
    }

    public static boolean playerCheckEnabled(boolean ruleValue, Protocol protocol) {
        if (!rulesExist(protocol)) {
            return true;
        }
        return rulesUseDirectPolarity(protocol) ? ruleValue : !ruleValue;
    }

    public static boolean elytraCheckEnabled(boolean ruleValue, Protocol protocol) {
        if (!rulesExist(protocol)) {
            return true;
        }
        return rulesUseDirectPolarity(protocol) ? ruleValue : !ruleValue;
    }

    public static boolean checkApplies(
            boolean playerRuleValue,
            boolean elytraRuleValue,
            boolean fallFlying,
            Protocol protocol) {
        if (!playerCheckEnabled(playerRuleValue, protocol)) {
            return false;
        }
        return !fallFlying || elytraCheckEnabled(elytraRuleValue, protocol);
    }

    public static boolean defaultRuleValue(Protocol protocol) {
        return rulesUseDirectPolarity(protocol);
    }
}
