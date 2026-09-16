package teacommontea.veritechasse.Vanilla.ControllableEntities.Minecarts;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.ControllableEntities.Minecarts.Support.MinecartBehaviour;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Minecarts.Support.MinecartSpeed;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Minecarts.Support.Rails;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Minecart {

    public static final String KEY = "minecart";

    public static final Set<String> VARIANTS = Set.of(
        "minecart",
        "chest_minecart",
        "furnace_minecart",
        "hopper_minecart",
        "tnt_minecart",
        "spawner_minecart",
        "command_block_minecart");

    public static final boolean STEERABLE = false;
    public static final boolean CAN_JUMP = false;
    public static final boolean CAN_FLY = false;
    public static final boolean ACCEPTS_POTION_EFFECTS = false;
    public static final boolean REQUIRES_RAIL_TO_ACCELERATE = true;

    public static final double NATURAL_SLOWDOWN = 0.98D;

    public static final double RIDER_NUDGE_PER_TICK = 0.001D;
    public static final double RIDER_NUDGE_MAXIMUM_SPEED_SQR = 0.01D;

    private Minecart() {
    }

    public static boolean is(String entityName) {
        if (entityName == null) {
            return false;
        }
        return VARIANTS.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static double maxSpeed(MinecartBehaviour behaviour, boolean inWater, int maxMinecartSpeedRule) {
        return MinecartSpeed.maxSpeed(behaviour, inWater, maxMinecartSpeedRule);
    }

    public static double maxSpeed(MinecartBehaviour behaviour, boolean inWater) {
        return MinecartSpeed.maxSpeed(behaviour, inWater);
    }

    public static boolean speedIsPossible(
            double observedSpeed,
            MinecartBehaviour behaviour,
            boolean inWater,
            int maxMinecartSpeedRule) {
        return observedSpeed <= maxSpeed(behaviour, inWater, maxMinecartSpeedRule);
    }

    public static boolean accelerationRequiresRail(String blockName, boolean powered, double horizontalSpeed) {
        return Rails.accelerates(blockName, powered, horizontalSpeed);
    }

    public static boolean riderCanSteer() {
        return STEERABLE;
    }

    public static boolean riderCanNudge(double horizontalSpeedSqr) {
        return horizontalSpeedSqr < RIDER_NUDGE_MAXIMUM_SPEED_SQR;
    }

    public static double nudgeImpulse(double horizontalSpeedSqr, double moveIntentComponent) {
        if (!riderCanNudge(horizontalSpeedSqr)) {
            return 0.0D;
        }
        return moveIntentComponent * RIDER_NUDGE_PER_TICK;
    }

    public static boolean nudgeCancelsBraking(double horizontalSpeedSqr, boolean hasMoveIntent) {
        return hasMoveIntent && riderCanNudge(horizontalSpeedSqr);
    }

    public static boolean acceptsPotionEffects() {
        return ACCEPTS_POTION_EFFECTS;
    }

    public static Reality speedFrom(MinecartBehaviour behaviour, boolean inWater, int maxMinecartSpeedRule) {
        return Reality.of(maxSpeed(behaviour, inWater, maxMinecartSpeedRule));
    }

    public static Reality steeringInfluence(double horizontalSpeedSqr) {
        if (!riderCanNudge(horizontalSpeedSqr)) {
            return Reality.impossible();
        }
        return Reality.of(RIDER_NUDGE_PER_TICK);
    }
}
