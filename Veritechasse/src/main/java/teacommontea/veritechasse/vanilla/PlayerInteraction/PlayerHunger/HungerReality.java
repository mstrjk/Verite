package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger;

import teacommontea.veritechasse.vanilla.Potions.Hunger;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class HungerReality {

    public static final float TOLERANCE = 1.0E-4F;

    private HungerReality() {
    }

    public static float maximumExhaustionGain(
            double dx,
            double dy,
            double dz,
            boolean riding,
            boolean swimming,
            boolean eyeInWater,
            boolean inWater,
            boolean climbing,
            boolean onGround,
            boolean sprinting,
            boolean crouching,
            boolean jumped,
            int attacks,
            int blocksMined,
            ActiveEffects effects,
            int elapsedTicks) {
        float total = Exhaustion.movement(
            dx, dy, dz, riding, swimming, eyeInWater, inWater,
            climbing, onGround, sprinting, crouching);
        if (jumped) {
            total = total + Exhaustion.jumping(sprinting);
        }
        if (attacks > 0) {
            total = total + Exhaustion.ATTACK * (float) attacks;
        }
        if (blocksMined > 0) {
            total = total + Exhaustion.MINE * (float) blocksMined;
        }
        total = total + hungerEffectExhaustion(effects, elapsedTicks);
        return total;
    }

    public static float hungerEffectExhaustion(ActiveEffects effects, int elapsedTicks) {
        if (effects == null || elapsedTicks <= 0) {
            return 0.0F;
        }
        int amplifier = effects.amplifierOf(Hunger.KEY);
        if (amplifier == ActiveEffects.ABSENT) {
            return 0.0F;
        }
        return Hunger.exhaustionOver(amplifier, elapsedTicks);
    }

    public static boolean foodRoseWithoutCause(
            int foodBefore,
            int foodAfter,
            boolean consumed,
            boolean saturationEffect,
            String difficulty,
            boolean naturalRegeneration,
            int elapsedTicks) {
        if (consumed || saturationEffect) {
            return false;
        }
        if (foodAfter <= foodBefore) {
            return false;
        }
        if (!HungerTick.peacefulRestoresFood(difficulty, naturalRegeneration)) {
            return true;
        }
        int allowed = HungerTick.peacefulFoodGainOver(elapsedTicks);
        return foodAfter - foodBefore > allowed;
    }

    public static boolean saturationRoseWithoutCause(
            float saturationBefore,
            float saturationAfter,
            boolean consumed,
            boolean saturationEffect,
            String difficulty,
            boolean naturalRegeneration,
            int elapsedTicks,
            Protocol protocol) {
        if (consumed || saturationEffect) {
            return false;
        }
        if (saturationAfter <= saturationBefore + TOLERANCE) {
            return false;
        }
        if (!HungerTick.peacefulRestoresSaturation(
                difficulty, naturalRegeneration, protocol)) {
            return true;
        }
        float allowed = HungerTick.peacefulSaturationGainOver(elapsedTicks);
        return saturationAfter - saturationBefore > allowed + TOLERANCE;
    }

    public static boolean saturationExceedsFood(float saturation, int foodLevel) {
        return saturation > (float) foodLevel + TOLERANCE;
    }

    public static boolean exhaustionExceedsCap(float exhaustion) {
        return exhaustion > FoodConstants.MAX_EXHAUSTION + TOLERANCE;
    }

    public static boolean foodExceedsCap(int foodLevel) {
        return foodLevel > FoodConstants.MAX_FOOD;
    }

    public static boolean sprintedWhileExhausted(
            boolean sprinting,
            int foodLevel,
            boolean mayFly) {
        return sprinting && !HungerTick.canSprint(foodLevel, mayFly);
    }

    public static boolean healedWithoutFood(
            float healthBefore,
            float healthAfter,
            boolean naturalRegeneration,
            int foodLevel,
            float saturation,
            String difficulty) {
        if (healthAfter <= healthBefore) {
            return false;
        }
        if (!naturalRegeneration) {
            return false;
        }
        if (HungerTick.isPeaceful(difficulty)) {
            return false;
        }
        boolean saturated = saturation > 0.0F && foodLevel >= FoodConstants.MAX_FOOD;
        boolean slow = foodLevel >= FoodConstants.HEAL_LEVEL;
        return !saturated && !slow;
    }

    public static Reality exhaustionFrom(float gained) {
        return Reality.of(gained);
    }

    public static Reality foodFrom(int foodLevel) {
        return Reality.of(foodLevel);
    }
}
