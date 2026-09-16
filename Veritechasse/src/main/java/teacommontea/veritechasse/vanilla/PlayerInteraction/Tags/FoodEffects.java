package teacommontea.veritechasse.Vanilla.PlayerInteraction.Tags;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Potions.BadOmen;
import teacommontea.veritechasse.Vanilla.Protocol;

public final class FoodEffects {

    public static final String KEY = "food_effects";

    public static final float ALWAYS = 1.0F;

    public static final Map<String, List<Applied>> MEMBERS = Map.of(
        "chicken", List.of(
            new Applied("hunger", 600, 0, 0.3F)),
        "enchanted_golden_apple", List.of(
            new Applied("regeneration", 400, 1, ALWAYS),
            new Applied("resistance", 6000, 0, ALWAYS),
            new Applied("fire_resistance", 6000, 0, ALWAYS),
            new Applied("absorption", 2400, 3, ALWAYS)),
        "golden_apple", List.of(
            new Applied("regeneration", 100, 1, ALWAYS),
            new Applied("absorption", 2400, 0, ALWAYS)),
        "poisonous_potato", List.of(
            new Applied("poison", 100, 0, 0.6F)),
        "pufferfish", List.of(
            new Applied("poison", 1200, 1, ALWAYS),
            new Applied("hunger", 300, 2, ALWAYS),
            new Applied("nausea", 300, 0, ALWAYS)),
        "rotten_flesh", List.of(
            new Applied("hunger", 600, 0, 0.8F)),
        "spider_eye", List.of(
            new Applied("poison", 100, 0, ALWAYS)));

    public static final Set<String> VARIABLE_AMPLIFIER = Set.of("ominous_bottle");

    private FoodEffects() {
    }

    public static boolean contains(String itemName) {
        return itemName != null && MEMBERS.containsKey(itemName.toLowerCase(Locale.ROOT));
    }

    public static List<Applied> of(String itemName) {
        if (itemName == null) {
            return List.of();
        }
        List<Applied> applied = MEMBERS.get(itemName.toLowerCase(Locale.ROOT));
        return applied == null ? List.of() : applied;
    }

    public static boolean applies(String itemName, String effectKey) {
        if (effectKey == null) {
            return false;
        }
        String wanted = effectKey.toLowerCase(Locale.ROOT);
        for (Applied applied : of(itemName)) {
            if (applied.effectKey().equals(wanted)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCertain(String itemName, String effectKey) {
        if (effectKey == null) {
            return false;
        }
        String wanted = effectKey.toLowerCase(Locale.ROOT);
        for (Applied applied : of(itemName)) {
            if (applied.effectKey().equals(wanted)) {
                return applied.probability() >= ALWAYS;
            }
        }
        return false;
    }

    public static boolean hasVariableAmplifier(String itemName) {
        return itemName != null
            && VARIABLE_AMPLIFIER.contains(itemName.toLowerCase(Locale.ROOT));
    }

    public static List<Applied> ofOminousBottle(int amplifier, Protocol protocol) {
        if (!BadOmen.grantedByOminousBottle(amplifier, protocol)) {
            return List.of();
        }
        return List.of(new Applied(
            BadOmen.KEY, BadOmen.ominousBottleDurationTicks(), amplifier, ALWAYS));
    }

    public record Applied(String effectKey, int durationTicks, int amplifier, float probability) {
    }
}
