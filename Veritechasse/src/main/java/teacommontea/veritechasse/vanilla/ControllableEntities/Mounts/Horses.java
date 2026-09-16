package teacommontea.veritechasse.Vanilla.ControllableEntities.Mounts;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Mounts.Support.ControlGate;
import teacommontea.veritechasse.Vanilla.ControllableEntities.Mounts.Support.MovementEnvelope;

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

    public static final Set<String> CHESTED = Set.of(
        "donkey",
        "mule");

    public static final double CHESTED_MOVEMENT_SPEED = 0.175D;
    public static final double CHESTED_JUMP_STRENGTH = 0.5D;

    public static final String HORSE = "horse";
    public static final String ZOMBIE_HORSE = "zombie_horse";
    public static final String SKELETON_HORSE = "skeleton_horse";

    public static final double ZOMBIE_JUMP_BASE = 0.5D;
    public static final double ZOMBIE_JUMP_STEP = 0.06666666666666667D;
    public static final double ZOMBIE_SPEED_BASE = 9.0D;
    public static final double ZOMBIE_SPEED_STEP = 1.0D;
    public static final float ZOMBIE_SPEED_DIVISOR = 42.16F;

    public static final double MIN_ZOMBIE_JUMP_STRENGTH = ZOMBIE_JUMP_BASE;
    public static final double MAX_ZOMBIE_JUMP_STRENGTH =
        ZOMBIE_JUMP_BASE + 3.0D * ZOMBIE_JUMP_STEP;

    public static final double MIN_ZOMBIE_MOVEMENT_SPEED =
        ZOMBIE_SPEED_BASE / (double) ZOMBIE_SPEED_DIVISOR;
    public static final double MAX_ZOMBIE_MOVEMENT_SPEED =
        (ZOMBIE_SPEED_BASE + 3.0D * ZOMBIE_SPEED_STEP) / (double) ZOMBIE_SPEED_DIVISOR;

    public static final double SPEED_BASE = 0.45D;
    public static final double SPEED_STEP = 0.3D;
    public static final double SPEED_SCALE = 0.25D;

    public static final double JUMP_BASE = 0.4D;
    public static final double JUMP_STEP = 0.2D;

    public static final double MIN_MOVEMENT_SPEED = SPEED_BASE * SPEED_SCALE;
    public static final double MAX_MOVEMENT_SPEED = (SPEED_BASE + 3.0D * SPEED_STEP) * SPEED_SCALE;

    public static final double MIN_JUMP_STRENGTH = JUMP_BASE;
    public static final double MAX_JUMP_STRENGTH = JUMP_BASE + 3.0D * JUMP_STEP;

    public static final double HEALTH_BASE = 15.0D;
    public static final int HEALTH_FIRST_BOUND = 8;
    public static final int HEALTH_SECOND_BOUND = 9;

    public static final double MIN_MAX_HEALTH = HEALTH_BASE;
    public static final double MAX_MAX_HEALTH =
        HEALTH_BASE + (double) (HEALTH_FIRST_BOUND - 1) + (double) (HEALTH_SECOND_BOUND - 1);

    public static final double ZOMBIE_HORSE_MAX_HEALTH = 25.0D;
    public static final double SKELETON_HORSE_MAX_HEALTH = 15.0D;
    public static final double SKELETON_HORSE_MOVEMENT_SPEED = 0.2D;

    public static final double BASE_JUMP_STRENGTH_ATTRIBUTE = 0.7D;
    public static final double BASE_MAX_HEALTH_ATTRIBUTE = 53.0D;
    public static final double BASE_MOVEMENT_SPEED_ATTRIBUTE = 0.225D;
    public static final double STEP_HEIGHT = 1.0D;
    public static final double SAFE_FALL_DISTANCE = 6.0D;
    public static final double FALL_DAMAGE_MULTIPLIER = 0.5D;

    public static final double GENERATION_TOLERANCE = 1.0E-6D;

    public static final float BACKWARDS_MOVE_SPEED_FACTOR = 0.25F;
    public static final float SIDEWAYS_MOVE_SPEED_FACTOR = 0.5F;

    public static final boolean STEERABLE = true;
    public static final boolean CAN_JUMP = true;
    public static final boolean CAN_FLY = false;
    public static final boolean CAN_DASH = false;
    public static final boolean ACCEPTS_POTION_EFFECTS = true;

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

    public static Reality generatedSpeedCeiling(String entityName) {
        return Reality.of(maximumMovementSpeed(entityName));
    }

    public static Reality generatedJumpCeiling(String entityName) {
        return Reality.of(maximumJumpStrength(entityName));
    }

    public static boolean isChested(String entityName) {
        if (entityName == null) {
            return false;
        }
        return CHESTED.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static boolean healthIsRandomised(String entityName) {
        return contains(entityName) && !isUndead(entityName);
    }

    public static boolean speedIsRandomised(String entityName) {
        if (!contains(entityName)) {
            return false;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        return HORSE.equals(name) || ZOMBIE_HORSE.equals(name);
    }

    public static boolean jumpIsRandomised(String entityName) {
        if (!contains(entityName)) {
            return false;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        return HORSE.equals(name) || ZOMBIE_HORSE.equals(name) || SKELETON_HORSE.equals(name);
    }

    public static double minimumMovementSpeed(String entityName) {
        if (!contains(entityName)) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (isChested(name)) {
            return CHESTED_MOVEMENT_SPEED;
        }
        if (SKELETON_HORSE.equals(name)) {
            return SKELETON_HORSE_MOVEMENT_SPEED;
        }
        if (ZOMBIE_HORSE.equals(name)) {
            return MIN_ZOMBIE_MOVEMENT_SPEED;
        }
        return MIN_MOVEMENT_SPEED;
    }

    public static double maximumMovementSpeed(String entityName) {
        if (!contains(entityName)) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (isChested(name)) {
            return CHESTED_MOVEMENT_SPEED;
        }
        if (SKELETON_HORSE.equals(name)) {
            return SKELETON_HORSE_MOVEMENT_SPEED;
        }
        if (ZOMBIE_HORSE.equals(name)) {
            return MAX_ZOMBIE_MOVEMENT_SPEED;
        }
        return MAX_MOVEMENT_SPEED;
    }

    public static double minimumJumpStrength(String entityName) {
        if (!contains(entityName)) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (isChested(name)) {
            return CHESTED_JUMP_STRENGTH;
        }
        if (ZOMBIE_HORSE.equals(name)) {
            return MIN_ZOMBIE_JUMP_STRENGTH;
        }
        return MIN_JUMP_STRENGTH;
    }

    public static double maximumJumpStrength(String entityName) {
        if (!contains(entityName)) {
            return 0.0D;
        }
        String name = entityName.toLowerCase(Locale.ROOT);
        if (isChested(name)) {
            return CHESTED_JUMP_STRENGTH;
        }
        if (ZOMBIE_HORSE.equals(name)) {
            return MAX_ZOMBIE_JUMP_STRENGTH;
        }
        return MAX_JUMP_STRENGTH;
    }

    public static boolean speedIsGeneratable(String entityName, double observed) {
        return observed >= minimumMovementSpeed(entityName) - GENERATION_TOLERANCE
            && observed <= maximumMovementSpeed(entityName) + GENERATION_TOLERANCE;
    }

    public static boolean jumpIsGeneratable(String entityName, double observed) {
        return observed >= minimumJumpStrength(entityName) - GENERATION_TOLERANCE
            && observed <= maximumJumpStrength(entityName) + GENERATION_TOLERANCE;
    }

    public static double minimumMaxHealth(String entityName) {
        if (!contains(entityName)) {
            return 0.0D;
        }
        if ("zombie_horse".equals(entityName.toLowerCase(Locale.ROOT))) {
            return ZOMBIE_HORSE_MAX_HEALTH;
        }
        if ("skeleton_horse".equals(entityName.toLowerCase(Locale.ROOT))) {
            return SKELETON_HORSE_MAX_HEALTH;
        }
        return MIN_MAX_HEALTH;
    }

    public static double maximumMaxHealth(String entityName) {
        if (!contains(entityName)) {
            return 0.0D;
        }
        if ("zombie_horse".equals(entityName.toLowerCase(Locale.ROOT))) {
            return ZOMBIE_HORSE_MAX_HEALTH;
        }
        if ("skeleton_horse".equals(entityName.toLowerCase(Locale.ROOT))) {
            return SKELETON_HORSE_MAX_HEALTH;
        }
        return MAX_MAX_HEALTH;
    }

    public static boolean healthIsGeneratable(String entityName, double observed) {
        return observed >= minimumMaxHealth(entityName) - GENERATION_TOLERANCE
            && observed <= maximumMaxHealth(entityName) + GENERATION_TOLERANCE;
    }

    public static Reality generatedHealthCeiling(String entityName) {
        return Reality.of(maximumMaxHealth(entityName));
    }

    public static boolean acceptsPotionEffects() {
        return ACCEPTS_POTION_EFFECTS;
    }
}
