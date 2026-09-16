package teacommontea.veritechasse.Vanilla.Tools.Shovels;

import teacommontea.veritechasse.Vanilla.Protocol;
import teacommontea.veritechasse.Vanilla.Reality;
import teacommontea.veritechasse.Vanilla.Tools.Shovels.Support.Shovel;
import teacommontea.veritechasse.Vanilla.Tools.Support.Material;
import teacommontea.veritechasse.Vanilla.Tools.Support.Materials;
import teacommontea.veritechasse.Vanilla.Tools.Support.ToolEra;

public final class CopperShovel {

    public static final String KEY = "copper_shovel";
    public static final Material MATERIAL = Materials.COPPER;

    public static final int MAX_DAMAGE = 190;
    public static final float MINING_SPEED = 5.0F;
    public static final int ENCHANTMENT_VALUE = 13;
    public static final int MINING_TIER = 1;

    public static final boolean FIRE_RESISTANT = false;

    private CopperShovel() {
    }

    public static boolean exists(Protocol protocol) {
        return protocol.atLeast(1, 21, 9);
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
