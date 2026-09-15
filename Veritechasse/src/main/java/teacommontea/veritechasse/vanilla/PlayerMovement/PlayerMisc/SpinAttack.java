package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerMisc;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Riptide;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY.RiptideImpulseY;
import teacommontea.veritechasse.vanilla.PlayerMovement.PlayerXZ.RiptideImpulseXZ;
import teacommontea.veritechasse.vanilla.Reality;

public final class SpinAttack {

    public static final int DURATION_TICKS = 20;

    public static final double TOUCH_REBOUND = -0.2D;

    public static final boolean COLLISION_ENDS_IT = true;

    public static final double GROUND_LAUNCH_LIFT = Riptide.GROUND_LAUNCH_LIFT;
    public static final boolean GROUND_LIFT_IS_DISPLACEMENT = true;

    private SpinAttack() {
    }

    public static boolean canLaunch(ItemStack trident, boolean inWaterOrRain, boolean passenger) {
        return RiptideImpulseXZ.canLaunch(trident, inWaterOrRain, passenger);
    }

    public static double verticalComponent(float pitchDegrees) {
        return RiptideImpulseY.rawComponent(pitchDegrees);
    }

    public static double verticalImpulse(
            ItemStack trident,
            float yawDegrees,
            float pitchDegrees,
            Era era) {
        return RiptideImpulseY.verticalImpulse(trident, yawDegrees, pitchDegrees, era);
    }

    public static double verticalImpulse(
            int riptideLevel,
            float yawDegrees,
            float pitchDegrees,
            Era era) {
        return RiptideImpulseY.verticalImpulse(riptideLevel, yawDegrees, pitchDegrees, era);
    }

    public static double groundLaunchDisplacement(boolean onGround) {
        return onGround ? GROUND_LAUNCH_LIFT : 0.0D;
    }

    public static double afterTouchingEntity(double component) {
        return component * TOUCH_REBOUND;
    }

    public static boolean endsEarly(boolean touchedEntity, boolean horizontalCollision) {
        return touchedEntity || horizontalCollision;
    }

    public static int remainingTicks(int elapsed) {
        int left = DURATION_TICKS - elapsed;
        return left < 0 ? 0 : left;
    }

    public static boolean active(int autoSpinAttackTicks) {
        return autoSpinAttackTicks > 0;
    }

    public static Reality verticalFrom(
            ItemStack trident,
            float yawDegrees,
            float pitchDegrees,
            Era era) {
        return Reality.of(Math.abs(verticalImpulse(trident, yawDegrees, pitchDegrees, era)));
    }
}
