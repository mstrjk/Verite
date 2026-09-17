package teacommontea.veritechasse.Reality.Checks.Jesus;

import java.util.Locale;

import teacommontea.veritechasse.Reality.Check;
import teacommontea.veritechasse.Reality.CheckContext;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.SinkableSurface;

public final class JesusRest implements Check {

    public static final String KEY = "jesus_rest";

    public static final String TITLE = "Jesus (Rest)";

    public static final String DESCRIPTION =
        "Resting motionless on a sinkable surface. Measured from the server's own"
            + " position each tick, so a client that stops sending movement is still"
            + " caught.";

    public static final int REQUIRED_TICKS = 6;

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
        if (context.horizontal() > JesusGlide.minimumHorizontal(context)) {
            return null;
        }
        if (!context.verticalContradictsEngine()) {
            return null;
        }
        return "stationary on " + context.sinkableSurface() + " holding "
            + format(context.deltaY()) + " where vanilla computes "
            + format(context.expectedVertical());
    }

    @Override
    public String handwritten(CheckContext context) {
        String blocked = blockedReason(context);
        if (blocked != null) {
            context.trace(KEY, "handwritten", blocked);
            context.state().clearStreak(KEY);
            return null;
        }

        double moved = context.current().y() - context.previous().y();
        boolean resting = SinkableSurface.suspensionIsImpossible(
            context.overSinkable(),
            context.current().supported(),
            context.submergedDeep(),
            context.swimmingPose(),
            context.current().deltaY())
            && context.horizontal() <= JesusGlide.minimumHorizontal(context);

        if (!resting) {
            context.state().clearStreak(KEY);
            return null;
        }

        int streak = context.state().advanceStreak(KEY);
        if (streak < REQUIRED_TICKS) {
            return null;
        }

        return "rested on " + context.sinkableSurface() + " for " + streak
            + " ticks, moving " + format(moved) + " vertically";
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
