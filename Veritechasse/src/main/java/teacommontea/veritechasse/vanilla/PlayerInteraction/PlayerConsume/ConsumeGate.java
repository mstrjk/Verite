package teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerConsume;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger.FoodConstants;
import teacommontea.veritechasse.vanilla.PlayerInteraction.Tags.ConsumeDuration;
import teacommontea.veritechasse.vanilla.PlayerInteraction.Tags.FoodValues;
import teacommontea.veritechasse.vanilla.Protocol;

public final class ConsumeGate {

    public static final String KEY = "consume_gate";

    public static final int HONEY_ALWAYS_EDIBLE_MAJOR = 1;
    public static final int HONEY_ALWAYS_EDIBLE_MINOR = 21;
    public static final int HONEY_ALWAYS_EDIBLE_PATCH = 2;

    public static final Set<String> ALWAYS_EDIBLE = Set.of(
        "chorus_fruit",
        "enchanted_golden_apple",
        "golden_apple",
        "suspicious_stew");

    public static final String HONEY_BOTTLE = "honey_bottle";

    private ConsumeGate() {
    }

    public static boolean honeyIsAlwaysEdible(Protocol protocol) {
        return protocol.atLeast(
            HONEY_ALWAYS_EDIBLE_MAJOR,
            HONEY_ALWAYS_EDIBLE_MINOR,
            HONEY_ALWAYS_EDIBLE_PATCH);
    }

    public static boolean isAlwaysEdible(String itemName, Protocol protocol) {
        if (itemName == null) {
            return false;
        }
        String name = itemName.toLowerCase(Locale.ROOT);
        if (HONEY_BOTTLE.equals(name)) {
            return honeyIsAlwaysEdible(protocol);
        }
        return ALWAYS_EDIBLE.contains(name);
    }

    public static boolean requiresHunger(String itemName, Protocol protocol) {
        return FoodValues.isEdible(itemName) && !isAlwaysEdible(itemName, protocol);
    }

    public static boolean canStart(
            String itemName,
            int foodLevel,
            boolean invulnerable,
            Protocol protocol) {
        if (!ConsumeDuration.isConsumable(itemName)) {
            return false;
        }
        if (!FoodValues.isEdible(itemName)) {
            return true;
        }
        if (invulnerable || isAlwaysEdible(itemName, protocol)) {
            return true;
        }
        return foodLevel < FoodConstants.MAX_FOOD;
    }

    public static boolean startedWhileFull(
            String itemName,
            int foodLevel,
            boolean invulnerable,
            Protocol protocol) {
        if (!FoodValues.isEdible(itemName)) {
            return false;
        }
        return !canStart(itemName, foodLevel, invulnerable, protocol);
    }
}
