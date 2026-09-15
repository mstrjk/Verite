package teacommontea.veritechasse.vanilla.PlayerMovement.PlayerDeltaY;

import teacommontea.veritechasse.vanilla.ControllableEntities.Support.BlockEjection;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.BubbleColumn;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.ExplosionKnockback;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.Knockback;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.PistonPush;
import teacommontea.veritechasse.vanilla.ControllableEntities.Support.StuckInBlock;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Reality;

public final class ExternalY {

    public static final double HONEY_SLIDE_TARGET = -0.05D;
    public static final double HONEY_SLIDE_THRESHOLD = -0.13D;
    public static final double HONEY_HORIZONTAL_NUMERATOR = -0.05D;

    private ExternalY() {
    }

    public static double stuckMultiplier(String blockHere, ActiveEffects effects) {
        return StuckInBlock.verticalMultiplier(blockHere, effects);
    }

    public static double afterStuck(double deltaY, String blockHere, ActiveEffects effects) {
        return deltaY * stuckMultiplier(blockHere, effects);
    }

    public static boolean acceleratesDescent(String blockHere, ActiveEffects effects) {
        return stuckMultiplier(blockHere, effects) > 1.0D;
    }

    public static double oldDeltaY(double deltaY) {
        return deltaY / (double) VerticalDrag.BASE_VERTICAL_AIR_DRAG + Gravity.DEFAULT_BASE_GRAVITY;
    }

    public static double newDeltaY(double deltaY) {
        return (deltaY - Gravity.DEFAULT_BASE_GRAVITY) * (double) VerticalDrag.BASE_VERTICAL_AIR_DRAG;
    }

    public static boolean honeySlideApplies(double deltaY) {
        return oldDeltaY(deltaY) < HONEY_SLIDE_THRESHOLD;
    }

    public static double honeySlideVertical() {
        return newDeltaY(HONEY_SLIDE_TARGET);
    }

    public static double honeySlideHorizontalFactor(double deltaY) {
        double old = oldDeltaY(deltaY);
        if (old >= HONEY_SLIDE_THRESHOLD) {
            return 1.0D;
        }
        return HONEY_HORIZONTAL_NUMERATOR / old;
    }

    public static boolean honeyResetsFallDistance() {
        return true;
    }

    public static double explosion(String source, double distanceToCentre, float exposure) {
        if (source == null) {
            return 0.0D;
        }
        return ExplosionKnockback.knockbackFor(source, distanceToCentre, exposure);
    }

    public static double bubbleColumn(double deltaY, boolean above, boolean drags) {
        return above
            ? BubbleColumn.verticalAfterAbove(deltaY, drags)
            : BubbleColumn.verticalAfterInside(deltaY, drags);
    }

    public static double knockback(double deltaY, double power, boolean onGround) {
        return Knockback.verticalAfter(deltaY, power, onGround);
    }

    public static double piston() {
        return PistonPush.maximumDisplacementPerTick();
    }

    public static double ejection() {
        return BlockEjection.maximumHorizontalImpulse();
    }

    public static double additiveImpulse(
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean pistonAdjacent,
            boolean insideSolidBlock) {
        double total = explosion(explosionSource, explosionDistance, explosionExposure);
        if (pistonAdjacent) {
            total = total + piston();
        }
        if (insideSolidBlock) {
            total = total + ejection();
        }
        return total;
    }

    public static Reality additiveFrom(
            String explosionSource,
            double explosionDistance,
            float explosionExposure,
            boolean pistonAdjacent,
            boolean insideSolidBlock) {
        return Reality.of(additiveImpulse(
            explosionSource, explosionDistance, explosionExposure,
            pistonAdjacent, insideSolidBlock));
    }
}
