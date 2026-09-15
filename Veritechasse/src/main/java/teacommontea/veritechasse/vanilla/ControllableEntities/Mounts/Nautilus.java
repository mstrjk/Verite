package teacommontea.veritechasse.vanilla.ControllableEntities.Mounts;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support.ControlGate;
import teacommontea.veritechasse.vanilla.ControllableEntities.Mounts.Support.MovementEnvelope;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class Nautilus {

    public static final String KEY = "nautilus";
    public static final String ZOMBIE_KEY = "zombie_nautilus";

    public static final Set<String> MEMBERS = Set.of(KEY, ZOMBIE_KEY);

    public static final int PROTOCOL_MAJOR = 1;
    public static final int PROTOCOL_MINOR = 21;
    public static final int PROTOCOL_PATCH = 11;

    public static final double MOVEMENT_SPEED_BASE = 1.0D;
    public static final double MAX_HEALTH = 15.0D;
    public static final double ATTACK_DAMAGE = 3.0D;
    public static final double KNOCKBACK_RESISTANCE = 0.3D;

    public static final float WATER_SPEED_FACTOR = 0.0325F;
    public static final float LAND_SPEED_FACTOR = 0.02F;

    public static final float WATER_DRAG = 0.9F;

    public static final float DASH_MOMENTUM_IN_WATER = 1.2F;
    public static final float DASH_MOMENTUM_ON_LAND = 0.5F;
    public static final int DASH_COOLDOWN_TICKS = 40;
    public static final int DASH_MINIMUM_DURATION_TICKS = 5;

    public static final float BACKWARD_MULTIPLIER = 0.5F;

    public static final float RIDDEN_PITCH_SCALE = 0.5F;
    public static final float TURN_RATE = 0.5F;

    public static final boolean STEERABLE = true;
    public static final boolean CAN_JUMP = true;
    public static final boolean CAN_FLY = false;
    public static final boolean CAN_DASH = true;
    public static final boolean ACCEPTS_POTION_EFFECTS = true;

    public static final boolean REQUIRES_ADULT = true;
    public static final boolean REQUIRES_TAMED = true;

    public static final MovementEnvelope ENVELOPE = new MovementEnvelope(
        STEERABLE, CAN_JUMP, CAN_FLY, CAN_DASH, WATER_SPEED_FACTOR, BACKWARD_MULTIPLIER);

    private Nautilus() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(PROTOCOL_MAJOR, PROTOCOL_MINOR, PROTOCOL_PATCH);
    }

    public static boolean contains(String entityName) {
        return entityName != null
            && MEMBERS.contains(entityName.toLowerCase(Locale.ROOT));
    }

    public static ControlGate controlGate() {
        return ControlGate.SADDLE_ONLY;
    }

    public static boolean canBeSaddled(boolean alive, boolean baby, boolean tamed) {
        return alive && !baby && tamed;
    }

    public static float speedFactor(boolean inWater) {
        return inWater ? WATER_SPEED_FACTOR : LAND_SPEED_FACTOR;
    }

    public static double acceleration(double movementSpeedAttribute, boolean inWater) {
        return movementSpeedAttribute * (double) speedFactor(inWater);
    }

    public static double acceleration(boolean inWater) {
        return acceleration(MOVEMENT_SPEED_BASE, inWater);
    }

    public static double terminalSpeed(double movementSpeedAttribute, boolean inWater) {
        double drag = (double) WATER_DRAG;
        if (drag >= 1.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return acceleration(movementSpeedAttribute, inWater) * drag / (1.0D - drag);
    }

    public static double terminalSpeed(boolean inWater) {
        return terminalSpeed(MOVEMENT_SPEED_BASE, inWater);
    }

    public static double horizontalAfterTick(
            double currentHorizontal,
            double movementSpeedAttribute,
            boolean inWater) {
        return (currentHorizontal + acceleration(movementSpeedAttribute, inWater))
            * (double) WATER_DRAG;
    }

    public static double dashImpulse(
            double movementSpeedAttribute,
            float jumpScale,
            boolean inWater,
            double blockSpeedFactor) {
        float momentum = inWater ? DASH_MOMENTUM_IN_WATER : DASH_MOMENTUM_ON_LAND;
        return (double) (momentum * jumpScale)
            * movementSpeedAttribute
            * blockSpeedFactor;
    }

    public static double maximumDashImpulse(boolean inWater, double blockSpeedFactor) {
        return dashImpulse(MOVEMENT_SPEED_BASE, 1.0F, inWater, blockSpeedFactor);
    }

    public static boolean dashIsReady(int dashCooldown) {
        return dashCooldown <= 0;
    }

    public static boolean dashTooSoon(int ticksSinceLastDash) {
        return ticksSinceLastDash < DASH_COOLDOWN_TICKS;
    }

    public static boolean acceptsPotionEffects() {
        return ACCEPTS_POTION_EFFECTS;
    }

    public static Reality speedFrom(double movementSpeedAttribute, boolean inWater) {
        return Reality.of(terminalSpeed(movementSpeedAttribute, inWater));
    }

    public static Reality dashFrom(boolean inWater, double blockSpeedFactor) {
        return Reality.of(maximumDashImpulse(inWater, blockSpeedFactor));
    }
}
