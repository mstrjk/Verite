package teacommontea.veritechasse.Vanilla.Potions.Support;

public final class AirSupply {

    public static final int MAX_AIR_SUPPLY_TICKS = 300;
    public static final float DROWNING_DAMAGE = 2.0F;

    private AirSupply() {
    }

    public static boolean hasWaterBreathing(boolean waterBreathing, boolean conduitPower, boolean breathOfTheNautilus) {
        return waterBreathing || conduitPower || breathOfTheNautilus;
    }

    public static boolean shouldRefill(boolean waterBreathing, boolean conduitPower, boolean breathOfTheNautilus) {
        return !breathOfTheNautilus || waterBreathing || conduitPower;
    }

    public static boolean drowns(boolean canBreatheUnderwater, boolean waterBreathing, boolean conduitPower, boolean breathOfTheNautilus, boolean invulnerable) {
        if (canBreatheUnderwater) {
            return false;
        }
        if (hasWaterBreathing(waterBreathing, conduitPower, breathOfTheNautilus)) {
            return false;
        }
        return !invulnerable;
    }

    public static int decrease(int currentSupply, double oxygenBonus, double roll) {
        if (oxygenBonus > 0.0D && roll >= 1.0D / (oxygenBonus + 1.0D)) {
            return currentSupply;
        }
        return currentSupply - 1;
    }

    public static int decreaseWithoutRespiration(int currentSupply) {
        return currentSupply - 1;
    }

    public static int ticksUntilDrowning(int currentSupply) {
        return currentSupply < 0 ? 0 : currentSupply;
    }
}
