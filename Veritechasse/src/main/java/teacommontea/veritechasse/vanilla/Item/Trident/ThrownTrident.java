package teacommontea.veritechasse.vanilla.Item.Trident;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Enchantments.Loyalty;

public final class ThrownTrident {

    public static final float BASE_DAMAGE = 8.0F;

    public static final float AIR_INERTIA = 0.99F;
    public static final float WATER_INERTIA = 0.99F;
    public static final double GRAVITY = 0.05D;

    public static final int SHAKE_TIME = 7;
    public static final int IN_GROUND_TIME_TO_DEAL_DAMAGE = 4;

    public static final double HIT_ENTITY_DRAG_X = 0.02D;
    public static final double HIT_ENTITY_DRAG_Y = 0.2D;
    public static final double HIT_ENTITY_DRAG_Z = 0.02D;

    private ThrownTrident() {
    }

    public static double launchSpeed(ItemStack stack, Protocol protocol) {
        TridentState state = Trident.resolve(stack, protocol);
        return state.throwPower();
    }

    public static Reality flightSpeed(ItemStack stack, Protocol protocol) {
        double speed = launchSpeed(stack, protocol);
        if (speed <= 0.0D) {
            return Reality.impossible();
        }
        return Reality.of(speed);
    }

    public static double speedAfterTicks(double initialSpeed, int ticks, boolean inWater) {
        double inertia = inWater ? WATER_INERTIA : AIR_INERTIA;
        double speed = initialSpeed;
        for (int i = 0; i < ticks; i++) {
            speed *= inertia;
        }
        return speed;
    }

    public static boolean despawns(ItemStack stack, Protocol protocol) {
        return Loyalty.levelOn(stack) <= 0;
    }

    public static Reality returnSpeed(ItemStack stack, Protocol protocol) {
        int level = Loyalty.levelOn(stack);
        if (level <= 0) {
            return Reality.impossible();
        }
        return Reality.of(Loyalty.returnSpeed(level, Era.enchantments(protocol)));
    }

    public static Reality returnLift(ItemStack stack, Protocol protocol) {
        int level = Loyalty.levelOn(stack);
        if (level <= 0) {
            return Reality.impossible();
        }
        return Reality.of(Loyalty.returnLift(level, Era.enchantments(protocol)));
    }

    public static double returnSpeedAfterTicks(double currentSpeed, double acceleration, int ticks) {
        double speed = currentSpeed;
        for (int i = 0; i < ticks; i++) {
            speed = speed * Loyalty.RETURN_DRAG + acceleration;
        }
        return speed;
    }

    public static Reality impactDamage(ItemStack stack, boolean sensitiveToImpaling, Protocol protocol) {
        TridentState state = Trident.resolve(stack, protocol);
        float damage = BASE_DAMAGE;
        if (sensitiveToImpaling) {
            damage += state.bonusDamage();
        }
        return Reality.of(damage);
    }
}
