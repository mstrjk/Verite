package teacommontea.veritechasse.vanilla.PlayerInteraction.Tags;

import java.util.Locale;
import java.util.Map;

import teacommontea.veritechasse.vanilla.PlayerInteraction.PlayerHunger.FoodConstants;

public final class FoodValues {

    public static final String KEY = "food_values";

    public static final int ABSENT = -1;

    public static final Map<String, Integer> NUTRITION = Map.ofEntries(
        Map.entry("apple", Integer.valueOf(4)),
        Map.entry("baked_potato", Integer.valueOf(5)),
        Map.entry("beef", Integer.valueOf(3)),
        Map.entry("beetroot", Integer.valueOf(1)),
        Map.entry("beetroot_soup", Integer.valueOf(6)),
        Map.entry("bread", Integer.valueOf(5)),
        Map.entry("carrot", Integer.valueOf(3)),
        Map.entry("chicken", Integer.valueOf(2)),
        Map.entry("chorus_fruit", Integer.valueOf(4)),
        Map.entry("cod", Integer.valueOf(2)),
        Map.entry("cooked_beef", Integer.valueOf(8)),
        Map.entry("cooked_chicken", Integer.valueOf(6)),
        Map.entry("cooked_cod", Integer.valueOf(5)),
        Map.entry("cooked_mutton", Integer.valueOf(6)),
        Map.entry("cooked_porkchop", Integer.valueOf(8)),
        Map.entry("cooked_rabbit", Integer.valueOf(5)),
        Map.entry("cooked_salmon", Integer.valueOf(6)),
        Map.entry("cookie", Integer.valueOf(2)),
        Map.entry("dried_kelp", Integer.valueOf(1)),
        Map.entry("enchanted_golden_apple", Integer.valueOf(4)),
        Map.entry("glow_berries", Integer.valueOf(2)),
        Map.entry("golden_apple", Integer.valueOf(4)),
        Map.entry("golden_carrot", Integer.valueOf(6)),
        Map.entry("honey_bottle", Integer.valueOf(6)),
        Map.entry("melon_slice", Integer.valueOf(2)),
        Map.entry("mushroom_stew", Integer.valueOf(6)),
        Map.entry("mutton", Integer.valueOf(2)),
        Map.entry("poisonous_potato", Integer.valueOf(2)),
        Map.entry("porkchop", Integer.valueOf(3)),
        Map.entry("potato", Integer.valueOf(1)),
        Map.entry("pufferfish", Integer.valueOf(1)),
        Map.entry("pumpkin_pie", Integer.valueOf(8)),
        Map.entry("rabbit", Integer.valueOf(3)),
        Map.entry("rabbit_stew", Integer.valueOf(10)),
        Map.entry("rotten_flesh", Integer.valueOf(4)),
        Map.entry("salmon", Integer.valueOf(2)),
        Map.entry("spider_eye", Integer.valueOf(2)),
        Map.entry("suspicious_stew", Integer.valueOf(6)),
        Map.entry("sweet_berries", Integer.valueOf(2)),
        Map.entry("tropical_fish", Integer.valueOf(1)));

    public static final Map<String, Float> SATURATION_MODIFIER = Map.ofEntries(
        Map.entry("apple", Float.valueOf(0.3F)),
        Map.entry("baked_potato", Float.valueOf(0.6F)),
        Map.entry("beef", Float.valueOf(0.3F)),
        Map.entry("beetroot", Float.valueOf(0.6F)),
        Map.entry("beetroot_soup", Float.valueOf(0.6F)),
        Map.entry("bread", Float.valueOf(0.6F)),
        Map.entry("carrot", Float.valueOf(0.6F)),
        Map.entry("chicken", Float.valueOf(0.3F)),
        Map.entry("chorus_fruit", Float.valueOf(0.3F)),
        Map.entry("cod", Float.valueOf(0.1F)),
        Map.entry("cooked_beef", Float.valueOf(0.8F)),
        Map.entry("cooked_chicken", Float.valueOf(0.6F)),
        Map.entry("cooked_cod", Float.valueOf(0.6F)),
        Map.entry("cooked_mutton", Float.valueOf(0.8F)),
        Map.entry("cooked_porkchop", Float.valueOf(0.8F)),
        Map.entry("cooked_rabbit", Float.valueOf(0.6F)),
        Map.entry("cooked_salmon", Float.valueOf(0.8F)),
        Map.entry("cookie", Float.valueOf(0.1F)),
        Map.entry("dried_kelp", Float.valueOf(0.3F)),
        Map.entry("enchanted_golden_apple", Float.valueOf(1.2F)),
        Map.entry("glow_berries", Float.valueOf(0.1F)),
        Map.entry("golden_apple", Float.valueOf(1.2F)),
        Map.entry("golden_carrot", Float.valueOf(1.2F)),
        Map.entry("honey_bottle", Float.valueOf(0.1F)),
        Map.entry("melon_slice", Float.valueOf(0.3F)),
        Map.entry("mushroom_stew", Float.valueOf(0.6F)),
        Map.entry("mutton", Float.valueOf(0.3F)),
        Map.entry("poisonous_potato", Float.valueOf(0.3F)),
        Map.entry("porkchop", Float.valueOf(0.3F)),
        Map.entry("potato", Float.valueOf(0.3F)),
        Map.entry("pufferfish", Float.valueOf(0.1F)),
        Map.entry("pumpkin_pie", Float.valueOf(0.3F)),
        Map.entry("rabbit", Float.valueOf(0.3F)),
        Map.entry("rabbit_stew", Float.valueOf(0.6F)),
        Map.entry("rotten_flesh", Float.valueOf(0.1F)),
        Map.entry("salmon", Float.valueOf(0.1F)),
        Map.entry("spider_eye", Float.valueOf(0.8F)),
        Map.entry("suspicious_stew", Float.valueOf(0.6F)),
        Map.entry("sweet_berries", Float.valueOf(0.1F)),
        Map.entry("tropical_fish", Float.valueOf(0.1F)));

    private FoodValues() {
    }

    public static boolean isEdible(String itemName) {
        return itemName != null && NUTRITION.containsKey(itemName.toLowerCase(Locale.ROOT));
    }

    public static int nutritionOf(String itemName) {
        if (itemName == null) {
            return ABSENT;
        }
        Integer value = NUTRITION.get(itemName.toLowerCase(Locale.ROOT));
        return value == null ? ABSENT : value.intValue();
    }

    public static float saturationModifierOf(String itemName) {
        if (itemName == null) {
            return 0.0F;
        }
        Float value = SATURATION_MODIFIER.get(itemName.toLowerCase(Locale.ROOT));
        return value == null ? 0.0F : value.floatValue();
    }

    public static float saturationOf(String itemName) {
        int nutrition = nutritionOf(itemName);
        if (nutrition == ABSENT) {
            return 0.0F;
        }
        return FoodConstants.saturationByModifier(nutrition, saturationModifierOf(itemName));
    }
}
