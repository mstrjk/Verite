package teacommontea.veritechasse.vanilla.Tools.Trident.Support;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Trident.Trident;

public final class TridentUse {

    public static final int USE_DURATION = 72000;

    public static final double ATTACK_DAMAGE_MODIFIER = 8.0D;
    public static final double ATTACK_SPEED_MODIFIER = -2.9D;

    public static final double DEFAULT_PLAYER_ATTACK_SPEED = 4.0D;

    private TridentUse() {
    }

    public static double attackSpeed() {
        return DEFAULT_PLAYER_ATTACK_SPEED + ATTACK_SPEED_MODIFIER;
    }

    public static double attackCooldownTicks(double attackSpeedAttribute) {
        if (attackSpeedAttribute <= 0.0D) {
            return Double.POSITIVE_INFINITY;
        }
        return 1.0D / attackSpeedAttribute * 20.0D;
    }

    public static double attackCooldownTicks() {
        return attackCooldownTicks(attackSpeed());
    }

    public static double attackStrengthScale(int ticksSinceAttack, double attackSpeedAttribute) {
        double delay = attackCooldownTicks(attackSpeedAttribute);
        if (delay <= 0.0D) {
            return 1.0D;
        }
        double scale = (double) ticksSinceAttack / delay;
        if (scale < 0.0D) {
            return 0.0D;
        }
        return scale > 1.0D ? 1.0D : scale;
    }

    public static Reality meleeDamage(double baseAttackDamage) {
        return Reality.of(baseAttackDamage + ATTACK_DAMAGE_MODIFIER);
    }

    public static boolean canStartRiptide(ItemStack stack, boolean inWaterOrRain, Protocol protocol) {
        if (!Trident.is(stack)) {
            return false;
        }
        TridentState state = Trident.resolve(stack, protocol);
        if (!state.propelsHolder()) {
            return false;
        }
        return inWaterOrRain;
    }

    public static boolean canReleaseRiptide(ItemStack stack, boolean inWaterOrRain, boolean passenger, Protocol protocol) {
        if (!canStartRiptide(stack, inWaterOrRain, protocol)) {
            return false;
        }
        return !passenger;
    }

    public static boolean canThrow(ItemStack stack, int ticksHeld, Protocol protocol) {
        if (!Trident.is(stack)) {
            return false;
        }
        if (!Trident.heldLongEnough(ticksHeld)) {
            return false;
        }
        return !Trident.resolve(stack, protocol).propelsHolder();
    }

    public static int ticksHeld(int remainingUseTicks) {
        return USE_DURATION - remainingUseTicks;
    }
}
