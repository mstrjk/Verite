package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerExit;

public final class DisconnectReasons {

    public static final String KEY = "disconnect_reasons";

    public static final String FLYING = "multiplayer.disconnect.flying";
    public static final String IDLING = "multiplayer.disconnect.idling";
    public static final String SLOW_LOGIN = "multiplayer.disconnect.slow_login";
    public static final String TIMEOUT = "disconnect.timeout";
    public static final String INVALID_PLAYER_MOVEMENT =
        "multiplayer.disconnect.invalid_player_movement";
    public static final String INVALID_VEHICLE_MOVEMENT =
        "multiplayer.disconnect.invalid_vehicle_movement";

    private DisconnectReasons() {
    }

    public static boolean isMovementRelated(String reason) {
        return FLYING.equals(reason)
            || INVALID_PLAYER_MOVEMENT.equals(reason)
            || INVALID_VEHICLE_MOVEMENT.equals(reason);
    }

    public static boolean isVanillaEnforced(String reason) {
        return isMovementRelated(reason)
            || IDLING.equals(reason)
            || SLOW_LOGIN.equals(reason)
            || TIMEOUT.equals(reason);
    }

    public static boolean stateIsPersistedOnExit() {
        return true;
    }

    public static boolean baselineIsDiscardedOnExit() {
        return true;
    }

    public static boolean idleKickApplies(
            long lastActionTimeMillis,
            long nowMillis,
            int playerIdleTimeoutMinutes,
            boolean wonGame) {
        if (lastActionTimeMillis <= 0L || playerIdleTimeoutMinutes <= 0 || wonGame) {
            return false;
        }
        long limitMillis = (long) playerIdleTimeoutMinutes * 60L * 1000L;
        return nowMillis - lastActionTimeMillis > limitMillis;
    }
}
