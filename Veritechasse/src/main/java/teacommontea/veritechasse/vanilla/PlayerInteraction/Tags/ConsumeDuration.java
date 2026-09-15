package teacommontea.veritechasse.vanilla.PlayerInteraction.Tags;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class ConsumeDuration {

    public static final String KEY = "consume_duration";

    public static final int TICKS_PER_SECOND = 20;

    public static final float DEFAULT_SECONDS = 1.6F;
    public static final float FAST_SECONDS = 0.8F;
    public static final float HONEY_SECONDS = 2.0F;

    public static final int DEFAULT_TICKS = 32;
    public static final int FAST_TICKS = 16;
    public static final int HONEY_TICKS = 40;

    public static final int NOT_CONSUMABLE = 0;

    public static final Map<String, Integer> OVERRIDES = Map.of(
        "dried_kelp", Integer.valueOf(FAST_TICKS),
        "honey_bottle", Integer.valueOf(HONEY_TICKS));

    public static final Set<String> DRINKS = Set.of(
        "potion",
        "milk_bucket",
        "honey_bottle",
        "ominous_bottle");

    public static final Set<String> NON_FOOD_CONSUMABLES = Set.of(
        "potion",
        "milk_bucket",
        "ominous_bottle");

    private ConsumeDuration() {
    }

    public static boolean isConsumable(String itemName) {
        if (itemName == null) {
            return false;
        }
        String name = itemName.toLowerCase(Locale.ROOT);
        return FoodValues.isEdible(name) || NON_FOOD_CONSUMABLES.contains(name);
    }

    public static int ticksOf(String itemName) {
        if (!isConsumable(itemName)) {
            return NOT_CONSUMABLE;
        }
        Integer override = OVERRIDES.get(itemName.toLowerCase(Locale.ROOT));
        return override == null ? DEFAULT_TICKS : override.intValue();
    }

    public static boolean isDrink(String itemName) {
        return itemName != null && DRINKS.contains(itemName.toLowerCase(Locale.ROOT));
    }

    public static int ticksFromSeconds(float seconds) {
        return (int) (seconds * (float) TICKS_PER_SECOND);
    }

    public static boolean completedTooFast(String itemName, int observedTicks) {
        int required = ticksOf(itemName);
        if (required == NOT_CONSUMABLE) {
            return false;
        }
        return observedTicks < required;
    }
}
