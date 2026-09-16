package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerRespawn;

public final class RespawnRules {

    public static final String KEY = "respawn_rules";

    public static final String IMMEDIATE_RESPAWN_RULE = "immediate_respawn";

    public static final String KEEP_INVENTORY_RULE = "keep_inventory";

    public static final boolean IMMEDIATE_RESPAWN_DEFAULT = false;

    public static final boolean KEEP_INVENTORY_DEFAULT = false;

    public static final String BED = "bed";
    public static final String RESPAWN_ANCHOR = "respawn_anchor";
    public static final String WORLD_SPAWN = "world_spawn";

    private RespawnRules() {
    }

    public static boolean skipsDeathScreen(boolean immediateRespawnRule) {
        return immediateRespawnRule;
    }

    public static boolean keepsInventory(boolean keepInventoryRule) {
        return keepInventoryRule;
    }

    public static boolean dropsInventory(boolean keepInventoryRule, boolean spectator) {
        if (spectator) {
            return false;
        }
        return !keepsInventory(keepInventoryRule);
    }

    public static String respawnSource(boolean hasBed, boolean hasAnchor) {
        if (hasAnchor) {
            return RESPAWN_ANCHOR;
        }
        return hasBed ? BED : WORLD_SPAWN;
    }

    public static boolean respawnPositionIsKnown(boolean hasBed, boolean hasAnchor) {
        return hasBed || hasAnchor;
    }

    public static boolean baselineIsInvalidatedByRespawn() {
        return true;
    }

    public static boolean replacesEntityInstance() {
        return true;
    }

    public static boolean momentumIsClearedByRespawn() {
        return replacesEntityInstance();
    }

    public static boolean fallDistanceIsClearedByRespawn() {
        return replacesEntityInstance();
    }

    public static boolean positionIsSnapped() {
        return true;
    }

    public static boolean endPortalReturnKeepsData(boolean fromEndPortal) {
        return fromEndPortal;
    }
}
