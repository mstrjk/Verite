package teacommontea.veritechasse.Vanilla.Enchantments;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LevelValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Maths.LinearValue;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Cost;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.EnchantableTrident;
import teacommontea.veritechasse.Vanilla.Enchantments.Tags.ExclusiveSetRiptide;
import teacommontea.veritechasse.Vanilla.Enchantments.Support.Registry;
import teacommontea.veritechasse.Vanilla.Era;

public final class Loyalty {

    private static final LevelValue DATA_DRIVEN = new LinearValue(1.0F, 1.0F);

    private static final int STORED_MIN = 0;
    private static final int STORED_MAX = 127;

    public static final double RETURN_ACCEL_PER_LEVEL = 0.05D;
    public static final double RETURN_LIFT_PER_LEVEL = 0.015D;
    public static final double RETURN_DRAG = 0.95D;

    public static final String KEY = "loyalty";
    public static final int VANILLA_MAX_LEVEL = 3;
    public static final int WEIGHT = 5;

    public static final String SUPPORTED_ITEMS = EnchantableTrident.KEY;
    public static final String EXCLUSIVE_SET = ExclusiveSetRiptide.KEY;

    public static final Cost COST = new Cost(
        new LinearValue(12.0F, 7.0F),
        new LinearValue(50.0F, 0.0F),
        2);

    private Loyalty() {
    }

    public static int levelOn(ItemStack stack) {
        return Registry.levelOn(stack, KEY);
    }

    public static boolean exceedsVanillaMax(int level) {
        return level > VANILLA_MAX_LEVEL;
    }

    public static int storedAcceleration(int level, Era era) {
        if (level <= 0) {
            return 0;
        }
        if (era == Era.ENCHANTS_AS_CLASSES) {
            return level;
        }

        int computed = (int) DATA_DRIVEN.calculate(level);
        if (computed < STORED_MIN) {
            return STORED_MIN;
        }
        return computed > STORED_MAX ? STORED_MAX : computed;
    }

    public static double returnSpeed(int level, Era era) {
        return RETURN_ACCEL_PER_LEVEL * (double) storedAcceleration(level, era);
    }

    public static double returnLift(int level, Era era) {
        return RETURN_LIFT_PER_LEVEL * (double) storedAcceleration(level, era);
    }

    public static double speedAfterTicks(double currentSpeed, double acceleration, int ticks) {
        double speed = currentSpeed;
        for (int i = 0; i < ticks; i++) {
            speed = speed * RETURN_DRAG + acceleration;
        }
        return speed;
    }

    public static boolean preventsDespawn(int level) {
        return level > 0;
    }
}
