package teacommontea.veritechasse.vanilla.RideableArmour.Mounts;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.ControlGate;
import teacommontea.veritechasse.vanilla.RideableArmour.Support.MovementEnvelope;

public final class Horses {

    public static final Set<String> FAMILY = Set.of(
        "donkey",
        "horse",
        "mule",
        "skeleton_horse",
        "zombie_horse");

    public static final Set<String> UNDEAD = Set.of(
        "skeleton_horse",
        "zombie_horse");

    public static final double SPEED_BASE = 0.45D;
    public static final double SPEED_STEP = 0.3D;
    public static final double SPEED_SCALE = 0.25D;

    public static final double JUMP_BASE = 0.4D;
    public static final double JUMP_STEP = 0.2D;

    public static final double MIN_MOVEMENT_SPEED = SPEED_BASE * SPEED_SCALE;
    public static final double MAX_MOVEMENT_SPEED = (SPEED_BASE + 3.0D * SPEED_STEP) * SPEED_SCALE;

    public static final double MIN_JUMP_STRENGTH = JUMP_BASE;
    public static final double MAX_JUMP_STRENGTH = JUMP_BASE + 3.0D * JUMP_STEP;

    public static final double GENERATION_TOLERANCE = 1.0E-6D;

    public static final float BACKWARDS_MOVE_SPEED_FACTOR = 0.25F;
    public static final float SIDEWAYS_MOVE_SPEED_FACTOR = 0.5F;

    public static final boolean STEERABLE = true;
    public static final boolean CAN_JUMP = true;
    public static final boolean CAN_FLY = false;
    public static final boolean CAN_DASH = false;

    public static final MovementEnvelope ENVELOPE =
        new MovementEnvelope(STEERABLE, CAN_JUMP, CAN_FLY, CAN_DASH, 1.0F, BACKWARDS_MOVE_SPEED_FACTOR);

    private Horses() {
    }

    public static boolean contains(String entityName) {
        if (entityName == null) {
            return false;
        }
        return FAMILY.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static boolean isUndead(String entityName) {
        if (entityName == null) {
            return false;
        }
        return UNDEAD.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static ControlGate controlGate() {
        return ControlGate.SADDLE_ONLY;
    }

    public static boolean canBeSaddled(String entityName, boolean alive, boolean baby, boolean tamed) {
        if (!contains(entityName)) {
            return false;
        }
        if (isUndead(entityName)) {
            return true;
        }
        return alive && !baby && tamed;
    }

    public static boolean requiresTaming(String entityName) {
        return contains(entityName) && !isUndead(entityName);
    }

    public static double maxSpeed(double movementSpeedAttribute) {
        return movementSpeedAttribute;
    }

    public static Reality speedFrom(double movementSpeedAttribute) {
        return Reality.of(movementSpeedAttribute);
    }

    public static Reality generatedSpeedCeiling() {
        return Reality.of(MAX_MOVEMENT_SPEED);
    }

    public static Reality generatedJumpCeiling() {
        return Reality.of(MAX_JUMP_STRENGTH);
    }

    public static boolean speedIsGeneratable(double observed) {
        return observed >= MIN_MOVEMENT_SPEED - GENERATION_TOLERANCE
            && observed <= MAX_MOVEMENT_SPEED + GENERATION_TOLERANCE;
    }

    public static boolean jumpIsGeneratable(double observed) {
        return observed >= MIN_JUMP_STRENGTH - GENERATION_TOLERANCE
            && observed <= MAX_JUMP_STRENGTH + GENERATION_TOLERANCE;
    }
}
