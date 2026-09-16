package teacommontea.veritechasse.Vanilla.Tools.Pickaxes;

import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Pickaxes.Support.Pickaxe;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class IronPickaxe {

    public static final String KEY = "iron_pickaxe";
    public static final Material MATERIAL = Materials.IRON;

    public static final int MAX_DAMAGE = 250;
    public static final float MINING_SPEED = 6.0F;
    public static final int ENCHANTMENT_VALUE = 14;
    public static final int MINING_TIER = 2;

    public static final boolean FIRE_RESISTANT = false;

    private IronPickaxe() {
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
