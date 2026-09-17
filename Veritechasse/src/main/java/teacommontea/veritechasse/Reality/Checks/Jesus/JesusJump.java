package teacommontea.veritechasse.Reality.Checks.Jesus;

import java.util.Locale;

import teacommontea.veritechasse.Reality.Check;
import teacommontea.veritechasse.Reality.CheckContext;
import teacommontea.veritechasse.Reality.PlayerState;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerJump.JumpGate;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.SinkableSurface;

public final class JesusJump implements Check {

    public static final String KEY = "jesus_jump";

    public static final String TITLE = "Jesus (Jump)";

    public static final String DESCRIPTION =
        "Beginning to rise from an unsupported sinkable surface. A jump can only start"
            + " from the ground, and a player over a fluid is never on the ground.";

    public static final String WAS_OVER_FLAG = "jesus_jump_was_over";

    public static final String WAS_RISING_FLAG = "jesus_jump_was_rising";

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
        if (!context.overSinkableUnsupported()) {
            return null;
        }
        double excess = context.verticalExcess();
        if (excess <= JumpGate.LIQUID_JUMP_IMPULSE) {
            return null;
        }
        return "rose " + format(context.deltaY()) + " where vanilla computes "
            + format(context.expectedVertical()) + ", an excess of " + format(excess)
            + " beyond the " + format(JumpGate.LIQUID_JUMP_IMPULSE)
            + " a fluid jump may add";
    }

    @Override
    public String handwritten(CheckContext context) {
        PlayerState state = context.state();

        String blocked = blockedReason(context);
        if (blocked == null && context.touchingFluid()) {
            blocked = "touchingFluid";
        }
        if (blocked != null) {
            context.trace(KEY, "handwritten", blocked);
            state.setFlag(WAS_OVER_FLAG, false);
            state.setFlag(WAS_RISING_FLAG, false);
            return null;
        }

        double deltaY = context.deltaY();
        boolean rising = deltaY > JumpGate.LIQUID_JUMP_IMPULSE;
        boolean overUnsupported = context.overSinkableUnsupported();

        boolean jumped = SinkableSurface.risingFromSurfaceIsImpossible(
            state.flag(WAS_OVER_FLAG),
            state.flag(WAS_RISING_FLAG),
            rising,
            context.touchingFluid());

        state.setFlag(WAS_RISING_FLAG, rising);
        if (overUnsupported) {
            state.setFlag(WAS_OVER_FLAG, true);
        } else if (!rising) {
            state.setFlag(WAS_OVER_FLAG, false);
        }

        if (!jumped) {
            return null;
        }

        return "began rising at " + format(deltaY)
            + " from an unsupported surface with no ground to jump from";
    }

    private static String blockedReason(CheckContext context) {
        String reason = context.evaluableReason();
        if (reason != null) {
            return reason;
        }
        if (context.inBubbleColumn()) {
            return "bubbleColumn";
        }
        if (context.swimmingPose()) {
            return "swimmingPose";
        }
        if (context.submergedDeep()) {
            return "submergedDeep";
        }
        return null;
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.4f", Double.valueOf(value));
    }
}
