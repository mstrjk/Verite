package teacommontea.veritechasse.Vanilla.ControllableEntities.Mounts;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.ControllableEntities.Mounts.Support.ControlGate;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Mounts.Support.MovementEnvelope;
import teacommontea.veritechasse.Vanilla.Reality;

public final class Llamas {

    public static final Set<String> FAMILY = Set.of(
        "llama",
        "trader_llama");

    public static final double MOVEMENT_SPEED_BASE = 0.175D;
    public static final double JUMP_STRENGTH_BASE = 0.5D;

    public static final boolean CAN_BE_SADDLED = false;
    public static final boolean CAN_PERFORM_REARING = false;

    public static final boolean STEERABLE = false;
    public static final boolean CAN_JUMP = false;
    public static final boolean CAN_FLY = false;
    public static final boolean CAN_DASH = false;

    public static final MovementEnvelope ENVELOPE =
        new MovementEnvelope(STEERABLE, CAN_JUMP, CAN_FLY, CAN_DASH, 0.0F, 0.0F);

    private Llamas() {
    }

    public static boolean contains(String entityName) {
        if (entityName == null) {
            return false;
        }
        return FAMILY.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static ControlGate controlGate() {
        return ControlGate.NONE;
    }

    public static boolean grantsControl() {
        return false;
    }

    public static boolean riderCanSteer() {
        return STEERABLE;
    }

    public static boolean acceptsPotionEffects() {
        return true;
    }

    public static Reality steeringInfluence() {
        return Reality.impossible();
    }

    public static Reality riderJumpInfluence() {
        return Reality.impossible();
    }

    public static Reality ownSpeedFrom(double movementSpeedAttribute) {
        return Reality.of(movementSpeedAttribute);
    }
}
