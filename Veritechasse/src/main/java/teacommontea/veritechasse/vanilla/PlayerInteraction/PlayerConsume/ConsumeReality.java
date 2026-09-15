package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerConsume;

import org.bukkit.inventory.ItemStack;

import teacommontea.veritechasse.vanilla.Era;
import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger.FoodConstants;
import teacommontea.veritechasse.vanilla.PlayerInteraction.Tags.ConsumeDuration;
import teacommontea.veritechasse.vanilla.PlayerInteraction.Tags.FoodEffects;
import teacommontea.veritechasse.vanilla.PlayerInteraction.Tags.FoodValues;
import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;

public final class ConsumeReality {

    public static final double TOLERANCE = 1.0E-6D;

    private ConsumeReality() {
    }

    public static int minimumTicks(String itemName) {
        return ConsumeDuration.ticksOf(itemName);
    }

    public static boolean completedTooFast(String itemName, int observedElapsedTicks) {
        return ConsumeDuration.completedTooFast(itemName, observedElapsedTicks);
    }

    public static int expectedFoodAfter(String itemName, int currentFood, Protocol protocol) {
        int nutrition = FoodValues.nutritionOf(itemName);
        if (nutrition == FoodValues.ABSENT) {
            return currentFood;
        }
        return FoodConstants.addFood(currentFood, nutrition, protocol);
    }

    public static float expectedSaturationAfter(
            String itemName,
            float currentSaturation,
            int foodAfter,
            Protocol protocol) {
        if (!FoodValues.isEdible(itemName)) {
            return currentSaturation;
        }
        return FoodConstants.addSaturation(
            currentSaturation, FoodValues.saturationOf(itemName), foodAfter, protocol);
    }

    public static boolean gainedTooMuchFood(
            String itemName,
            int foodBefore,
            int foodAfter,
            Protocol protocol) {
        return foodAfter > expectedFoodAfter(itemName, foodBefore, protocol);
    }

    public static boolean gainedTooMuchSaturation(
            String itemName,
            float saturationBefore,
            float saturationAfter,
            int foodAfter,
            Protocol protocol) {
        float bound = expectedSaturationAfter(
            itemName, saturationBefore, foodAfter, protocol);
        return saturationAfter > bound + (float) TOLERANCE;
    }

    public static boolean grantsEffect(String itemName, String effectKey) {
        return FoodEffects.applies(itemName, effectKey);
    }

    public static boolean effectIsUnexplained(String itemName, String effectKey) {
        return !FoodEffects.applies(itemName, effectKey);
    }

    public static double maximumHorizontal(
            String itemName,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            ItemStack boots,
            float percentFrozen,
            boolean onGround,
            Era era,
            Protocol protocol) {
        float multiplier = UseSlowdown.multiplierFor(itemName, protocol);
        return UseSlowdown.terminalSpeed(
            effects, sprinting, blockBelow, blockHere,
            boots, percentFrozen, onGround, era, multiplier);
    }

    public static boolean movedTooFastWhileUsing(
            double observedHorizontal,
            String itemName,
            ActiveEffects effects,
            boolean sprinting,
            String blockBelow,
            String blockHere,
            ItemStack boots,
            float percentFrozen,
            boolean onGround,
            Era era,
            Protocol protocol) {
        double bound = maximumHorizontal(
            itemName, effects, sprinting, blockBelow, blockHere,
            boots, percentFrozen, onGround, era, protocol);
        return observedHorizontal > bound + TOLERANCE;
    }

    public static boolean sprintedWhileUsing(
            String itemName,
            boolean sprinting,
            Protocol protocol) {
        if (!sprinting || !UseAnimations.isUsable(itemName, protocol)) {
            return false;
        }
        return !UseSlowdown.permitsSprintingWith(itemName, protocol);
    }

    public static Reality durationFrom(String itemName) {
        return Reality.of(minimumTicks(itemName));
    }

    public static Reality foodFrom(String itemName, int currentFood, Protocol protocol) {
        return Reality.of(expectedFoodAfter(itemName, currentFood, protocol));
    }
}
