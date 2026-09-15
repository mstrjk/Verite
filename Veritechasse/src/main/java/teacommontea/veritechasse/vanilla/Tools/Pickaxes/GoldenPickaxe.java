package teacommontea.veritechasse.vanilla.Tools.Pickaxes;

import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Pickaxes.Support.Pickaxe;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class GoldenPickaxe {

    public static final String KEY = "golden_pickaxe";
    public static final Material MATERIAL = Materials.GOLD;

    public static final int MAX_DAMAGE = 32;
    public static final float MINING_SPEED = 12.0F;
    public static final int ENCHANTMENT_VALUE = 22;
    public static final int MINING_TIER = 0;

    public static final boolean FIRE_RESISTANT = false;

    private GoldenPickaxe() {
    }

    public static float baseDestroySpeed(String blockName, ToolEra era) {
        return Pickaxe.baseDestroySpeed(MATERIAL, blockName, era);
    }

    public static boolean correctForDrops(boolean requiresCorrectToolForDrops, String blockName, ToolEra era) {
        return Pickaxe.correctForDrops(MATERIAL, requiresCorrectToolForDrops, blockName, era);
    }

    public static double attackDamage() {
        return Pickaxe.attackDamage(MATERIAL);
    }

    public static double attackSpeed() {
        return Pickaxe.attackSpeed();
    }

    public static Reality durabilityFrom(int damage) {
        return Reality.of(MAX_DAMAGE - damage);
    }
}
