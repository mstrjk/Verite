package teacommontea.veritechasse.vanilla.Tools.Spears.Support;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.PlayerBase;

public final class Spear {

    public static final int CONTACT_COOLDOWN_TICKS = 10;
    public static final int HIT_FEEDBACK_TICKS = 10;

    public static final float FORWARD_MOVEMENT = 0.38F;
    public static final float MINIMUM_ATTACK_CHARGE = 1.0F;
    public static final int DAMAGE_PER_ATTACK = 1;

    public static final float PLAYER_ACTION_FACTOR = 1.0F;
    public static final float MOB_ACTION_FACTOR = 0.2F;

    public static final float ATTACK_RANGE_MIN = 2.0F;
    public static final float ATTACK_RANGE_MAX = 6.5F;

    public static final double MOTION_SCALE = 20.0D;

    public static final String DAMAGE_TYPE = "spear";

    private Spear() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 21, 11);
    }

    public static double motionPerSecond(double speedPerTick) {
        return speedPerTick * MOTION_SCALE;
    }

    public static double speedProjection(double lookX, double lookY, double lookZ, double motionX, double motionY, double motionZ) {
        return lookX * motionX + lookY * motionY + lookZ * motionZ;
    }

    public static double relativeSpeed(double attackerProjection, double targetProjection) {
        double difference = attackerProjection - targetProjection;
        return difference < 0.0D ? 0.0D : difference;
    }

    public static float actionFactor(boolean isPlayer) {
        return isPlayer ? PLAYER_ACTION_FACTOR : MOB_ACTION_FACTOR;
    }

    public static float damage(double baseAttackDamage, double relativeSpeed, float damageMultiplier) {
        return (float) baseAttackDamage + (float) Math.floor(relativeSpeed * (double) damageMultiplier);
    }

    public static double attackDamageBase(Material material) {
        return PlayerBase.ATTACK_DAMAGE + (double) material.attackDamageBonus();
    }

    public static double attackSpeed(float attackDurationSeconds) {
        return PlayerBase.ATTACK_SPEED + (1.0D / (double) attackDurationSeconds) - 4.0D;
    }

    public static int swingAnimationTicks(float attackDurationSeconds) {
        return (int) (attackDurationSeconds * 20.0F);
    }

    public static int delayTicks(float delaySeconds) {
        return (int) (delaySeconds * 20.0F);
    }

    public static boolean pierces() {
        return true;
    }

    public static Reality damageFrom(double baseAttackDamage, double relativeSpeed, float damageMultiplier) {
        return Reality.of(damage(baseAttackDamage, relativeSpeed, damageMultiplier));
    }

    public static Reality rangeFrom(double observedRange) {
        if (observedRange > ATTACK_RANGE_MAX) {
            return Reality.impossible();
        }
        return Reality.of(observedRange);
    }
}
