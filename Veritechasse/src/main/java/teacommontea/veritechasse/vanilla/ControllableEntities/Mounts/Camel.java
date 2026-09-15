package teacommontea.veritechasse.vanilla.ControllableEntities.Mounts;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support.ControlGate;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support.MovementEnvelope;

public final class Camel {

    public static final String KEY = "camel";
    public static final String HUSK_KEY = "camel_husk";

    public static final Set<String> MEMBERS = Set.of(KEY, HUSK_KEY);

    public static final int HUSK_PROTOCOL_MAJOR = 1;
    public static final int HUSK_PROTOCOL_MINOR = 21;
    public static final int HUSK_PROTOCOL_PATCH = 11;

    public static final double MOVEMENT_SPEED_BASE = 0.09D;
    public static final double JUMP_STRENGTH_BASE = 0.42D;
    public static final float SPRINT_BONUS = 0.1F;

    public static final int DASH_COOLDOWN_TICKS = 55;
    public static final float DASH_VERTICAL_MOMENTUM = 1.4285F;
    public static final float DASH_HORIZONTAL_MOMENTUM = 22.2222F;
    public static final int DASH_MINIMUM_DURATION_TICKS = 5;

    public static final boolean STEERABLE = true;
    public static final boolean CAN_JUMP = true;
    public static final boolean CAN_FLY = false;
    public static final boolean CAN_DASH = true;
    public static final boolean ACCEPTS_POTION_EFFECTS = true;

    public static final MovementEnvelope ENVELOPE =
        new MovementEnvelope(STEERABLE, CAN_JUMP, CAN_FLY, CAN_DASH, 1.0F, Horses.BACKWARDS_MOVE_SPEED_FACTOR);

    private Camel() {
    }

    public static boolean contains(String entityName) {
        return entityName != null
            && MEMBERS.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static boolean isHusk(String entityName) {
        return entityName != null
            && HUSK_KEY.equals(entityName.toLowerCase(Locale.ROOT));
    }

    public static boolean huskExists(Protocol protocol) {
        return protocol.atLeast(HUSK_PROTOCOL_MAJOR, HUSK_PROTOCOL_MINOR, HUSK_PROTOCOL_PATCH);
    }

    public static boolean canBeABaby(String entityName) {
        return !isHusk(entityName);
    }

    public static ControlGate controlGate() {
        return ControlGate.SADDLE_ONLY;
    }

    public static boolean canBeSaddled(boolean alive, boolean baby, boolean tamed) {
        return alive && !baby && tamed;
    }

    public static double riddenSpeed(double movementSpeedAttribute, boolean sprinting, int jumpCooldown) {
        double bonus = sprinting && jumpCooldown == 0 ? SPRINT_BONUS : 0.0D;
        return movementSpeedAttribute + bonus;
    }

    public static boolean refusesToMove(boolean sitting) {
        return sitting;
    }

    public static boolean canDashNow(boolean saddled, int dashCooldown, boolean onGround) {
        return saddled && dashCooldown <= 0 && onGround;
    }

    public static double dashHorizontalImpulse(float jumpScale, double movementSpeedAttribute, double blockSpeedFactor) {
        return DASH_HORIZONTAL_MOMENTUM * jumpScale * movementSpeedAttribute * blockSpeedFactor;
    }

    public static Reality speedFrom(double movementSpeedAttribute, boolean sprinting, int jumpCooldown) {
        return Reality.of(riddenSpeed(movementSpeedAttribute, sprinting, jumpCooldown));
    }

    public static Reality dashFrom(float jumpScale, double movementSpeedAttribute, double blockSpeedFactor) {
        return Reality.of(dashHorizontalImpulse(jumpScale, movementSpeedAttribute, blockSpeedFactor));
    }

    public static boolean acceptsPotionEffects() {
        return ACCEPTS_POTION_EFFECTS;
    }
}
