package teacommontea.veritechasse.Reality.Checks.Jesus;

import java.util.Locale;

import teacommontea.veritechasse.Reality.Check;
import teacommontea.veritechasse.Reality.CheckContext;
import teacommontea.veritechasse.Reality.PlayerSnapshot;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.LiquidReality;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.SinkableSurface;

public final class JesusGlide implements Check {

    public static final String KEY = "jesus_glide";

    public static final String TITLE = "Jesus (Glide)";

    public static final String DESCRIPTION =
        "Travelling horizontally across the top of a sinkable surface while vertical"
            + " motion stays pinned. A real player at the surface is always bobbing or"
            + " sinking, never flat.";

    public static final int REQUIRED_TICKS = 3;

    @Override
    public String key() {
        return KEY;
    }

    @Override
    public String title() {
        return TITLE;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public String engine(CheckContext context) {
        String blocked = blockedReason(context);
        if (blocked != null) {
            context.trace(KEY, "engine", blocked);
            return null;
        }
        if (!context.verticalContradictsEngine()) {
            return null;
        }
        double horizontal = context.horizontal();
        double bound = horizontalBound(context);
        if (horizontal <= bound) {
            return null;
        }
        return "carried " + format(horizontal) + " horizontally past the "
            + format(bound) + " bound while holding " + format(context.deltaY())
            + " against an expected " + format(context.expectedVertical());
    }

    @Override
    public String handwritten(CheckContext context) {
        String blocked = blockedReason(context);
        if (blocked != null) {
            context.trace(KEY, "handwritten", blocked);
            context.state().clearStreak(KEY);
            return null;
        }

        double horizontal = context.horizontal();
        boolean gliding = SinkableSurface.suspensionIsImpossible(
            context.overSinkable(),
            context.current().supported(),
            context.submergedDeep(),
            context.swimmingPose(),
            context.current().deltaY())
            && horizontal > minimumHorizontal(context);

        if (!gliding) {
            context.state().clearStreak(KEY);
            return null;
        }

        int streak = context.state().advanceStreak(KEY);
        if (streak < REQUIRED_TICKS) {
            return null;
        }

        return "glided " + format(horizontal) + " across "
            + context.sinkableSurface() + " with deltaY " + format(context.deltaY())
            + " held flat for " + streak + " ticks";
    }

    private static String blockedReason(CheckContext context) {
        String reason = context.evaluableReason();
        if (reason != null) {
            return reason;
        }
        if (context.current().supported()) {
            return "supported";
        }
        if (context.inBubbleColumn()) {
            return "bubbleColumn";
        }
        if (context.submergedDeep()) {
            return "submergedDeep";
        }
        if (context.swimmingPose()) {
            return "swimmingPose";
        }
        return null;
    }

    static double minimumHorizontal(CheckContext context) {
        return horizontalBound(context) * MOVING_FRACTION;
    }

    public static final double MOVING_FRACTION = 0.5D;

    private static double horizontalBound(CheckContext context) {
        PlayerSnapshot current = context.current();
        if (SinkableSurface.LAVA.equals(context.sinkableSurface())) {
            return LiquidReality.maximumLavaSpeed(
                context.previous().observedHorizontal(),
                current.shallowLava(),
                current.ultraWarm());
        }
        return LiquidReality.maximumWaterSpeedWithCurrent(
            context.previous().observedHorizontal(),
            current.effects(),
            current.boots(),
            context.sprintingInFluid(),
            context.derivedGround(),
            context.era());
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.4f", Double.valueOf(value));
    }
}
