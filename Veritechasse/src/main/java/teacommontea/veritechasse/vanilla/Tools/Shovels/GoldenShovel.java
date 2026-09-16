package teacommontea.veritechasse.Vanilla.Tools.Shovels;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Shovels.Support.Shovel;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class GoldenShovel {

    public static final String KEY = "golden_shovel";
    public static final Material MATERIAL = Materials.GOLD;

    public static final int MAX_DAMAGE = 32;
    public static final float MINING_SPEED = 12.0F;
    public static final int ENCHANTMENT_VALUE = 22;
    public static final int MINING_TIER = 0;

    public static final boolean FIRE_RESISTANT = false;

    private GoldenShovel() {
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
