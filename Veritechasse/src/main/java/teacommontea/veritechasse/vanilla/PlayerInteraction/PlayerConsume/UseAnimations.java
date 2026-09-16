package teacommontea.veritechasse.Vanilla.PlayerInteraction.PlayerConsume;

import java.util.Locale;
import java.util.Set;

import teacommontea.veritechasse.Vanilla.Enchantments.Tags.Spears;
import teacommontea.veritechasse.Vanilla.PlayerInteraction.Tags.ConsumeDuration;
import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Tools.Spears.Support.Spear;

public final class UseAnimations {

    public static final String KEY = "use_animations";

    public static final String NONE = "none";
    public static final String EAT = "eat";
    public static final String DRINK = "drink";
    public static final String BLOCK = "block";
    public static final String BOW = "bow";
    public static final String CROSSBOW = "crossbow";
    public static final String TRIDENT = "trident";
    public static final String SPEAR = "spear";
    public static final String SPYGLASS = "spyglass";
    public static final String TOOT_HORN = "toot_horn";
    public static final String BRUSH = "brush";
    public static final String BUNDLE = "bundle";

    public static final Set<String> BOWS = Set.of("bow");

    public static final Set<String> CROSSBOWS = Set.of("crossbow");

    public static final Set<String> TRIDENTS = Set.of("trident");

    public static final Set<String> SHIELDS = Set.of("shield");

    public static final Set<String> SPYGLASSES = Set.of("spyglass");

    public static final Set<String> HORNS = Set.of("goat_horn");

    public static final Set<String> BRUSHES = Set.of("brush");

    public static final Set<String> BUNDLES = Set.of(
        "bundle",
        "white_bundle",
        "orange_bundle",
        "magenta_bundle",
        "light_blue_bundle",
        "yellow_bundle",
        "lime_bundle",
        "pink_bundle",
        "gray_bundle",
        "light_gray_bundle",
        "cyan_bundle",
        "purple_bundle",
        "blue_bundle",
        "brown_bundle",
        "green_bundle",
        "red_bundle",
        "black_bundle");

    private UseAnimations() {
    }

    public static String of(String itemName, Protocol protocol) {
        if (itemName == null) {
            return NONE;
        }
        String name = itemName.toLowerCase(Locale.ROOT);
        if (Spears.TAG.contains(name)) {
            return Spear.exists(protocol) ? SPEAR : NONE;
        }
        if (BOWS.contains(name)) {
            return BOW;
        }
        if (CROSSBOWS.contains(name)) {
            return CROSSBOW;
        }
        if (TRIDENTS.contains(name)) {
            return TRIDENT;
        }
        if (SHIELDS.contains(name)) {
            return BLOCK;
        }
        if (SPYGLASSES.contains(name)) {
            return SPYGLASS;
        }
        if (HORNS.contains(name)) {
            return TOOT_HORN;
        }
        if (BRUSHES.contains(name)) {
            return BRUSH;
        }
        if (BUNDLES.contains(name)) {
            return BUNDLE;
        }
        if (ConsumeDuration.isConsumable(name)) {
            return ConsumeDuration.isDrink(name) ? DRINK : EAT;
        }
        return NONE;
    }

    public static boolean isUsable(String itemName, Protocol protocol) {
        return !NONE.equals(of(itemName, protocol));
    }

    public static boolean isConsumption(String animation) {
        return EAT.equals(animation) || DRINK.equals(animation);
    }

    public static boolean slows(String itemName, Protocol protocol) {
        String animation = of(itemName, protocol);
        if (NONE.equals(animation)) {
            return false;
        }
        return UseSlowdown.multiplierFor(itemName, protocol) < 1.0F;
    }

    public static boolean permitsSprinting(String itemName, Protocol protocol) {
        if (!isUsable(itemName, protocol)) {
            return true;
        }
        return UseSlowdown.permitsSprintingWith(itemName, protocol);
    }
}
