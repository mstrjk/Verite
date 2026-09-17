package teacommontea.veritechasse.Reality.Checks.Jesus;

import java.util.Locale;

import teacommontea.veritechasse.Reality.Check;
import teacommontea.veritechasse.Reality.CheckContext;
import teacommontea.veritechasse.Reality.PlayerSnapshot;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.LiquidReality;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.SinkableSurface;

public final class Jesus implements Check {

    public static final String KEY = "jesus";

    public static final String TITLE = "Jesus";

    public static final String DESCRIPTION =
        "Horizontal travel through a fluid faster than that fluid permits for the"
            + " player's gear and effects.";

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
        String surface = context.sinkableSurface();
        double observed = context.horizontal();
        double bound = horizontalBound(context, surface);
        if (observed <= bound) {
            context.trace(KEY, "engine", "under bound "
                + format(observed) + " <= " + format(bound));
            return null;
        }
        return "travelled " + format(observed) + " through " + surface
            + ", vanilla permits " + format(bound)
            + " (ratio " + format(ratio(observed, bound)) + ")";
    }

    @Override
    public String handwritten(CheckContext context) {
        String blocked = blockedReason(context);
        if (blocked != null) {
            context.trace(KEY, "handwritten", blocked);
            context.state().clearStreak(KEY);
            return null;
        }

        double steady = steadyBound(context);
        double observed = context.horizontal();
        if (observed <= steady) {
            context.trace(KEY, "handwritten", "settled "
                + format(observed) + " <= " + format(steady));
            context.state().clearStreak(KEY);
            return null;
        }

        int streak = context.state().advanceStreak(KEY);
        int allowed = sustainTicks(context) + SUSTAIN_FORGIVENESS;
        if (streak <= allowed) {
            context.trace(KEY, "handwritten",
                "sustaining " + streak + "/" + allowed
                    + " at " + format(observed) + " over " + format(steady));
            return null;
        }
        return "held " + format(observed) + " through " + context.sinkableSurface()
            + " for " + streak + " ticks, where the fastest legal entry settles to "
            + format(steady) + " within " + allowed;
    }

    public static final int SUSTAIN_FORGIVENESS = 5;

    private static int sustainTicks(CheckContext context) {
        PlayerSnapshot current = context.current();
        if (SinkableSurface.LAVA.equals(context.sinkableSurface())) {
            return LiquidReality.ticksToSettleInLava(
                current.shallowLava(), current.ultraWarm());
        }
        return LiquidReality.ticksToSettleInWater(
            current.effects(),
            current.boots(),
            context.sprintingInFluid(),
            context.derivedGround(),
            context.era());
    }

    private static double steadyBound(CheckContext context) {
        PlayerSnapshot current = context.current();
        if (SinkableSurface.LAVA.equals(context.sinkableSurface())) {
            return LiquidReality.maximumLavaSpeed(
                current.shallowLava(), current.ultraWarm());
        }
        return LiquidReality.maximumWaterSpeedWithCurrent(
            current.effects(),
            current.boots(),
            context.sprintingInFluid(),
            context.derivedGround(),
            context.era());
    }

    public static final int REQUIRED_TICKS = 3;

    private static String blockedReason(CheckContext context) {
        String reason = context.evaluableReason();
        if (reason != null) {
            return reason;
        }
        if (context.current().supported()) {
            return "supported";
        }
        if (context.submergedDeep()) {
            return "submergedDeep";
        }
        if (SinkableSurface.POWDER_SNOW.equals(context.sinkableSurface())) {
            return "powderSnow";
        }
        return null;
    }

    private static double ratio(double observed, double bound) {
        if (bound <= 0.0D) {
            return observed;
        }
        return observed / bound;
    }

    private static double horizontalBound(CheckContext context, String surface) {
        PlayerSnapshot current = context.current();
        if (SinkableSurface.LAVA.equals(surface)) {
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
