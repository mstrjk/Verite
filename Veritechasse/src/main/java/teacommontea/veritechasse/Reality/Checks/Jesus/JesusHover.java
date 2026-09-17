package teacommontea.veritechasse.Reality.Checks.Jesus;

import java.util.Locale;

import teacommontea.veritechasse.Reality.Check;
import teacommontea.veritechasse.Reality.CheckContext;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.SinkableSurface;

public final class JesusHover implements Check {

    public static final String KEY = "jesus_hover";

    public static final String TITLE = "Jesus (Hover)";

    public static final String DESCRIPTION =
        "Holding position at the surface of a sinkable block. Vanilla buoyancy has no"
            + " resting state: an idle player in fluid always sinks, and above it"
            + " gravity always pulls.";

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
        return "held " + format(context.deltaY()) + " where vanilla computes "
            + format(context.expectedVertical()) + " on " + context.sinkableSurface()
            + " (excess " + format(context.verticalExcess()) + ")";
    }

    @Override
    public String handwritten(CheckContext context) {
        String blocked = blockedReason(context);
        if (blocked != null) {
            context.trace(KEY, "handwritten", blocked);
            context.state().clearStreak(KEY);
            return null;
        }

        boolean hovering = SinkableSurface.suspensionIsImpossible(
            context.overSinkable(),
            context.current().supported(),
            context.submergedDeep(),
            context.swimmingPose(),
            context.current().deltaY());

        if (!hovering) {
            context.state().clearStreak(KEY);
            return null;
        }

        int streak = context.state().advanceStreak(KEY);
        if (streak < REQUIRED_TICKS) {
            return null;
        }

        return "held deltaY " + format(context.deltaY()) + " on "
            + context.sinkableSurface() + " for " + streak
            + " ticks without sinking";
    }

    private static String blockedReason(CheckContext context) {
        String reason = context.evaluableReason();
        if (reason != null) {
            return reason;
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

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.4f", Double.valueOf(value));
    }
}
