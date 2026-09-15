package teacommontea.veritechasse.vanilla.Tools.Pickaxes;

import teacommontea.veritechasse.vanilla.Protocol;
import teacommontea.veritechasse.vanilla.Reality;
import teacommontea.veritechasse.vanilla.Tools.Pickaxes.Support.Pickaxe;
import teacommontea.veritechasse.vanilla.Tools.Support.Material;
import teacommontea.veritechasse.vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.vanilla.Tools.Support.ToolEra;

public final class CopperPickaxe {

    public static final String KEY = "copper_pickaxe";
    public static final Material MATERIAL = Materials.COPPER;

    public static final int MAX_DAMAGE = 190;
    public static final float MINING_SPEED = 5.0F;
    public static final int ENCHANTMENT_VALUE = 13;
    public static final int MINING_TIER = 1;

    public static final boolean FIRE_RESISTANT = false;

    private CopperPickaxe() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 21, 9);
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
