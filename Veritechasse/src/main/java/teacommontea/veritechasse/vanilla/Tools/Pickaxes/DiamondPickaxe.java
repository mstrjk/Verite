package teacommontea.veritechasse.Vanilla.Tools.Pickaxes;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Pickaxes.Support.Pickaxe;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class DiamondPickaxe {

    public static final String KEY = "diamond_pickaxe";
    public static final Material MATERIAL = Materials.DIAMOND;

    public static final int MAX_DAMAGE = 1561;
    public static final float MINING_SPEED = 8.0F;
    public static final int ENCHANTMENT_VALUE = 10;
    public static final int MINING_TIER = 3;

    public static final boolean FIRE_RESISTANT = false;

    private DiamondPickaxe() {
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
