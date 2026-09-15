package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger;

import teacommontea.veritechasse.vanilla.Protocol;

public final class HungerTick {

    public static final String KEY = "hunger_tick";

    public static final String PEACEFUL = "peaceful";
    public static final String HARD = "hard";
    public static final String NORMAL = "normal";

    private HungerTick() {
    }

    public static boolean drains(float exhaustion) {
        return exhaustion > FoodConstants.EXHAUSTION_DROP;
    }

    public static float exhaustionAfterDrain(float exhaustion) {
        if (!drains(exhaustion)) {
            return exhaustion;
        }
        return exhaustion - FoodConstants.EXHAUSTION_DROP;
    }

    public static float saturationAfterDrain(float exhaustion, float saturation) {
        if (!drains(exhaustion) || saturation <= 0.0F) {
            return saturation;
        }
        float reduced = saturation - 1.0F;
        return reduced < 0.0F ? 0.0F : reduced;
    }

    public static int foodAfterDrain(
            float exhaustion,
            float saturation,
            int foodLevel,
            String difficulty) {
        if (!drains(exhaustion) || saturation > 0.0F) {
            return foodLevel;
        }
        if (PEACEFUL.equals(difficulty)) {
            return foodLevel;
        }
        int reduced = foodLevel - 1;
        return reduced < 0 ? 0 : reduced;
    }

    public static boolean regeneratesSaturated(
            boolean naturalRegeneration,
            float saturation,
            boolean hurt,
            int foodLevel) {
        return naturalRegeneration && saturation > 0.0F && hurt
            && foodLevel >= FoodConstants.MAX_FOOD;
    }

    public static boolean regeneratesSlowly(
            boolean naturalRegeneration,
            boolean hurt,
            int foodLevel) {
        return naturalRegeneration && foodLevel >= FoodConstants.HEAL_LEVEL && hurt;
    }

    public static boolean starves(int foodLevel) {
        return foodLevel <= FoodConstants.STARVE_LEVEL;
    }

    public static float saturatedHealAmount(float saturation) {
        float spent = Math.min(saturation, FoodConstants.EXHAUSTION_HEAL);
        return spent / FoodConstants.EXHAUSTION_HEAL;
    }

    public static float saturatedHealExhaustion(float saturation) {
        return Math.min(saturation, FoodConstants.EXHAUSTION_HEAL);
    }

    public static int regenerationInterval(boolean saturated) {
        return saturated
            ? FoodConstants.HEALTH_TICK_COUNT_SATURATED
            : FoodConstants.HEALTH_TICK_COUNT;
    }

    public static boolean starvationDamages(float health, String difficulty) {
        if (health > 10.0F) {
            return true;
        }
        if (HARD.equals(difficulty)) {
            return true;
        }
        return health > 1.0F && NORMAL.equals(difficulty);
    }

    public static final int PEACEFUL_HEAL_INTERVAL_TICKS = 20;
    public static final int PEACEFUL_FOOD_INTERVAL_TICKS = 10;
    public static final int PEACEFUL_SATURATION_INTERVAL_TICKS = 20;

    public static final float PEACEFUL_HEAL_AMOUNT = 1.0F;
    public static final int PEACEFUL_FOOD_AMOUNT = 1;
    public static final float PEACEFUL_SATURATION_AMOUNT = 1.0F;

    public static final int PEACEFUL_SATURATION_MAJOR = 1;
    public static final int PEACEFUL_SATURATION_MINOR = 21;
    public static final int PEACEFUL_SATURATION_PATCH = 2;

    public static boolean isPeaceful(String difficulty) {
        return PEACEFUL.equals(difficulty);
    }

    public static boolean peacefulRestoresFood(String difficulty, boolean naturalRegeneration) {
        return isPeaceful(difficulty) && naturalRegeneration;
    }

    public static boolean peacefulRestoresSaturation(
            String difficulty,
            boolean naturalRegeneration,
            Protocol protocol) {
        return peacefulRestoresFood(difficulty, naturalRegeneration)
            && protocol.atLeast(
                PEACEFUL_SATURATION_MAJOR,
                PEACEFUL_SATURATION_MINOR,
                PEACEFUL_SATURATION_PATCH);
    }

    public static int peacefulFoodGainOver(int elapsedTicks) {
        if (elapsedTicks <= 0) {
            return 0;
        }
        return elapsedTicks / PEACEFUL_FOOD_INTERVAL_TICKS * PEACEFUL_FOOD_AMOUNT;
    }

    public static float peacefulSaturationGainOver(int elapsedTicks) {
        if (elapsedTicks <= 0) {
            return 0.0F;
        }
        return (float) (elapsedTicks / PEACEFUL_SATURATION_INTERVAL_TICKS)
            * PEACEFUL_SATURATION_AMOUNT;
    }

    public static float peacefulHealOver(int elapsedTicks) {
        if (elapsedTicks <= 0) {
            return 0.0F;
        }
        return (float) (elapsedTicks / PEACEFUL_HEAL_INTERVAL_TICKS) * PEACEFUL_HEAL_AMOUNT;
    }

    public static boolean hasEnoughFood(int foodLevel) {
        return foodLevel > FoodConstants.SPRINT_LEVEL;
    }

    public static boolean canSprint(int foodLevel, boolean mayFly) {
        return hasEnoughFood(foodLevel) || mayFly;
    }
}
