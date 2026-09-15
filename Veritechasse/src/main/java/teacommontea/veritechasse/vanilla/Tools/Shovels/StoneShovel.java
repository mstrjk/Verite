package teacommontea.veritechasse.vanilla.Tools.Shovels;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Shovels.Support.Shovel;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class StoneShovel {

    public static final String KEY = "stone_shovel";
    public static final Material MATERIAL = Materials.STONE;

    public static final int MAX_DAMAGE = 131;
    public static final float MINING_SPEED = 4.0F;
    public static final int ENCHANTMENT_VALUE = 5;
    public static final int MINING_TIER = 1;

    public static final boolean FIRE_RESISTANT = false;

    private StoneShovel() {
    }

    public static float baseDestroySpeed(String blockName, ToolEra era) {
        return Shovel.baseDestroySpeed(MATERIAL, blockName, era);
    }

    public static boolean correctForDrops(boolean requiresCorrectToolForDrops, String blockName, ToolEra era) {
        return Shovel.correctForDrops(MATERIAL, requiresCorrectToolForDrops, blockName, era);
    }

    public static double attackDamage() {
        return Shovel.attackDamage(MATERIAL);
    }

    public static double attackSpeed() {
        return Shovel.attackSpeed();
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}
