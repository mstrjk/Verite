package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger;

import teacommontea.veritechasse.vanilla.Protocol;

public final class FoodConstants {

    public static final int MAX_FOOD = 20;
    public static final float MAX_SATURATION = 20.0F;
    public static final float START_SATURATION = 5.0F;
    public static final float SATURATION_FLOOR = 2.5F;
    public static final float EXHAUSTION_DROP = 4.0F;
    public static final float MAX_EXHAUSTION = 40.0F;

    public static final int HEALTH_TICK_COUNT = 80;
    public static final int HEALTH_TICK_COUNT_SATURATED = 10;
    public static final int HEAL_LEVEL = 18;
    public static final int SPRINT_LEVEL = 6;
    public static final int STARVE_LEVEL = 0;

    public static final float EXHAUSTION_HEAL = 6.0F;
    public static final float EXHAUSTION_JUMP = 0.05F;
    public static final float EXHAUSTION_SPRINT_JUMP = 0.2F;
    public static final float EXHAUSTION_MINE = 0.005F;
    public static final float EXHAUSTION_ATTACK = 0.1F;
    public static final float EXHAUSTION_WALK = 0.0F;
    public static final float EXHAUSTION_CROUCH = 0.0F;
    public static final float EXHAUSTION_SPRINT = 0.1F;
    public static final float EXHAUSTION_SWIM = 0.01F;

    private FoodConstants() {
    }

    public static float saturationByModifier(int nutrition, float modifier) {
        return (float) nutrition * modifier * 2.0F;
    }

    public static float addExhaustion(float current, float amount) {
        float total = current + amount;
        return total > MAX_EXHAUSTION ? MAX_EXHAUSTION : total;
    }

    public static boolean clampsLowerBound(Protocol protocol) {
        return protocol.atLeast(1, 21, 0);
    }

    public static int addFood(int currentFood, int nutrition, Protocol protocol) {
        int total = nutrition + currentFood;
        if (total > MAX_FOOD) {
            return MAX_FOOD;
        }
        if (clampsLowerBound(protocol) && total < 0) {
            return 0;
        }
        return total;
    }

    public static float addSaturation(float currentSaturation, float saturation, int foodLevel, Protocol protocol) {
        float total = saturation + currentSaturation;
        if (total > (float) foodLevel) {
            return (float) foodLevel;
        }
        if (clampsLowerBound(protocol) && total < 0.0F) {
            return 0.0F;
        }
        return total;
    }
}
