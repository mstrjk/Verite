package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.Potions.Support.AirSupply;
import teacommontea.veritechasse.vanilla.Reality;

public final class WaterBreathing {

    public static final String KEY = "water_breathing";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = false;

    public static final boolean AMPLIFIER_CHANGES_NOTHING = true;

    private WaterBreathing() {
    }

    public static boolean preventsDrowning() {
        return true;
    }

    public static boolean permitsRefill() {
        return true;
    }

    public static Reality drowningDamage() {
        return Reality.impossible();
    }

    public static int airSupplyWhileSubmerged(int currentSupply) {
        return currentSupply < AirSupply.MAX_AIR_SUPPLY_TICKS ? currentSupply : AirSupply.MAX_AIR_SUPPLY_TICKS;
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
