package teacommontea.veritechasse.vanilla.Potions;

import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger.FoodConstants;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class Saturation {

    public static final String KEY = "saturation";
    public static final int VANILLA_MAX_AMPLIFIER = 0;
    public static final boolean BENEFICIAL = true;
    public static final boolean INSTANTANEOUS = true;

    public static final float SATURATION_MODIFIER = 1.0F;

    private Saturation() {
    }

    public static int nutrition(int amplifier) {
        return amplifier + 1;
    }

    public static float saturationGained(int amplifier) {
        return FoodConstants.saturationByModifier(nutrition(amplifier), SATURATION_MODIFIER);
    }

    public static boolean affectsPlayersOnly() {
        return true;
    }

    public static int foodAfter(int currentFood, int amplifier, Protocol protocol) {
        return FoodConstants.addFood(currentFood, nutrition(amplifier), protocol);
    }

    public static float saturationAfter(float currentSaturation, int currentFood, int amplifier, Protocol protocol) {
        int food = foodAfter(currentFood, amplifier, protocol);
        return FoodConstants.addSaturation(currentSaturation, saturationGained(amplifier), food, protocol);
    }

    public static Reality foodFrom(int currentFood, int amplifier, Protocol protocol) {
        return Reality.of(foodAfter(currentFood, amplifier, protocol));
    }

    public static boolean exceedsVanillaMax(int amplifier) {
        return amplifier > VANILLA_MAX_AMPLIFIER;
    }
}
