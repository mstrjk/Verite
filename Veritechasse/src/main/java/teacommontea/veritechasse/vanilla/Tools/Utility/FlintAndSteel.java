package teacommontea.veritechasse.Vanilla.Tools.Utility;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;
import teacommontea.veritechasse.Vanilla.Tools.Tags.CandleCakes;
import teacommontea.veritechasse.Vanilla.Tools.Tags.Candles;
import teacommontea.veritechasse.Vanilla.Tools.Tags.Campfires;

public final class FlintAndSteel {

    public static final String KEY = "flint_and_steel";

    public static final int MAX_DAMAGE = 64;
    public static final int ENCHANTMENT_VALUE = 1;
    public static final int DAMAGE_PER_USE = 1;

    public static final boolean ENCHANTABLE = true;
    public static final boolean FIRE_RESISTANT = false;

    private FlintAndSteel() {
    }

    public static boolean isCandle(String blockName, ToolEra era) {
        return Candles.contains(blockName, era) || CandleCakes.contains(blockName, era);
    }

    public static boolean isCampfire(String blockName, ToolEra era) {
        return Campfires.contains(blockName, era);
    }

    public static boolean lightsInPlace(String blockName, boolean lit, boolean waterlogged, ToolEra era) {
        if (lit || waterlogged) {
            return false;
        }
        return isCandle(blockName, era) || isCampfire(blockName, era);
    }

    public static boolean placesFire(String blockName, boolean lit, boolean waterlogged, boolean fireCanBePlaced, ToolEra era) {
        if (lightsInPlace(blockName, lit, waterlogged, era)) {
            return false;
        }
        return fireCanBePlaced;
    }

    public static boolean consumesDurability(String blockName, boolean lit, boolean waterlogged, boolean fireCanBePlaced, ToolEra era) {
        if (lightsInPlace(blockName, lit, waterlogged, era)) {
            return true;
        }
        return fireCanBePlaced;
    }

    public static int remaining(int damage) {
        int left = MAX_DAMAGE - damage;
        return left < 0 ? 0 : left;
    }

    public static boolean broken(int damage) {
        return damage >= MAX_DAMAGE;
    }

    public static int maxUses() {
        return MAX_DAMAGE;
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(remaining(damage));
    }
}
