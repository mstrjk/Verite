package teacommontea.veritechasse.vanilla.Tools.Utility;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;
import teacommontea.veritechasse.vanilla.Tools.Tags.ShearsExtremeBreakingSpeed;
import teacommontea.veritechasse.vanilla.Tools.Tags.ShearsMajorBreakingSpeed;
import teacommontea.veritechasse.vanilla.Tools.Tags.ShearsMinorBreakingSpeed;

public final class Shears {

    public static final String KEY = "shears";

    public static final int MAX_DAMAGE = 238;
    public static final int DAMAGE_PER_BLOCK = 1;
    public static final int DAMAGE_PER_INTERACTION = 1;

    public static final float EXTREME_SPEED = 15.0F;
    public static final float MAJOR_SPEED = 5.0F;
    public static final float MINOR_SPEED = 2.0F;
    public static final float DEFAULT_SPEED = 1.0F;

    public static final String COBWEB = "cobweb";

    public static final boolean ENCHANTABLE = false;
    public static final boolean FIRE_RESISTANT = false;

    private Shears() {
    }

    public static boolean speedIsTagDriven(Protocol protocol) {
        return protocol.atLeast(26, 2, 0);
    }

    public static float destroySpeed(String blockName, boolean isLeaves, boolean isWool, ToolEra era) {
        if (blockName == null) {
            return DEFAULT_SPEED;
        }
        if (COBWEB.equalsIgnoreCase(blockName)) {
            return EXTREME_SPEED;
        }
        if (isLeaves || ShearsExtremeBreakingSpeed.contains(blockName, era)) {
            return EXTREME_SPEED;
        }
        if (isWool || ShearsMajorBreakingSpeed.contains(blockName, era)) {
            return MAJOR_SPEED;
        }
        if (ShearsMinorBreakingSpeed.contains(blockName, era)) {
            return MINOR_SPEED;
        }
        return DEFAULT_SPEED;
    }

    public static boolean correctForDrops(String blockName) {
        return COBWEB.equalsIgnoreCase(blockName);
    }

    public static boolean consumesDurabilityMining(boolean isFire) {
        return !isFire;
    }

    public static boolean trimsGrowingPlant(boolean isGrowingPlantHead, boolean atMaxAge) {
        return isGrowingPlantHead && !atMaxAge;
    }

    public static int remaining(int damage) {
        int left = MAX_DAMAGE - damage;
        return left < 0 ? 0 : left;
    }

    public static boolean broken(int damage) {
        return damage >= MAX_DAMAGE;
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(remaining(damage));
    }
}
