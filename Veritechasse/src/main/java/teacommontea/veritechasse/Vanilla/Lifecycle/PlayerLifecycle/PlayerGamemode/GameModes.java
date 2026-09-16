package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerGamemode;

public final class GameModes {

    public static final String KEY = "game_modes";

    public static final String SURVIVAL = "survival";
    public static final String CREATIVE = "creative";
    public static final String ADVENTURE = "adventure";
    public static final String SPECTATOR = "spectator";

    public static final int SURVIVAL_ID = 0;
    public static final int CREATIVE_ID = 1;
    public static final int ADVENTURE_ID = 2;
    public static final int SPECTATOR_ID = 3;

    public static final String DEFAULT_MODE = SURVIVAL;

    private GameModes() {
    }

    public static boolean isKnown(String mode) {
        return SURVIVAL.equals(mode)
            || CREATIVE.equals(mode)
            || ADVENTURE.equals(mode)
            || SPECTATOR.equals(mode);
    }

    public static int idOf(String mode) {
        if (CREATIVE.equals(mode)) {
            return CREATIVE_ID;
        }
        if (ADVENTURE.equals(mode)) {
            return ADVENTURE_ID;
        }
        if (SPECTATOR.equals(mode)) {
            return SPECTATOR_ID;
        }
        return SURVIVAL_ID;
    }

    public static String byId(int id) {
        if (id == CREATIVE_ID) {
            return CREATIVE;
        }
        if (id == ADVENTURE_ID) {
            return ADVENTURE;
        }
        if (id == SPECTATOR_ID) {
            return SPECTATOR;
        }
        return SURVIVAL;
    }

    public static boolean isCreative(String mode) {
        return CREATIVE.equals(mode);
    }

    public static boolean isSurvival(String mode) {
        return SURVIVAL.equals(mode) || ADVENTURE.equals(mode);
    }

    public static boolean isSpectator(String mode) {
        return SPECTATOR.equals(mode);
    }

    public static boolean isBlockPlacingRestricted(String mode) {
        return ADVENTURE.equals(mode) || SPECTATOR.equals(mode);
    }
}
