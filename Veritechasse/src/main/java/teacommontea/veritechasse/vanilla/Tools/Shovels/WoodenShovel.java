package teacommontea.veritechasse.vanilla.Tools.Shovels;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Shovels.Support.Shovel;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class WoodenShovel {

    public static final String KEY = "wooden_shovel";
    public static final Material MATERIAL = Materials.WOOD;

    public static final int MAX_DAMAGE = 59;
    public static final float MINING_SPEED = 2.0F;
    public static final int ENCHANTMENT_VALUE = 15;
    public static final int MINING_TIER = 0;

    public static final boolean FIRE_RESISTANT = false;

    private WoodenShovel() {
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
