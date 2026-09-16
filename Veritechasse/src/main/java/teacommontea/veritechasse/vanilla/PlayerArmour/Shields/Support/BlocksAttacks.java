package teacommontea.veritechasse.Vanilla.PlayerArmour.Shields.Support;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class BlocksAttacks {

    public static final float BLOCK_DELAY_SECONDS = 0.25F;
    public static final int BLOCK_DELAY_TICKS = 5;

    public static final float DISABLE_COOLDOWN_SCALE = 1.0F;

    public static final float HORIZONTAL_BLOCKING_ANGLE = 90.0F;
    public static final float DAMAGE_REDUCTION_BASE = 0.0F;
    public static final float DAMAGE_REDUCTION_FACTOR = 1.0F;

    public static final float ITEM_DAMAGE_THRESHOLD = 3.0F;
    public static final float ITEM_DAMAGE_BASE = 1.0F;
    public static final float ITEM_DAMAGE_FACTOR = 1.0F;

    public static final int COMPONENT_PROTOCOL_MAJOR = 1;
    public static final int COMPONENT_PROTOCOL_MINOR = 21;
    public static final int COMPONENT_PROTOCOL_PATCH = 5;

    private BlocksAttacks() {
    }

    public static boolean isComponentDriven(Protocol protocol) {
        return protocol.atLeast(COMPONENT_PROTOCOL_MAJOR, COMPONENT_PROTOCOL_MINOR, COMPONENT_PROTOCOL_PATCH);
    }

    public static boolean delayElapsed(int ticksHeld) {
        return ticksHeld >= BLOCK_DELAY_TICKS;
    }

    public static double blockingAngleRadians() {
        return Math.toRadians(HORIZONTAL_BLOCKING_ANGLE);
    }

    public static boolean withinBlockingAngle(double angleRadians) {
        return angleRadians <= blockingAngleRadians();
    }

    public static float damageReduction(float dealtDamage, double angleRadians) {
        if (!withinBlockingAngle(angleRadians)) {
            return 0.0F;
        }
        float blocked = DAMAGE_REDUCTION_BASE + DAMAGE_REDUCTION_FACTOR * dealtDamage;
        if (blocked < 0.0F) {
            return 0.0F;
        }
        return blocked > dealtDamage ? dealtDamage : blocked;
    }

    public static float damageAfterBlocking(float dealtDamage, double angleRadians, boolean bypassed) {
        if (bypassed) {
            return dealtDamage;
        }
        return dealtDamage - damageReduction(dealtDamage, angleRadians);
    }

    public static boolean piercingBypassesBlock(int pierceLevel) {
        return pierceLevel > 0;
    }

    public static double horizontalAngleTo(
            double sourceX,
            double sourceZ,
            double victimX,
            double victimZ,
            double headYawRadians) {
        double toSourceX = sourceX - victimX;
        double toSourceZ = sourceZ - victimZ;
        double length = Math.sqrt(toSourceX * toSourceX + toSourceZ * toSourceZ);
        if (length <= 0.0D) {
            return Math.PI;
        }
        double normalisedX = toSourceX / length;
        double normalisedZ = toSourceZ / length;
        double viewX = -Math.sin(headYawRadians);
        double viewZ = Math.cos(headYawRadians);
        double dot = normalisedX * viewX + normalisedZ * viewZ;
        if (dot < -1.0D) {
            dot = -1.0D;
        }
        if (dot > 1.0D) {
            dot = 1.0D;
        }
        return Math.acos(dot);
    }

    public static int itemDamageFrom(float dealtDamage) {
        if (dealtDamage < ITEM_DAMAGE_THRESHOLD) {
            return 0;
        }
        return (int) (ITEM_DAMAGE_BASE + ITEM_DAMAGE_FACTOR * dealtDamage);
    }

    public static Reality reductionFrom(float dealtDamage, double angleRadians) {
        return Reality.of(damageReduction(dealtDamage, angleRadians));
    }

    public static Reality delayFrom() {
        return Reality.of(BLOCK_DELAY_TICKS);
    }
}
