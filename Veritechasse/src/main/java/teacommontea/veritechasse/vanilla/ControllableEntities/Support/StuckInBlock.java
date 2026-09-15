package teacommontea.veritechasse.vanilla.ControllableEntities.Support;

import java.util.Locale;

import teacommontea.veritechasse.vanilla.Potions.Support.ActiveEffects;
import teacommontea.veritechasse.vanilla.Potions.Weaving;
import teacommontea.veritechasse.vanilla.Reality;

public final class StuckInBlock {

    public static final String COBWEB = "cobweb";
    public static final String POWDER_SNOW = "powder_snow";
    public static final String SWEET_BERRY_BUSH = "sweet_berry_bush";

    public static final double COBWEB_HORIZONTAL = 0.25D;
    public static final double COBWEB_VERTICAL = 0.05D;

    public static final double COBWEB_WEAVING_HORIZONTAL = 0.5D;
    public static final double COBWEB_WEAVING_VERTICAL = 0.25D;

    public static final double POWDER_SNOW_HORIZONTAL = 0.9D;
    public static final double POWDER_SNOW_VERTICAL = 1.5D;

    public static final double SWEET_BERRY_HORIZONTAL = 0.8D;
    public static final double SWEET_BERRY_VERTICAL = 0.75D;

    public static final double APPLIES_THRESHOLD_SQR = 1.0E-7D;

    public static final boolean RESETS_FALL_DISTANCE = true;

    private StuckInBlock() {
    }

    public static boolean isStickyBlock(String blockName) {
        if (blockName == null) {
            return false;
        }
        String name = blockName.toLowerCase(Locale.ROOT);
        return name.equals(COBWEB) || name.equals(POWDER_SNOW) || name.equals(SWEET_BERRY_BUSH);
    }

    public static double horizontalMultiplier(String blockName, boolean weaving) {
        if (blockName == null) {
            return 1.0D;
        }
        String name = blockName.toLowerCase(Locale.ROOT);
        if (name.equals(COBWEB)) {
            return weaving ? COBWEB_WEAVING_HORIZONTAL : COBWEB_HORIZONTAL;
        }
        if (name.equals(POWDER_SNOW)) {
            return POWDER_SNOW_HORIZONTAL;
        }
        if (name.equals(SWEET_BERRY_BUSH)) {
            return SWEET_BERRY_HORIZONTAL;
        }
        return 1.0D;
    }

    public static double verticalMultiplier(String blockName, boolean weaving) {
        if (blockName == null) {
            return 1.0D;
        }
        String name = blockName.toLowerCase(Locale.ROOT);
        if (name.equals(COBWEB)) {
            return weaving ? COBWEB_WEAVING_VERTICAL : COBWEB_VERTICAL;
        }
        if (name.equals(POWDER_SNOW)) {
            return POWDER_SNOW_VERTICAL;
        }
        if (name.equals(SWEET_BERRY_BUSH)) {
            return SWEET_BERRY_VERTICAL;
        }
        return 1.0D;
    }

    public static boolean hasWeaving(ActiveEffects effects) {
        return effects != null && effects.has(Weaving.KEY);
    }

    public static double horizontalMultiplier(String blockName, ActiveEffects effects) {
        return horizontalMultiplier(blockName, hasWeaving(effects));
    }

    public static double verticalMultiplier(String blockName, ActiveEffects effects) {
        return verticalMultiplier(blockName, hasWeaving(effects));
    }

    public static double horizontalAfter(double horizontal, String blockName, ActiveEffects effects) {
        return horizontal * horizontalMultiplier(blockName, effects);
    }

    public static double verticalAfter(double vertical, String blockName, ActiveEffects effects) {
        return vertical * verticalMultiplier(blockName, effects);
    }

    public static boolean acceleratesVertically(String blockName) {
        return verticalMultiplier(blockName, false) > 1.0D;
    }

    public static Reality horizontalFrom(double horizontal, String blockName, ActiveEffects effects) {
        return Reality.of(horizontalAfter(horizontal, blockName, effects));
    }
}
