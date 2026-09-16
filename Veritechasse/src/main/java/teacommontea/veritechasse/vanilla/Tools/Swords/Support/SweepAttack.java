package teacommontea.veritechasse.Vanilla.Tools.Swords.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Support.LootModifiers;
import teacommontea.veritechasse.Vanilla.Era;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;

public final class SweepAttack {

    public static final double REACH_SQR = 9.0D;
    public static final double BOX_INFLATE_XZ = 1.0D;
    public static final double BOX_INFLATE_Y = 0.25D;

    public static final float KNOCKBACK_STRENGTH = 0.4F;
    public static final float BASE_SWEEP_DAMAGE = 1.0F;

    public static final double MODERN_SPEED_MULTIPLIER = 2.5D;

    private SweepAttack() {
    }

    public static boolean speedGateIsSquared(Protocol protocol) {
        return protocol.atLeast(1, 21, 0);
    }

    public static boolean withinSpeedGate(double horizontalDistanceSqr, double walkedLastTick, double speed, Protocol protocol) {
        if (speedGateIsSquared(protocol)) {
            double limit = speed * MODERN_SPEED_MULTIPLIER;
            return horizontalDistanceSqr < limit * limit;
        }
        return walkedLastTick < speed;
    }

    public static boolean sweeps(
        boolean fullStrength,
        boolean criticalAttack,
        boolean knockbackAttack,
        boolean onGround,
        boolean holdingSword,
        double horizontalDistanceSqr,
        double walkedLastTick,
        double speed,
        Protocol protocol
    ) {
        if (!fullStrength || criticalAttack || knockbackAttack || !onGround) {
            return false;
        }
        if (!withinSpeedGate(horizontalDistanceSqr, walkedLastTick, speed, protocol)) {
            return false;
        }
        return holdingSword;
    }

    public static float sweepDamage(float baseDamage, double sweepingDamageRatio) {
        return BASE_SWEEP_DAMAGE + (float) sweepingDamageRatio * baseDamage;
    }

    public static float sweepDamageAfterCharge(float baseDamage, double sweepingDamageRatio, float attackStrengthScale) {
        return sweepDamage(baseDamage, sweepingDamageRatio) * attackStrengthScale;
    }

    public static float sweepDamage(ItemStack sword, float baseDamage, Era era) {
        return LootModifiers.sweepDamage(sword, baseDamage, era);
    }

    public static float sweepDamageAfterCharge(ItemStack sword, float baseDamage, float attackStrengthScale, Era era) {
        return sweepDamage(sword, baseDamage, era) * attackStrengthScale;
    }

    public static boolean withinReach(double distanceSqr) {
        return distanceSqr < REACH_SQR;
    }

    public static Reality damageFrom(float baseDamage, double sweepingDamageRatio, float attackStrengthScale) {
        return Reality.of(sweepDamageAfterCharge(baseDamage, sweepingDamageRatio, attackStrengthScale));
    }

    public static Reality damageFrom(ItemStack sword, float baseDamage, float attackStrengthScale, Era era) {
        return Reality.of(sweepDamageAfterCharge(sword, baseDamage, attackStrengthScale, era));
    }
}
