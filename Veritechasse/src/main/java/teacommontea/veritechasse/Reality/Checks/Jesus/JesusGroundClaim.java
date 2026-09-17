package teacommontea.veritechasse.Reality.Checks.Jesus;

import java.util.Locale;

import teacommontea.veritechasse.Reality.Check;
import teacommontea.veritechasse.Reality.CheckContext;
import teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerLiquid.SinkableSurface;
import teacommontea.veritechasse.Vanilla.PlayerMovement.Support.GroundState;

public final class JesusGroundClaim implements Check {

    public static final String KEY = "jesus_ground_claim";

    public static final String TITLE = "Jesus (Ground Claim)";

    public static final String DESCRIPTION =
        "Claiming to stand on the ground while the only thing underfoot is a sinkable"
            + " surface. Vanilla sets the ground flag from a solid collision shape;"
            + " fluids never set it.";

    public static final int REQUIRED_TICKS = 2;

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
        String blocked = context.evaluableReason();
        if (blocked != null) {
            context.trace(KEY, "engine", blocked);
            return null;
        }
        boolean falsified = GroundState.claimIsFalsified(
            context.derivedGround(),
            context.expectedVertical(),
            context.deltaY());
        if (!falsified) {
            return null;
        }
        return "resolved as grounded while vanilla computes deltaY "
            + format(context.expectedVertical()) + " against an observed "
            + format(context.deltaY()) + ", which lands on no block";
    }

    @Override
    public String handwritten(CheckContext context) {
        String blocked = context.evaluableReason();
        if (blocked != null) {
            context.trace(KEY, "handwritten", blocked);
            context.state().clearStreak(KEY);
            return null;
        }

        boolean impossible = SinkableSurface.groundClaimIsImpossible(
            context.derivedGround(),
            context.overSinkable(),
            context.current().supported(),
            context.submergedDeep(),
            context.swimmingPose());

        if (!impossible) {
            context.state().clearStreak(KEY);
            return null;
        }

        int streak = context.state().advanceStreak(KEY);
        if (streak < REQUIRED_TICKS) {
            return null;
        }

        return "behaved as grounded over " + context.sinkableSurface()
            + " with no supporting collision shape for " + streak + " ticks";
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.4f", Double.valueOf(value));
    }
}
