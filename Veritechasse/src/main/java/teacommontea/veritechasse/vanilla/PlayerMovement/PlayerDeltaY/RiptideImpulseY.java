package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Enchantments.Riptide;
import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Reality;

public final class RiptideImpulseY {

    public static final double GROUND_LAUNCH_LIFT = Riptide.GROUND_LAUNCH_LIFT;

    public static final boolean GROUND_LIFT_IS_DISPLACEMENT = true;
    public static final boolean IS_PUSH_NOT_SET = true;

    private RiptideImpulseY() {
    }

    public static double rawComponent(float pitchDegrees) {
        return -Math.sin(Math.toRadians(pitchDegrees));
    }

    public static double vectorLength(float yawDegrees, float pitchDegrees) {
        double yaw = Math.toRadians(yawDegrees);
        double pitch = Math.toRadians(pitchDegrees);
        double xd = -Math.sin(yaw) * Math.cos(pitch);
        double yd = -Math.sin(pitch);
        double zd = Math.cos(yaw) * Math.cos(pitch);
        return Math.sqrt(xd * xd + yd * yd + zd * zd);
    }

    public static double verticalImpulse(
            int riptideLevel,
            float yawDegrees,
            float pitchDegrees,
            Era era) {
        if (riptideLevel <= 0) {
            return 0.0D;
        }
        double length = vectorLength(yawDegrees, pitchDegrees);
        if (length <= 0.0D) {
            return 0.0D;
        }
        double strength = Riptide.spinAttackStrength(riptideLevel, era);
        return rawComponent(pitchDegrees) * strength / length;
    }

    public static double verticalImpulse(
            ItemStack trident,
            float yawDegrees,
            float pitchDegrees,
            Era era) {
        if (trident == null) {
            return 0.0D;
        }
        return verticalImpulse(Riptide.levelOn(trident), yawDegrees, pitchDegrees, era);
    }

    public static double maximumVerticalImpulse(int riptideLevel, Era era) {
        if (riptideLevel <= 0) {
            return 0.0D;
        }
        return Riptide.spinAttackStrength(riptideLevel, era);
    }

    public static double groundLaunchDisplacement(boolean onGround) {
        return onGround ? GROUND_LAUNCH_LIFT : 0.0D;
    }

    public static double verticalAfter(
            double currentDeltaY,
            int riptideLevel,
            float yawDegrees,
            float pitchDegrees,
            Era era) {
        return currentDeltaY + verticalImpulse(riptideLevel, yawDegrees, pitchDegrees, era);
    }

    public static Reality impulseFrom(int riptideLevel, Era era) {
        return Reality.of(maximumVerticalImpulse(riptideLevel, era));
    }
}
