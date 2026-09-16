package teacommontea.veritechasse.Vanilla.ControllableEntities.Minecarts;

import teacommontea.veritechasse.Vanilla.Reality;

public final class FurnaceMinecart {

    public static final String KEY = "furnace_minecart";

    public static final int FUEL_TICKS_PER_ITEM = 3600;
    public static final int MAX_FUEL_TICKS = 32000;

    public static final double PUSH_FRICTION = 0.8D;
    public static final double UNFUELLED_FRICTION = 0.98D;
    public static final double WATER_SCALE = 0.1D;

    public static final double PUSH_EPSILON_SQR = 1.0E-4D;
    public static final double MOVEMENT_EPSILON_SQR = 0.001D;
    public static final double PUSH_ACTIVE_THRESHOLD_SQR = 1.0E-7D;

    public static final boolean SELF_PROPELLED = true;

    private FurnaceMinecart() {
    }

    public static boolean hasFuel(int fuelTicks) {
        return fuelTicks > 0;
    }

    public static int fuelAfterItem(int currentFuel) {
        int total = currentFuel + FUEL_TICKS_PER_ITEM;
        return total > MAX_FUEL_TICKS ? MAX_FUEL_TICKS : total;
    }

    public static int fuelAfterTick(int currentFuel) {
        int left = currentFuel - 1;
        return left < 0 ? 0 : left;
    }

    public static boolean fuelIsLegal(int fuelTicks) {
        return fuelTicks >= 0 && fuelTicks <= MAX_FUEL_TICKS;
    }

    public static double frictionFor(boolean pushing) {
        return pushing ? PUSH_FRICTION : UNFUELLED_FRICTION;
    }

    public static double speedAfterFriction(double speed, boolean pushing, boolean inWater) {
        double result = speed * frictionFor(pushing);
        return inWater ? result * WATER_SCALE : result;
    }

    public static boolean pushIsActive(double pushLengthSqr) {
        return pushLengthSqr > PUSH_ACTIVE_THRESHOLD_SQR;
    }

    public static Reality maxFuelFrom() {
        return Reality.of(MAX_FUEL_TICKS);
    }
}
