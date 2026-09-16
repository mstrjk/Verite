package teacommontea.veritechasse.Vanilla.Lifecycle.PlayerLifecycle.PlayerGamemode;

public final class ModeAbilities {

    public static final String KEY = "mode_abilities";

    private ModeAbilities() {
    }

    public static boolean mayFly(String mode) {
        return GameModes.isCreative(mode) || GameModes.isSpectator(mode);
    }

    public static boolean instabuild(String mode) {
        return GameModes.isCreative(mode);
    }

    public static boolean invulnerable(String mode) {
        return GameModes.isCreative(mode) || GameModes.isSpectator(mode);
    }

    public static boolean forcedFlying(String mode) {
        return GameModes.isSpectator(mode);
    }

    public static boolean mayBuild(String mode) {
        return !GameModes.isBlockPlacingRestricted(mode);
    }

    public static boolean creativeForcesFlying() {
        return false;
    }

    public static boolean flyingIsPermitted(String mode, boolean observedFlying) {
        if (!observedFlying) {
            return true;
        }
        return mayFly(mode);
    }

    public static boolean flyingIsMandatory(String mode) {
        return forcedFlying(mode);
    }

    public static boolean collidesWithBlocks(String mode) {
        return !GameModes.isSpectator(mode);
    }

    public static boolean takesFallDamage(String mode) {
        return !invulnerable(mode);
    }

    public static boolean canInteractWithBlocks(String mode) {
        return !GameModes.isSpectator(mode);
    }

    public static boolean modeChangeInvalidatesBaseline(String previousMode, String currentMode) {
        if (previousMode == null || currentMode == null) {
            return true;
        }
        return !previousMode.equals(currentMode);
    }
}
